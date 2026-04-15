package server.auction;

import server.models.Entity;
import server.models.auction.Auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AutoBid - Đăng ký đặt giá tự động cho một phiên đấu giá.
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  Cách hoạt động:                                        │
 * │  1. Bidder đăng ký AutoBid với maxBid và increment.     │
 * │  2. Khi có bid mới từ đối thủ, hệ thống tự động gọi    │
 * │     processAutoBids() để xử lý tất cả auto-bid.         │
 * │  3. Ưu tiên theo thứ tự: registeredAt sớm nhất trước.  │
 * │  4. Giá đặt không được vượt quá maxBid.                 │
 * │  5. Khi 2 auto-bid bằng nhau → người đăng ký trước win.│
 * └─────────────────────────────────────────────────────────┘
 */
public class AutoBid extends Entity {

    // -----------------------------------------------------------------------
    // Thuộc tính
    // -----------------------------------------------------------------------

    private final String auctionId;      // Phiên đấu giá mà auto-bid này thuộc về
    private final String bidderId;       // Người sở hữu auto-bid này
    private final double maxBid;         // Giá tối đa không được vượt qua
    private final double increment;      // Bước giá tăng mỗi lần hệ thống tự đặt
    private final LocalDateTime registeredAt; // Thời điểm đăng ký (dùng để ưu tiên)
    private boolean active;         // false nếu đã bị loại (maxBid không đủ)

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Tạo AutoBid MỚI (ID tự sinh, thời điểm đăng ký = ngay bây giờ).
     *
     * @param auctionId Phiên đấu giá
     * @param bidderId  Người đăng ký
     * @param maxBid    Giá tối đa
     * @param increment Bước tăng mỗi lần hệ thống tự đặt (phải >= minIncrement của Auction)
     */
    public AutoBid(String auctionId, String bidderId, double maxBid, double increment) {
        super(); // ID tự sinh UUID
        if (maxBid <= 0) throw new IllegalArgumentException("maxBid phải > 0");
        if (increment <= 0) throw new IllegalArgumentException("increment phải > 0");

        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Nạp AutoBid TỪ FILE / DATABASE (ID và registeredAt có sẵn).
     */
    public AutoBid(String id, String auctionId, String bidderId, double maxBid, double increment, LocalDateTime registeredAt, boolean active) {
        super(id);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
        this.active = active;
    }

    // -----------------------------------------------------------------------
    // Logic cá nhân: tính giá đặt tiếp theo
    // -----------------------------------------------------------------------

    /**
     * Tính giá mà auto-bid này sẽ đặt khi currentPrice của phiên là {@code currentPrice}.
     *
     * Quy tắc:
     * - Giá đề xuất = currentPrice + increment
     * - Nếu đề xuất > maxBid → trả về maxBid (cố gắng hết sức)
     * - Nếu maxBid <= currentPrice → không thể đặt, trả về -1
     *
     * @param currentPrice Giá hiện tại của phiên đấu giá
     * @return Giá sẽ đặt, hoặc -1 nếu auto-bid này không còn cạnh tranh được
     */
    public double calculateNextBid(double currentPrice) {
        if (!active || maxBid <= currentPrice) {
            return -1; // Không còn đủ khả năng cạnh tranh
        }
        double proposed = currentPrice + increment;
        return Math.min(proposed, maxBid); // Không vượt quá maxBid
    }

    /**
     * Kiểm tra xem auto-bid này có thể cạnh tranh ở mức giá hiện tại không.
     */
    public boolean canCompete(double currentPrice) {
        return active && maxBid > currentPrice;
    }

    /** Hủy kích hoạt auto-bid (khi maxBid đã bị vượt qua). */
    public void deactivate() {
        this.active = false;
    }

    // -----------------------------------------------------------------------
    // Static: Xử lý toàn bộ auto-bid cho một phiên đấu giá
    // -----------------------------------------------------------------------

    /**
     * Xử lý tất cả auto-bid đang hoạt động cho phiên {@code auction}.
     * Lọc các auto-bid còn active và có thể cạnh tranh.
     * Sắp xếp theo maxBid giảm dần → người có maxBid cao hơn được ưu tiên.
     * Nếu maxBid bằng nhau → ưu tiên theo registeredAt (đăng ký sớm hơn = ưu tiên hơn).
     * Auto-bid xếp #1 sẽ đặt giá = (maxBid của #2) + increment của #1.
     * Nếu chỉ có 1 auto-bid → đặt currentPrice + increment của nó.
     * Auto-bid nào không đủ điều kiện sẽ bị deactivate().
     * @param auction  Phiên đấu giá cần xử lý
     * @param autoBids Danh sách tất cả auto-bid của phiên này
     * @return ID của người thắng sau khi xử lý, null nếu không có ai đủ điều kiện
     */
    public static synchronized String processAutoBids(Auction auction, List<AutoBid> autoBids) {
        if (auction.getStatus() != Auction.AuctionStatus.RUNNING) {
            System.out.println("[AutoBid] Phiên không ở trạng thái RUNNING. Bỏ qua.");
            return null;
        }

        double currentPrice = auction.getCurrentHighestBid();

        // Bước 1: Lọc auto-bid còn cạnh tranh được
        List<AutoBid> candidates = new ArrayList<>();
        for (AutoBid ab : autoBids) {
            if (ab.canCompete(currentPrice)) {
                candidates.add(ab);
            } else {
                ab.deactivate(); // Loại các auto-bid không đủ mạnh
                System.out.printf("[AutoBid] %s bị loại (maxBid=%.2f <= currentPrice=%.2f)%n",
                        ab.getBidderId(), ab.getMaxBid(), currentPrice);
            }
        }

        if (candidates.isEmpty()) {
            System.out.println("[AutoBid] Không có auto-bid nào còn đủ điều kiện.");
            return null;
        }

        // Bước 2: Sắp xếp — maxBid cao hơn → xếp trước
        //                      maxBid bằng nhau → đăng ký sớm hơn → xếp trước (ưu tiên)
        candidates.sort(Comparator.comparingDouble(AutoBid::getMaxBid).reversed().thenComparing(AutoBid::getRegisteredAt));

        AutoBid winner = candidates.get(0); // Người có ưu thế cao nhất
        double bidPrice;

        if (candidates.size() == 1) {
            // Chỉ 1 người → đặt thêm increment của chính họ
            bidPrice = currentPrice + winner.getIncrement();
        } else {
            // Nhiều người → winner đặt bằng maxBid của người xếp 2 + increment của winner
            // (vừa đủ để thắng, không tiêu quá nhiều)
            AutoBid runnerUp = candidates.get(1);
            bidPrice = runnerUp.getMaxBid() + winner.getIncrement();
        }

        // Đảm bảo không vượt quá maxBid của winner
        bidPrice = Math.min(bidPrice, winner.getMaxBid());

        // Bước 3: Kiểm tra hợp lệ và đặt giá vào phiên
        if (!auction.isActive()) {
            System.out.println("[AutoBid] Phiên không còn hoạt động.");
            return null;
        }
        if (bidPrice <= auction.getCurrentHighestBid()) {
            System.out.printf("[AutoBid] Giá tự động %.2f không cao hơn giá hiện tại %.2f.%n",
                    bidPrice, auction.getCurrentHighestBid());
            return null;
        }

        // Cập nhật trực tiếp vào Auction
        auction.setCurrentHighestBid(bidPrice);
        auction.setHighestBidderId(winner.getBidderId());

        System.out.printf("[AutoBid] Tự động đặt giá: %s → $%.2f%n",
                winner.getBidderId(), bidPrice);

        // Deactivate những người thua
        for (int i = 1; i < candidates.size(); i++) {
            AutoBid loser = candidates.get(i);
            if (loser.getMaxBid() < bidPrice) {
                loser.deactivate();
                System.out.printf("[AutoBid] %s bị loại (maxBid=%.2f < newPrice=%.2f)%n",
                        loser.getBidderId(), loser.getMaxBid(), bidPrice);
            }
        }
        return winner.getBidderId();
    }

    /**
     * Xử lý khi một bid THỦ CÔNG được đặt (kích hoạt phản ứng dây chuyền).
     *
     * Khi bidder thường đặt giá, hệ thống kiểm tra xem có auto-bid nào
     * phản ứng lại không, và chạy vòng lặp cho đến khi ổn định.
     *
     * @param manualBidAmount Giá vừa được đặt thủ công
     * @param manualBidderId  ID người vừa đặt thủ công
     * @param auction         Phiên đấu giá
     * @param autoBids        Danh sách auto-bid của phiên
     */
    public static synchronized void handleManualBid(double manualBidAmount, String manualBidderId, Auction auction, List<AutoBid> autoBids) {
        System.out.printf("%n[AutoBid] Phát hiện bid thủ công từ %s: $%.2f. Kích hoạt auto-bid...%n",
                manualBidderId, manualBidAmount);

        int maxRounds = 20; // Chặn vòng lặp vô tận
        int round     = 0;

        while (round < maxRounds) {
            round++;
            String prevWinner = auction.getHighestBidderId();
            String newWinner  = processAutoBids(auction, autoBids);

            // Không có auto-bid nào phản ứng → dừng
            if (newWinner == null) break;

            // Người thắng là chính bidder thủ công này → không cần tự đặt thêm
            if (newWinner.equals(manualBidderId)) break;

            // Người thắng không đổi → ổn định, dừng vòng lặp
            if (newWinner.equals(prevWinner)) break;

            System.out.printf("[AutoBid] Vòng %d: %s đang dẫn với $%.2f%n",
                    round, newWinner, auction.getCurrentHighestBid());
        }

        System.out.printf("[AutoBid] Kết thúc xử lý. Giá hiện tại: $%.2f | Người dẫn: %s%n%n",
                auction.getCurrentHighestBid(), auction.getHighestBidderId());
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getMaxBid() { return maxBid; }
    public double getIncrement() { return increment; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return String.format( "AutoBid{bidderId='%s', maxBid=%.2f, increment=%.2f, registeredAt=%s, active=%b}", bidderId, maxBid, increment, registeredAt, active);
    }
}

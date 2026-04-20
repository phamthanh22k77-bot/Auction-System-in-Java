package server.auction;

import server.models.Entity;
import server.models.auction.Auction;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Khách thể đại diện cho cấu hình Đấu giá tự động (Auto-Bid) của một Bidder.
 * Cơ chế:
 * 1. Đăng ký thông qua maxBid và increment.
 * 2. Lưu trữ và sắp xếp trong PriorityQueue theo mức độ ưu tiên (Giá tối đa).
 */
public class AutoBid extends Entity {

    private final String auctionId; // Phiên đấu giá mà auto-bid này thuộc về
    private final String bidderId; // Người sở hữu auto-bid này
    private final double maxBid; // Giá tối đa không được vượt qua
    private final double increment; // Bước giá tăng mỗi lần hệ thống tự đặt
    private final LocalDateTime registeredAt; // Thời điểm đăng ký (dùng để ưu tiên)
    private boolean active; // false nếu đã bị loại (maxBid không đủ)

    /**
     * Tạo AutoBid mới và đánh dấu thời điểm đăng ký thực tế.
     * @param auctionId Thuộc phiên đấu giá nào
     * @param bidderId  Người đăng ký
     * @param maxBid    Mức giá trần tự động
     * @param increment Bước giá tự tăng
     */
    public AutoBid(String auctionId, String bidderId, double maxBid, double increment) {
        super(); // ID tự sinh UUID
        if (maxBid <= 0)
            throw new IllegalArgumentException("maxBid phải > 0");
        if (increment <= 0)
            throw new IllegalArgumentException("increment phải > 0");

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
    public AutoBid(String id, String auctionId, String bidderId,
                   double maxBid, double increment,
                   LocalDateTime registeredAt, boolean active) {
        super(id);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
        this.active = active;
    }

    /**
     * Đề xuất mức đặt giá tiếp theo dựa trên currentPrice.
     * Đảm bảo giá không vượt qua maxBid.
     *
     * @return Giá hợp lệ hoặc -1 nếu không thể đáp ứng khả năng cạnh tranh.
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

    /**
     * Cốt lõi của AutoBid: Kích hoạt thuật toán giải quyết va chạm giữa các bộ AutoBid.
     * Sử dụng cấu trúc Max-Heap (PriorityQueue) để tìm ra người chiến thắng O(logN).
     *
     * @return ID người thắng hoặc null.
     */
    public static synchronized String processAutoBids(Auction auction, List<AutoBid> autoBids) {
        if (auction.getStatus() != Auction.AuctionStatus.RUNNING) {
            System.out.println("[AutoBid] Phiên không ở trạng thái RUNNING. Bỏ qua.");
            return null;
        }

        double currentPrice = auction.getCurrentHighestBid();

        // Bước 1: Lọc và nạp vào PriorityQueue tự sắp xếp (Max-Heap theo maxBid)
        // Ưu tiên: maxBid cao hơn → trước; maxBid bằng nhau → đăng ký SỚM hơn → trước
        PriorityQueue<AutoBid> pq = new PriorityQueue<>(
                Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
                        .thenComparing(AutoBid::getRegisteredAt) // earlier = higher priority
        );

        for (AutoBid ab : autoBids) {
            if (ab.canCompete(currentPrice)) {
                pq.offer(ab); // O(log n) — heap tự sắp xếp
            } else {
                ab.deactivate();
                System.out.printf("[AutoBid] %s bị loại (maxBid=%.2f <= currentPrice=%.2f)%n",
                        ab.getBidderId(), ab.getMaxBid(), currentPrice);
            }
        }

        if (pq.isEmpty()) {
            System.out.println("[AutoBid] Không có auto-bid nào còn đủ điều kiện.");
            return null;
        }

        // Bước 2: Lấy winner (poll = lấy + xóa phần tử đứng đầu heap — O(log n))
        AutoBid winner = pq.poll();
        double bidPrice;

        if (pq.isEmpty()) {
            // Chỉ còn 1 người → đặt thêm increment của chính họ
            bidPrice = currentPrice + winner.getIncrement();
        } else {
            // Nhiều người → winner đặt bằng maxBid của runnerUp + increment của winner
            // peek() = xem phần tử đứng đầu mà KHÔNG xóa khỏi queue — O(1)
            AutoBid runnerUp = pq.peek();
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

        // Deactivate những người thua còn lại trong queue
        for (AutoBid loser : pq) {
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
    public static synchronized void handleManualBid(double manualBidAmount,
                                                    String manualBidderId,
                                                    Auction auction,
                                                    List<AutoBid> autoBids) {
        System.out.printf("%n[AutoBid] Phát hiện bid thủ công từ %s: $%.2f. Kích hoạt auto-bid...%n",
                manualBidderId, manualBidAmount);

        int maxRounds = 20; // Chặn vòng lặp vô tận trong trường hợp cực đoan
        int round = 0;

        while (round < maxRounds) {
            round++;
            String prevWinner = auction.getHighestBidderId();
            String newWinner = processAutoBids(auction, autoBids);

            // Không có auto-bid nào phản ứng → dừng
            if (newWinner == null)
                break;

            // Người thắng là chính bidder thủ công này → không cần tự đặt thêm
            if (newWinner.equals(manualBidderId))
                break;

            // Người thắng không đổi → ổn định, dừng vòng lặp
            if (newWinner.equals(prevWinner))
                break;

            System.out.printf("[AutoBid] Vòng %d: %s đang dẫn với $%.2f%n",
                    round, newWinner, auction.getCurrentHighestBid());
        }

        System.out.printf("[AutoBid] Kết thúc xử lý. Giá hiện tại: $%.2f | Người dẫn: %s%n%n",
                auction.getCurrentHighestBid(), auction.getHighestBidderId());
    }



    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return String.format(
                "AutoBid{bidderId='%s', maxBid=%.2f, increment=%.2f, registeredAt=%s, active=%b}",
                bidderId, maxBid, increment, registeredAt, active);
    }
}

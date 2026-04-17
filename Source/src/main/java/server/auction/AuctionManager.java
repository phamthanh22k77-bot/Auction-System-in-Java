package server.auction;

import server.dao.AuctionDAO;
import server.models.auction.Auction;
import server.models.auction.Auction.AuctionStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AuctionManager — Singleton quản lý toàn bộ các phiên đấu giá.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  Cơ chế Singleton: Double-Checked Locking (DCL)             │
 * │  - volatile đảm bảo visibility giữa các thread               │
 * │  - synchronized chỉ chạy 1 lần duy nhất khi khởi tạo        │
 * │  - Sau khi có instance, không còn overhead của lock nữa      │
 * │                                                              │
 * │  Thread-safety cho dữ liệu:                                  │
 * │  - Mỗi thao tác ghi (tao/dat/capNhat) dùng synchronized      │
 * │  - layTatCa() trả về bản sao phòng thủ (defensive copy)      │
 * │  - AuctionDAO đã được gọi bên trong synchronized block       │
 * └──────────────────────────────────────────────────────────────┘
 */
public class AuctionManager {

    // =========================================================================
    // Singleton — Double-Checked Locking
    // =========================================================================

    /**
     * volatile bắt buộc: ngăn CPU/JVM tái sắp xếp lệnh (instruction reordering).
     * Nếu thiếu volatile, thread B có thể thấy instance != null nhưng object
     * bên trong chưa được khởi tạo hoàn toàn.
     */
    private static volatile AuctionManager instance;

    /** Constructor private — cấm new AuctionManager() từ bên ngoài. */
    private AuctionManager() {}

    /**
     * Trả về instance duy nhất của AuctionManager.
     *
     * Luồng hoạt động:
     * 1. Kiểm tra lần 1 (KHÔNG lock)  → hầu hết các lần gọi dừng ở đây.
     * 2. Vào synchronized block         → chỉ 1 thread được vào tại 1 thời điểm.
     * 3. Kiểm tra lần 2 (CÓ lock)      → ngăn tạo trùng khi 2 thread vào cùng lúc.
     */
    public static AuctionManager getInstance() {
        if (instance == null) {                      // Kiểm tra lần 1 — không lock (fast path)
            synchronized (AuctionManager.class) {
                if (instance == null) {              // Kiểm tra lần 2 — đã lock (safe path)
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // =========================================================================
    // Thuộc tính
    // =========================================================================

    /**
     * Cache in-memory toàn bộ phiên đấu giá.
     * Dùng ArrayList thông thường — thread-safety được xử lý qua synchronized method.
     */
    private final List<Auction> auctions = new ArrayList<>();

    /**
     * DAO để persist/load dữ liệu từ file JSON (data/auctions.json).
     * Được chia sẻ duy nhất qua Singleton — không cần tạo lại ở mỗi class.
     */
    private final AuctionDAO dao = new AuctionDAO();

    // =========================================================================
    // Anti-Sniping — Hằng số cấu hình
    // =========================================================================

    /** Ngưỡng thời gian (giây) kích hoạt Anti-Sniping. */
    private static final long ANTI_SNIPE_THRESHOLD_SECONDS = 120; // 2 phút

    /** Thời gian gia hạn thêm (giây) khi Anti-Sniping kích hoạt. */
    private static final long ANTI_SNIPE_EXTENSION_SECONDS = 120; // +2 phút


    // =========================================================================
    // Khởi động — nạp dữ liệu từ file
    // =========================================================================

    /**
     * Nạp toàn bộ dữ liệu từ file JSON vào bộ nhớ.
     * Gọi 1 lần khi server khởi động (trước khi nhận request từ client).
     */
    public synchronized void khoiDong() throws IOException {
        auctions.clear();
        auctions.addAll(dao.loadAll());

        // Cập nhật lại trạng thái theo thời gian thực ngay khi nạp
        for (Auction a : auctions) {
            a.updateStatus();
        }

        System.out.println("[AuctionManager] Đã nạp " + auctions.size()
                + " phiên đấu giá. Sẵn sàng nhận kết nối.");
    }

    // =========================================================================
    // Tạo phiên đấu giá mới
    // =========================================================================

    /**
     * Tạo một phiên đấu giá mới và lưu ngay vào file.
     *
     * @param itemId              ID vật phẩm được đấu giá
     * @param sellerId            ID người bán
     * @param startTime           Thời gian bắt đầu
     * @param endTime             Thời gian kết thúc
     * @param startingPrice       Giá khởi điểm
     * @param minimumBidIncrement Bước giá tối thiểu
     * @return Auction vừa tạo (đã có ID)
     */
    public synchronized Auction taoPhien(String itemId, String sellerId,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         double startingPrice, double minimumBidIncrement)
            throws IOException {

        Auction auction = new Auction(itemId, sellerId, startTime, endTime,
                startingPrice, minimumBidIncrement);

        auctions.add(auction);
        dao.them(auction); // Persist ngay lập tức

        System.out.printf("[AuctionManager] Tạo phiên mới [%s] — Item: %s | Seller: %s%n",
                auction.getId(), itemId, sellerId);

        return auction;
    }

    // =========================================================================
    // Đặt giá thủ công (Manual Bid)
    // =========================================================================

    /**
     * Xử lý một lượt đặt giá thủ công từ client.
     *
     * Quy trình:
     * 1. Kiểm tra phiên tồn tại và đang hoạt động.
     * 2. Kiểm tra bidAmount > giá hiện tại.
     * 3. Cập nhật giá và người dẫn đầu.
     * 4. Kích hoạt chuỗi phản ứng Auto-Bid (nếu có).
     * 5. Persist trạng thái mới vào file.
     *
     * @param auctionId   ID phiên đấu giá
     * @param bidderId    ID người đặt giá
     * @param bidAmount   Giá muốn đặt
     * @param autoBids    Danh sách auto-bid của phiên này (có thể rỗng)
     * @return true nếu đặt giá thành công, false nếu không hợp lệ
     */
    public synchronized boolean datGia(String auctionId, String bidderId,
                                       double bidAmount, List<AutoBid> autoBids)
            throws IOException {

        Auction auction = timTheoId(auctionId);

        // Kiểm tra điều kiện hợp lệ
        if (auction == null) {
            System.out.println("[AuctionManager] Không tìm thấy phiên: " + auctionId);
            return false;
        }
        if (!auction.isActive()) {
            System.out.printf("[AuctionManager] Phiên [%s] không còn hoạt động (status=%s).%n",
                    auctionId, auction.getStatus());
            return false;
        }
        if (bidAmount <= auction.getCurrentHighestBid()) {
            System.out.printf("[AuctionManager] Giá đặt %.2f không cao hơn giá hiện tại %.2f.%n",
                    bidAmount, auction.getCurrentHighestBid());
            return false;
        }

        // Cập nhật giá vào Auction object
        auction.setCurrentHighestBid(bidAmount);
        auction.setHighestBidderId(bidderId);

        System.out.printf("[AuctionManager] %s đặt giá $%.2f cho phiên [%s].%n",
                bidderId, bidAmount, auctionId);

        // Kích hoạt phản ứng dây chuyền Auto-Bid (nếu có đăng ký)
        if (autoBids != null && !autoBids.isEmpty()) {
            AutoBid.handleManualBid(bidAmount, bidderId, auction, autoBids);
        }

        // Anti-Sniping: luôn chạy sau mọi bid (dù có hay không có auto-bid)
        applyAntiSniping(auction);

        // Persist trạng thái mới (bao gồm cả endTime nếu bị gia hạn)
        dao.capNhat(auction);
        return true;
    }

    // =========================================================================
    // Anti-Sniping — Gia hạn phiên khi có bid vào phút cuối
    // =========================================================================

    /**
     * Kiểm tra và gia hạn endTime nếu bid xảy ra gần cuối phiên.
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │  Sniping = chờ đến giây cuối mới bid để đối thủ không     │
     * │  kịp phản ứng.                                             │
     * │  Giải pháp: nếu bid trong ngưỡng THRESHOLD giây cuối    │
     * │  → tự động đẩy endTime thêm EXTENSION giây.             │
     * └─────────────────────────────────────────────────────────┘
     */
    private void applyAntiSniping(Auction auction) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        long secondsLeft = ChronoUnit.SECONDS.between(now, endTime);

        if (secondsLeft >= 0 && secondsLeft < ANTI_SNIPE_THRESHOLD_SECONDS) {
            LocalDateTime newEndTime = endTime.plusSeconds(ANTI_SNIPE_EXTENSION_SECONDS);
            auction.setEndTime(newEndTime);

            System.out.printf(
                    "[Anti-Snipe] Phiên [%s]: còn %ds < ngưỡng %ds → gia hạn +%ds. EndTime mới: %s%n",
                    auction.getId(), secondsLeft,
                    ANTI_SNIPE_THRESHOLD_SECONDS,
                    ANTI_SNIPE_EXTENSION_SECONDS,
                    newEndTime
            );
        }
    }

    // =========================================================================
    // Đăng ký Auto-Bid cho một phiên
    // =========================================================================

    /**
     * Cập nhật trạng thái tất cả phiên dựa trên thời gian thực.
     * Nên được gọi định kỳ bởi một ScheduledExecutorService trên server.
     *
     * Ví dụ gọi định kỳ mỗi 30 giây:
     * <pre>
     *   ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
     *   scheduler.scheduleAtFixedRate(() -> {
     *       try { AuctionManager.getInstance().capNhatTrangThai(); }
     *       catch (IOException e) { e.printStackTrace(); }
     *   }, 0, 30, TimeUnit.SECONDS);
     * </pre>
     */
    public synchronized void capNhatTrangThai() throws IOException {
        int updated = 0;
        for (Auction a : auctions) {
            AuctionStatus truoc = a.getStatus();
            a.updateStatus();
            if (a.getStatus() != truoc) {
                dao.capNhat(a); // Chỉ persist khi trạng thái thực sự thay đổi
                updated++;
                System.out.printf("[AuctionManager] Phiên [%s]: %s → %s%n",
                        a.getId(), truoc, a.getStatus());
            }
        }
        if (updated > 0) {
            System.out.println("[AuctionManager] Đã cập nhật " + updated + " phiên.");
        }
    }

    // =========================================================================
    // Kết thúc / Hủy phiên thủ công (do Admin)
    // =========================================================================

    /**
     * Đánh dấu phiên đấu giá là FINISHED (hoặc CANCELED).
     *
     * @param auctionId ID phiên cần kết thúc
     * @param huy       true = CANCELED, false = FINISHED
     * @return true nếu thao tác thành công
     */
    public synchronized boolean ketThucPhien(String auctionId, boolean huy)
            throws IOException {

        Auction auction = timTheoId(auctionId);
        if (auction == null) return false;

        AuctionStatus newStatus = huy ? AuctionStatus.CANCELED : AuctionStatus.FINISHED;
        auction.setStatus(newStatus);
        dao.capNhat(auction);

        System.out.printf("[AuctionManager] Phiên [%s] đã chuyển sang %s.%n",
                auctionId, newStatus);
        return true;
    }

    /**
     * Đánh dấu phiên đã thanh toán (PAID).
     *
     * @param auctionId ID phiên cần đánh dấu
     * @return true nếu thao tác thành công
     */
    public synchronized boolean xacNhanThanhToan(String auctionId) throws IOException {
        Auction auction = timTheoId(auctionId);
        if (auction == null) return false;
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("[AuctionManager] Chỉ phiên FINISHED mới có thể PAID.");
            return false;
        }
        auction.setStatus(AuctionStatus.PAID);
        dao.capNhat(auction);
        System.out.printf("[AuctionManager] Phiên [%s] đã PAID.%n", auctionId);
        return true;
    }

    // =========================================================================
    // Tìm kiếm & Lọc (Read-only — không cần synchronized)
    // =========================================================================

    /**
     * Tìm phiên theo ID. Tìm trong cache in-memory (không đọc file).
     *
     * @param id ID phiên đấu giá
     * @return Auction nếu tìm thấy, null nếu không
     */
    public Auction timTheoId(String id) {
        for (Auction a : auctions) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    /**
     * Lọc danh sách phiên theo trạng thái.
     *
     * @param status Trạng thái cần lọc
     * @return Danh sách các phiên có trạng thái tương ứng (bản sao)
     */
    public List<Auction> layTheoTrangThai(AuctionStatus status) {
        List<Auction> result = new ArrayList<>();
        for (Auction a : auctions) {
            if (a.getStatus() == status) result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Lọc danh sách phiên theo seller.
     *
     * @param sellerId ID người bán
     * @return Danh sách phiên của seller (bản sao)
     */
    public List<Auction> layTheoSeller(String sellerId) {
        List<Auction> result = new ArrayList<>();
        for (Auction a : auctions) {
            if (a.getSellerId().equals(sellerId)) result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Trả về toàn bộ danh sách phiên đấu giá.
     * Trả về bản sao phòng thủ (defensive copy) để tránh external mutation.
     */
    public List<Auction> layTatCa() {
        return Collections.unmodifiableList(new ArrayList<>(auctions));
    }

    /**
     * Trả về số lượng phiên đang trong bộ nhớ.
     */
    public int soLuongPhien() {
        return auctions.size();
    }
}

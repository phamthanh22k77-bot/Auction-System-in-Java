package server.auction;

import server.dao.AuctionDAO;
import server.models.auction.Auction;
import server.models.auction.Auction.AuctionStatus;
import server.models.auction.BidTransaction;
import server.models.network.AuctionClient;
import server.auction.AuctionLowBidException;
import server.auction.AuctionNotRegisteredException;
import server.auction.AuctionClientIsOwnerException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//AuctionManager — Singleton, Thread-safe, Observer Pattern, PriorityQueue

public class AuctionManager {
    private static volatile AuctionManager instance;

    private AuctionManager() {
    }

    public static AuctionManager getInstance() {
        if (instance == null) { // Kiểm tra lần 1 — không lock (fast path)
            synchronized (AuctionManager.class) {
                if (instance == null) { // Kiểm tra lần 2 — đã lock (safe path)
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // Danh sách lưu trữ trong bộ nhớ in-memory
    private final List<Auction> auctions = new java.util.concurrent.CopyOnWriteArrayList<>();

    // DAO để persist/load dữ liệu từ file JSON (data/auctions.json).

    private final AuctionDAO dao = new AuctionDAO();

    public AuctionDAO getDao() {
        return dao;
    }

    // Danh sách các Observer đã đăng ký nhận thông báo.
    private final List<BidObserver> observers = new java.util.concurrent.CopyOnWriteArrayList<>();

    // Cho phép đăng ký nhận thông báo
    public void addObserver(BidObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println(
                    "[Observer] Đã có một khán giả đăng ký nhận tin Bid mới: " + observer.getClass().getSimpleName());
        }
    }

    // Xóa đăng ký
    public void removeObserver(BidObserver observer) {
        if (observers.remove(observer)) {
            System.out.println("[Observer] Đã hủy đăng ký nhận tin của: " + observer.getClass().getSimpleName());
        }
    }

    // HÀM PHÁT LOA: Gọi tất cả những ai có trong danh sách đang đăng ký để báo tin.
    private void notifyObservers(Auction auction, String bidderId, double bidAmount) {
        for (BidObserver obs : observers) {
            obs.onBidPlaced(auction, bidderId, bidAmount);
        }
    }

    // Thời gian (giây) kích hoạt Anti-Sniping.
    private static final long ANTI_SNIPE_THRESHOLD_SECONDS = 30; // Dưới 30s

    // Thời gian đặt lại (giây) khi Anti-Sniping kích hoạt.
    private static final long ANTI_SNIPE_EXTENSION_SECONDS = 30; // Reset về 30s

    // Nạp toàn bộ dữ liệu từ file JSON vào bộ nhớ. Gọi 1 lần khi server khởi động
    // (trước khi nhận request từ client).
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

    public Auction taoPhien(String itemId, String sellerId,
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

    // Xử lý lượt Bid thủ công từ người dùng, kích hoạt phản ứng dây chuyền.
    public boolean datGia(BidTransaction transaction, AuctionClient client)
            throws IOException, AuctionLowBidException, AuctionNotRegisteredException, AuctionClientIsOwnerException {

        Auction auction = timTheoId(transaction.getAuctionId());
        if (auction == null) {
            return false;
        }

        synchronized (auction) {
            // GỌI LOGIC TRỰC TIẾP TRONG AUCTION (Đã gộp DatGia + AntiSniping)
            auction.addBid(transaction, client);

            // Notify Observers (nếu có)
            notifyObservers(auction, transaction.getBidderId(), transaction.getBidAmount());

            // Persist trạng thái mới
            dao.capNhat(auction);
            return true;
        }
    }

    // Cơ chế Anti-Sniping: Nếu người chơi đặt giá sát giờ kết thúc tự động cộng dồn
    // thời gian (Extension) cho phiên đấu giá.
    private void applyAntiSniping(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        long secondsLeft = ChronoUnit.SECONDS.between(now, endTime);

        if (secondsLeft >= 0 && secondsLeft < ANTI_SNIPE_THRESHOLD_SECONDS) {
            LocalDateTime newEndTime = now.plusSeconds(ANTI_SNIPE_EXTENSION_SECONDS); // Đặt lại về 30s từ thời điểm hiện tại
            
            if (newEndTime.isAfter(endTime)) {
                auction.setEndTime(newEndTime);
                System.out.printf(
                        "[Anti-Snipe] Phiên [%s]: còn %ds < ngưỡng %ds → Đặt lại thời gian về %ds. EndTime mới: %s%n",
                        auction.getId(), secondsLeft,
                        ANTI_SNIPE_THRESHOLD_SECONDS,
                        ANTI_SNIPE_EXTENSION_SECONDS,
                        newEndTime);
            }
        }
    }

    public void capNhatTrangThai() throws IOException {
        int updated = 0;
        for (Auction a : auctions) {
            synchronized (a) {
                AuctionStatus truoc = a.getStatus();
                a.updateStatus();
                if (a.getStatus() != truoc) {
                    dao.capNhat(a); // Chỉ persist khi trạng thái thực sự thay đổi
                    updated++;
                    System.out.printf("[AuctionManager] Phiên [%s]: %s → %s%n",
                            a.getId(), truoc, a.getStatus());
                }
            }
        }
        if (updated > 0) {
            System.out.println("[AuctionManager] Đã cập nhật " + updated + " phiên.");
        }
    }

    public boolean ketThucPhien(String auctionId, boolean huy)
            throws IOException {

        Auction auction = timTheoId(auctionId);
        if (auction == null)
            return false;

        synchronized (auction) {
            AuctionStatus newStatus = huy ? AuctionStatus.CANCELED : AuctionStatus.FINISHED;
            auction.setStatus(newStatus);
            dao.capNhat(auction);

            System.out.printf("[AuctionManager] Phiên [%s] đã chuyển sang %s.%n",
                    auctionId, newStatus);
            return true;
        }
    }

    public boolean xacNhanThanhToan(String auctionId) throws IOException {
        Auction auction = timTheoId(auctionId);
        if (auction == null)
            return false;

        synchronized (auction) {
            if (auction.getStatus() != AuctionStatus.FINISHED) {
                System.out.println("[AuctionManager] Chỉ phiên FINISHED mới có thể PAID.");
                return false;
            }
            auction.setStatus(AuctionStatus.PAID);
            dao.capNhat(auction);
            System.out.printf("[AuctionManager] Phiên [%s] đã PAID.%n", auctionId);
            return true;
        }
    }

    // Tự động cập nhật trạng thái của tất cả các phiên dựa trên thời gian thực
    public synchronized void updateAllAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        for (Auction auction : auctions) {
            if (now.isBefore(auction.getStartTime())) {
                auction.setStatus(Auction.AuctionStatus.OPEN);
            } else if (now.isAfter(auction.getEndTime())) {
                auction.setStatus(Auction.AuctionStatus.FINISHED);
            } else {
                // Trong khoảng thời gian từ Start đến End
                auction.setStatus(Auction.AuctionStatus.RUNNING);
            }
        }
    }

    // Tìm phiên theo ID. Tìm trong cache in-memory (không đọc file).

    public Auction timTheoId(String id) {
        for (Auction a : auctions) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }

    // Lọc danh sách phiên theo trạng thái.
    public List<Auction> layTheoTrangThai(AuctionStatus status) {
        List<Auction> result = new ArrayList<>();
        for (Auction a : auctions) {
            if (a.getStatus() == status)
                result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    // Lọc danh sách phiên theo seller.
    public List<Auction> layTheoSeller(String sellerId) {
        List<Auction> result = new ArrayList<>();
        for (Auction a : auctions) {
            if (a.getSellerId().equals(sellerId))
                result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    // Trả về toàn bộ danh sách phiên đấu giá. Trả về bản sao phòng thủ (defensive
    // copy) để tránh external mutation.
    public List<Auction> layTatCa() {
        return Collections.unmodifiableList(new ArrayList<>(auctions));
    }

    // Trả về số lượng phiên đang trong bộ nhớ.
    public int soLuongPhien() {
        return auctions.size();
    }
}

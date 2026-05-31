package server.auction;

import server.dao.AuctionDAO;
import server.models.auction.Auction;
import server.models.auction.Auction.AuctionStatus;
import server.models.auction.BidTransaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//AuctionManager — Singleton, Thread-safe, Observer Pattern, PriorityQueue

public class AuctionManager {
    private static volatile AuctionManager instance;
    private static final int ANTI_SNIPE_THRESHOLD_SECONDS = 30; // Nếu bid trong 30s cuối
    private static final int ANTI_SNIPE_EXTENSION_SECONDS = 30; // Gia hạn thêm thành 30s từ lúc bid

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

    public List<Auction> getAuctions() {
        return auctions;
    }

    // DAO để persist/load dữ liệu từ file JSON (data/auctions.json).
    private final AuctionDAO dao = new AuctionDAO();

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

    // Nạp toàn bộ dữ liệu từ file JSON vào bộ nhớ. Gọi 1 lần khi server khởi động
    public synchronized void khoiDong() throws IOException {
        auctions.clear();
        auctions.addAll(dao.loadAll());

        // Cập nhật lại trạng thái theo thời gian thực ngay khi nạp
        for (Auction a : auctions) {
            a.updateStatus();
        }

        System.out.println("[AuctionManager] Đã nạp " + auctions.size() + " phiên đấu giá. Sẵn sàng nhận kết nối.");
    }

    public boolean datGia(BidTransaction transaction)
            throws IOException, server.auction.AuctionLowBidException, server.models.network.ServerNoAuctionException {

        Auction auction = timTheoId(transaction.getAuctionId());
        if (auction == null) {
            return false;
        }

        synchronized (auction) {
            auction.updateStatus(); // Chỉ cập nhật trạng thái của riêng phiên này

            // GỌI VALIDATE TỪ TRANSACTION
            boolean isValid = transaction.validate(auction);

            if (!isValid) {
                System.out.println("[AuctionManager] Giao dịch không hợp lệ: " + transaction);
                // Ném lỗi thay vì chỉ return false để Server gửi ERROR về Client
                throw new server.auction.AuctionLowBidException("Giá đặt không hợp lệ hoặc thấp hơn giá hiện tại.");
            }

            // Lưu vết giao dịch vào lịch sử phiên đấu giá (chỉ lưu khi thành công)
            auction.addBidToHistory(transaction);

            // THỰC HIỆN ĐẶT GIÁ (Sử dụng logic đóng gói trong Auction)
            auction.setCurrentHighestBid(transaction.getBidAmount());
            auction.setHighestBidderId(transaction.getBidderId());

            System.out.printf("[AuctionManager] %s đặt giá $%.2f cho phiên [%s]. Kết quả: %s%n",
                    transaction.getBidderId(), transaction.getBidAmount(), transaction.getAuctionId(),
                    transaction.getStatus());

            // Anti-Sniping: luôn chạy sau mọi bid
            applyAntiSniping(auction);

            notifyObservers(auction, transaction.getBidderId(), transaction.getBidAmount());

            // Persist trạng thái mới (bao gồm cả endTime nếu bị gia hạn)
            dao.capNhat(auction);
            return true;
        }
    }

    private static final int ANTI_SNIPE_LIMIT = 5;

    // Cơ chế Anti-Sniping: Nếu người chơi đặt giá sát giờ kết thúc tự động cộng dồn
    // thời gian (Extension) cho phiên đấu giá.
    private void applyAntiSniping(Auction auction) {
        // [LIMIT] Giới hạn tối đa số lần gia hạn
        if (auction.getAntiSnipeCount() >= ANTI_SNIPE_LIMIT) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        long secondsLeft = ChronoUnit.SECONDS.between(now, endTime);

        if (secondsLeft >= 0 && secondsLeft < ANTI_SNIPE_THRESHOLD_SECONDS) {
            LocalDateTime newEndTime = now.plusSeconds(ANTI_SNIPE_EXTENSION_SECONDS); // Đặt lại về 30s từ thời điểm hiện tại

            if (newEndTime.isAfter(endTime)) {
                auction.setEndTime(newEndTime);
                auction.incrementAntiSnipeCount(); // Tăng biến đếm

                System.out.printf(
                        "[Anti-Snipe] Phiên [%s]: Lần %d/%d | Còn %ds < ngưỡng %ds → Đặt lại thời gian về %ds. EndTime mới: %s%n",
                        auction.getId(), auction.getAntiSnipeCount(), ANTI_SNIPE_LIMIT, secondsLeft,
                        ANTI_SNIPE_THRESHOLD_SECONDS, ANTI_SNIPE_EXTENSION_SECONDS, newEndTime);
            }
        }
    }

    public void capNhatTrangThai() throws IOException {
        int updated = 0;
        for (Auction a : auctions) {
            synchronized (a) {
                AuctionStatus truoc = a.getStatus();
                a.updateStatus();
                AuctionStatus sau = a.getStatus();

                if (sau != truoc) {
                    dao.capNhat(a); // Persist trạng thái mới
                    updated++;
                    System.out.printf("[AuctionManager] Phiên [%s]: %s → %s%n", a.getId(), truoc, sau);

                    // Broadcast trạng thái mới tới toàn hệ thống
                    server.payload.AuctionUpdatePayload statusPayload = new server.payload.AuctionUpdatePayload(
                            a.getId(), LocalDateTime.now(), a.getCurrentHighestBid(), "Status Update",
                            a.getHighestBidderId(), "", a.getEndTime(), a.getAntiSnipeCount());
                    server.network.AuctionServer.getInstance().broadcast(
                            new client.message.PacketMessage(client.message.MessageType.AUCTION_UPDATE, statusPayload));

                    // Thông báo cho các Observer (nếu có)
                    for (BidObserver obs : observers) {
                        obs.onAuctionStatusChanged(a);
                    }

                    // Nếu phiên kết thúc, thực hiện thông báo kết quả qua Network
                    if (a.getStatus() == AuctionStatus.FINISHED) {
                        thongBaoKetThucPhien(a);
                    }
                }
            }
        }
        if (updated > 0) {
            System.out.println("[AuctionManager] Đã cập nhật " + updated + " phiên.");
        }
    }

    // Gửi thông báo kết thúc phiên và kết quả (thắng/không ai thắng) tới tất cả các client.
    private void thongBaoKetThucPhien(Auction auction) {
        server.network.AuctionServer serverNet = server.network.AuctionServer.getInstance();

        String winnerId = auction.getHighestBidderId();
        double finalPrice = auction.getCurrentHighestBid();

        client.message.PacketMessage msg;
        if (winnerId != null && !winnerId.isEmpty()) {
            // Có người thắng
            server.payload.AuctionUpdatePayload payload = new server.payload.AuctionUpdatePayload(auction.getId(),
                    auction.getStartTime(), finalPrice, "Auction Finished", winnerId, "Winner determined",
                    auction.getEndTime(), auction.getAntiSnipeCount());
            msg = new client.message.PacketMessage(client.message.MessageType.NOTIFY_AUCTION_WINNER, payload);
            System.out.printf("[AuctionManager] Phiên [%s] KẾT THÚC. Người thắng: %s | Giá: %.2f%n", auction.getId(),
                    winnerId, finalPrice);
        } else {
            // Không có người thắng
            msg = new client.message.PacketMessage(client.message.MessageType.NOTIFY_NO_AUCTION_WINNER,
                    auction.getId());
            System.out.printf("[AuctionManager] Phiên [%s] KẾT THÚC. Không có người đặt giá.%n", auction.getId());
        }

        // Gửi tới tất cả các client (Broadcast) để người thắng nhận được dù đang ở bất kỳ màn hình nào
        serverNet.broadcast(msg);

        // Broadcast thông báo chung để cập nhật danh sách ở Dashboard (nếu chưa được xử lý bởi msg trên)
        serverNet.broadcast(
                new client.message.PacketMessage(client.message.MessageType.AUCTION_CONCLUDED, auction.getId()));
    }

    public boolean ketThucPhien(String auctionId, boolean huy) throws IOException {

        Auction auction = timTheoId(auctionId);
        if (auction == null)
            return false;

        synchronized (auction) {
            AuctionStatus newStatus = huy ? AuctionStatus.CANCELED : AuctionStatus.FINISHED;
            auction.setStatus(newStatus);
            dao.capNhat(auction);

            System.out.printf("[AuctionManager] Phiên [%s] đã chuyển sang %s.%n", auctionId, newStatus);
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

    // Trả về toàn bộ danh sách phiên đấu giá. Trả về bản sao phòng thủ (defensive copy)
    public List<Auction> layTatCa() {
        return Collections.unmodifiableList(new ArrayList<>(auctions));
    }

    // Trả về số lượng phiên đang trong bộ nhớ.
    public int soLuongPhien() {
        return auctions.size();
    }
}

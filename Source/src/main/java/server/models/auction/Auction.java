package server.models.auction;

import server.models.Entity;
import server.models.item.Item;
import server.models.item.ItemCategory;
import server.models.network.AuctionClient;
import server.models.user.Bidder;
import server.auction.*;
import server.network.AuctionServer;
import server.payload.*;
import client.message.PacketMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.List;
import java.util.ArrayList;

import static client.message.MessageType.*;

public class Auction extends Entity {
    private String itemId; // ID của vật phẩm được đưa ra đấu giá
    private String sellerId; // ID của người bán
    private LocalDateTime startTime; // Thời gian bắt đầu phiên
    private LocalDateTime endTime; // Thời gian kết thúc phiên
    private double startingPrice; // Giá khởi điểm
    private double currentHighestBid; // Giá cao nhất hiện tại
    private String highestBidderId; // ID người đang trả giá cao nhất (Leader)
    private AuctionStatus status; // Trạng thái phiên đấu giá
    private int antiSnipeCount = 0; // Số lần đã gia hạn Anti-Sniping
    private List<BidTransaction> bidHistory = new ArrayList<>(); // Lịch sử đặt giá
    private transient List<AuctionClient> clientList = new CopyOnWriteArrayList<>(); // Tạo danh sách các client có
                                                                                     // trong một phiên đấu giá

    // Mức giá tăng tối thiểu mỗi lần bid (Bước giá - Step)
    private double minimumBidIncrement;

    public enum AuctionStatus {
        OPEN, RUNNING, FINISHED, PAID, CANCELED
    }

    // 1. Constructor khởi tạo phiên đấu giá mới
    public Auction(String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice,
            double minimumBidIncrement) {
        super(); // Khởi tạo ID (UUID) từ base class Entity
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.minimumBidIncrement = minimumBidIncrement;
        this.status = AuctionStatus.OPEN;
    }

    // 2. Constructor dùng khi load dữ liệu từ Database
    public Auction(String id, String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime,
            double startingPrice, double currentHighestBid, String highestBidderId, double minimumBidIncrement,
            AuctionStatus status) {
        super(id);
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startingPrice = startingPrice;
        this.currentHighestBid = currentHighestBid;
        this.highestBidderId = highestBidderId;
        this.minimumBidIncrement = minimumBidIncrement;
        this.status = status;
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        // Không đổi trạng thái nếu đã kết thúc, thanh toán hoặc bị huỷ
        if (status == AuctionStatus.CANCELED || status == AuctionStatus.PAID || status == AuctionStatus.FINISHED) {
            return;
        }

        if (now.isBefore(startTime)) {
            setStatus(AuctionStatus.OPEN);
        } else if (!now.isAfter(endTime)) {
            setStatus(AuctionStatus.RUNNING);
        } else {
            setStatus(AuctionStatus.FINISHED);
        }

    }

    public boolean isActive() {
        return this.status == AuctionStatus.RUNNING;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public String getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(String highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public double getMinimumBidIncrement() {
        return minimumBidIncrement;
    }

    public void setMinimumBidIncrement(double minimumBidIncrement) {
        this.minimumBidIncrement = minimumBidIncrement;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public int getAntiSnipeCount() {
        return antiSnipeCount;
    }

    public void setAntiSnipeCount(int antiSnipeCount) {
        this.antiSnipeCount = antiSnipeCount;
    }

    public void incrementAntiSnipeCount() {
        this.antiSnipeCount++;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void addBidToHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }

    public Item getItem() {
        return Item.getCurrentItem();
    }

    public BidTransaction placeBid(Bidder bidder, double bidAmount) {
        // 1. Khởi tạo một giao dịch mới
        BidTransaction transaction = new BidTransaction(this.getId(), bidder.getId(), bidAmount);

        // 2. Tự kiểm tra tính hợp lệ
        try {
            if (transaction.validate(this)) {
                // 3. Nếu hợp lệ, cập nhật ngay các thông số của phiên
                this.currentHighestBid = bidAmount;
                this.highestBidderId = bidder.getId();
            }
        } catch (Exception e) {
            // Nếu có lỗi (ví dụ: giá thấp), transaction.validate đã set status = REJECTED
            System.err.println("[Auction] Đặt giá thất bại: " + e.getMessage());
        }

        // 4. Luôn ghi nhận vào lịch sử (dù thành hay bại)
        this.addBidToHistory(transaction);

        return transaction;
    }

    public List<AuctionClient> getClientList() {
        if (clientList == null)
            clientList = new CopyOnWriteArrayList<>();
        return clientList;
    }

    public void setClientList(List<AuctionClient> clientList) {
        this.clientList = clientList;
    }

    @Override
    public String toString() {
        return "Auction{" + "id='" + getId() + '\'' + ", itemId='" + itemId + '\'' + ", status=" + status
                + ", currentHighestBid=" + currentHighestBid + ", highestBidder='" + highestBidderId + '\'' + '}';
    }

    /**
     * Đăng ký một client tham gia vào phiên đấu giá.
     * Sử dụng Guard Clauses để ném lỗi sớm.
     */
    public void addClient(AuctionClient client)
            throws AuctionAlreadyRegisteredException, AuctionClientIsOwnerException {

        // 1. Kiểm tra xem client có phải chủ sở hữu phiên không
        if (sellerId != null && sellerId.equals(client.getUsername())) {
            throw new AuctionClientIsOwnerException(
                    "Chủ sở hữu của phiên không thể đăng ký vào chính auction của mình");
        }

        // 2. Kiểm tra xem client đã đăng ký trước đó chưa
        if (getClientList().contains(client)) {
            throw new AuctionAlreadyRegisteredException("Client đã được đăng ký rồi");
        }

        // Happy Path: Thực hiện khi tất cả điều kiện hợp lệ (Không lồng IF)
        client.getRegisteredAuctions().addFirst(this.getId());
        clientList.add(client);
    }

    /**
     * Hủy đăng ký client khỏi phiên đấu giá một cách bình thường.
     */
    public void removeClient(AuctionClient client) throws AuctionHighBidException, AuctionNotRegisteredException {

        // 1. Kiểm tra xem client đã đăng ký trong phiên chưa
        if (!getClientList().contains(client)) {
            throw new AuctionNotRegisteredException("Client chưa được đăng ký trong phiên đấu giá");
        }
        // Happy Path: Xóa phiên khỏi danh sách của client và hủy đăng ký
        client.getRegisteredAuctions().remove(this.getId());
        clientList.remove(client);
    }



    // Tìm lượt đặt giá cao nhất (lượt mới nhất = cuối danh sách).
    public BidTransaction findHighestBid() {
        return !bidHistory.isEmpty() ? bidHistory.getLast() : new BidTransaction(null, null, 0);
    }

    // Tìm giá của vật phẩm hiện tại cao nhất.
    public double findHighestItemPrice() {
        return !bidHistory.isEmpty() ? bidHistory.getLast().getBidAmount() : 0.0;
    }

}

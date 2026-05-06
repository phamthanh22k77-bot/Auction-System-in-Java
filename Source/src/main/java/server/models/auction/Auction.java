package server.models.auction;

import server.models.Entity;
import server.models.network.AuctionClient;
import server.auction.*;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class Auction extends Entity {
    private String itemId; // ID của vật phẩm được đưa ra đấu giá
    private String sellerId; // ID của người bán
    private LocalDateTime startTime; // Thời gian bắt đầu phiên
    private LocalDateTime endTime; // Thời gian kết thúc phiên
    private double startingPrice; // Giá khởi điểm
    private double currentHighestBid; // Giá cao nhất hiện tại
    private String highestBidderId; // ID người đang trả giá cao nhất (Leader)
    private AuctionStatus status; // Trạng thái phiên đấu giá
    private LinkedList<AuctionClient> clientList; // Tạo danh sách các client có trong một phiên đấu giá


    // Mức giá tăng tối thiểu mỗi lần bid (Bước giá - Step)
    private double minimumBidIncrement;

    // Theo yêu cầu phân công: OPEN -> RUNNING -> FINISHED -> PAID/CANCELED
    public enum AuctionStatus {
        PENDING,
        OPEN,
        RUNNING,
        FINISHED,
        PAID,
        CANCELED
    }

    // 1. Constructor khởi tạo phiên đấu giá mới
    public Auction(String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime,
            double startingPrice, double minimumBidIncrement) {
        super(); // Khởi tạo ID (UUID) từ base class Entity
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.minimumBidIncrement = minimumBidIncrement;
        this.status = AuctionStatus.PENDING;
    }

    // 2. Constructor dùng khi load dữ liệu từ Database
    public Auction(String id, String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime,
            double startingPrice, double currentHighestBid, String highestBidderId,
            double minimumBidIncrement, AuctionStatus status) {
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
            setStatus(AuctionStatus.PENDING);
        } else if (now.isAfter(startTime) && now.isBefore(endTime)) {
            setStatus(AuctionStatus.RUNNING);
        } else if (now.isAfter(endTime)) {
            setStatus(AuctionStatus.FINISHED);
        }
    }
    public boolean isActive() {
        return this.status == AuctionStatus.RUNNING || this.status == AuctionStatus.OPEN;
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

    public LinkedList<AuctionClient> getClientList() {return clientList;}

    public void setClientList(LinkedList<AuctionClient> clientList) {this.clientList = clientList;}

    @Override
    public String toString() {
        return "Auction{" +
                "id='" + getId() + '\'' +
                ", itemId='" + itemId + '\'' +
                ", status=" + status +
                ", currentHighestBid=" + currentHighestBid +
                ", highestBidder='" + highestBidderId + '\'' +
                '}';
    }
    /*
        Điều kiện trước: Phương thức yêu cầu nhận một đối tượng Client chưa được đăng ký trong auction
        Điều kiện sau: Phương thức thêm đối tượng Client nhận được vào biến clientList. Đối tượng Client sẽ có
        danh sách các auction đã đăng ký được cập nhật để bao gồm ID của auction này.
        Điều này chỉ xảy ra nếu Client chưa được đăng ký và không phải là chủ sở hữu.
        Phương thức không trả về giá trị.
        LƯU Ý:
        Nếu client là chủ sở hữu của auction thì ném ra AuctionClientIsOwnerException.
        Nếu client đã được đăng ký trước đó thì ném ra AuctionAlreadyRegisteredException.
 */

    public void addClient(AuctionClient client)
            throws AuctionAlreadyRegisteredException, AuctionClientIsOwnerException {

        // Kiểm tra xem client có phải là chủ sở hữu của auction hay không
        if (!sellerId.equals(client.getSocket().getInetAddress().getHostAddress())) {

            // Kiểm tra xem client đã đăng ký trong auction chưa
            if (!clientList.contains(client)) {

                // Thêm auction này vào danh sách các auction mà client đã đăng ký
                client.getRegisteredAuctions().addFirst(Integer.valueOf(sellerId));

                // Đăng ký client như một người tham gia auction
                clientList.add(client);

            } else {
                throw new AuctionAlreadyRegisteredException("Client đã được đăng ký rồi");
            }

        } else {
            throw new AuctionClientIsOwnerException("Chủ sở hữu của phiên" +
                    " không thể đăng ký vào chính auction của mình");
        }
    }
}

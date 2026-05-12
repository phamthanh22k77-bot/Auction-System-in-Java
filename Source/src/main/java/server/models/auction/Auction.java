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
    private List<BidTransaction> bidHistory = new ArrayList<>(); // Lịch sử đặt giá
    private transient List<AuctionClient> clientList = new CopyOnWriteArrayList<>(); // Tạo danh sách các client có trong một
                                                                           // phiên đấu giá
    // Mức giá tăng tối thiểu mỗi lần bid (Bước giá - Step)
    private double minimumBidIncrement;

    // Theo yêu cầu phân công: OPEN -> RUNNING -> FINISHED -> PAID/CANCELED
    public enum AuctionStatus {
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
        this.status = AuctionStatus.OPEN;
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
            setStatus(AuctionStatus.OPEN);
        } else if (now.isAfter(startTime) && now.isBefore(endTime)) {
            setStatus(AuctionStatus.RUNNING);
        } else if (now.isAfter(endTime)) {
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

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void addBidToHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }

    public Item getItem() {return Item.getCurrentItem();}

    /**
     *
     * @param bidder    Người đặt giá
     * @param bidAmount Số tiền đặt
     * @return BidTransaction chứa kết quả (ACCEPTED hoặc REJECTED)
     */
    public BidTransaction placeBid(Bidder bidder, double bidAmount) {
        // 1. Khởi tạo một giao dịch mới
        BidTransaction transaction = new BidTransaction(this.getId(), bidder.getId(), bidAmount);

        // 2. Tự kiểm tra tính hợp lệ
        if (transaction.validate(this)) {
            // 3. Nếu hợp lệ, cập nhật ngay các thông số của phiên
            this.currentHighestBid = bidAmount;
            this.highestBidderId = bidder.getId();
        }

        // 4. Luôn ghi nhận vào lịch sử (dù thành hay bại)
        this.addBidToHistory(transaction);

        return transaction;
    }

    public List<AuctionClient> getClientList() {
        return clientList;
    }

    public void setClientList(List<AuctionClient> clientList) {
        this.clientList = clientList;
    }

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
     * Điều kiện trước: Phương thức yêu cầu nhận một đối tượng Client chưa được đăng
     * ký trong auction
     * Điều kiện sau: Phương thức thêm đối tượng Client nhận được vào biến
     * clientList. Đối tượng Client sẽ có
     * danh sách các auction đã đăng ký được cập nhật để bao gồm ID của auction này.
     * Điều này chỉ xảy ra nếu Client chưa được đăng ký và không phải là chủ sở hữu.
     * Phương thức không trả về giá trị.
     * LƯU Ý:
     * Nếu client là chủ sở hữu của auction thì ném ra
     * AuctionClientIsOwnerException.
     * Nếu client đã được đăng ký trước đó thì ném ra
     * AuctionAlreadyRegisteredException.
     */

    public void addClient(AuctionClient client)
            throws AuctionAlreadyRegisteredException, AuctionClientIsOwnerException {

        // Kiểm tra xem client có phải là chủ sở hữu của auction hay không
        if (!sellerId.equals(client.getSocket().getInetAddress().getHostAddress())) {

            // Kiểm tra xem client đã đăng ký trong auction chưa
            if (!clientList.contains(client)) {

                // Thêm ID của phiên đấu giá này vào danh sách đã đăng ký của client
                client.getRegisteredAuctions().addFirst(this.getId());

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

    /*
    Điều kiện trước: Phương thức yêu cầu nhận một đối tượng Client đã được đăng ký trong phiên đấu giá.
    Điều kiện sau: Phương thức xóa đối tượng Client được cung cấp khỏi danh sách các client đã đăng ký trong biến clientList.
    Đồng thời, phiên đấu giá này cũng sẽ được xóa khỏi danh sách các phiên đấu giá đã đăng ký của client.
    Những thao tác này chỉ được thực hiện nếu Client đã được đăng ký và không sở hữu giá thầu cao nhất trong phiên đấu giá.
    Phương thức không trả về giá trị nào.
    LƯU Ý:
    Nếu Client được cung cấp đang sở hữu giá thầu cao nhất thì sẽ ném ra ngoại lệ AuctionHighBidException.
    Nếu Client được cung cấp chưa được đăng ký trong phiên đấu giá thì sẽ ném ra ngoại lệ AuctionNotRegisteredException.
*/
    public void removeClient(AuctionClient client)
            throws AuctionHighBidException, AuctionNotRegisteredException {

        // Kiểm tra xem client đã được đăng ký trong phiên đấu giá chưa
        if (clientList.contains(client)) {

            // Kiểm tra xem client có giá thầu cao nhất hay không
            if (!bidHistory.isEmpty() && bidHistory.getFirst().getBidderId()
                    .equals(client.getSocketAddress().getAddress().getHostAddress())) {
                throw new AuctionHighBidException("Người dùng đang sở hữu giá thầu cao nhất");
            }

            // Xóa phiên đấu giá này khỏi danh sách các phiên đấu giá đã đăng ký của client
            int auctionIndex = client.getRegisteredAuctions().indexOf(this.getId());
            if (auctionIndex != -1) {
                client.getRegisteredAuctions().remove(auctionIndex);
            }

            // Hủy đăng ký client khỏi phiên đấu giá
            clientList.remove(client);
        } else {
            throw new AuctionNotRegisteredException("Client chưa được đăng ký trong phiên đấu giá");
        }
    }
    /*
    Điều kiện trước: Phương thức này yêu cầu nhận một đối tượng Client đã được đăng ký trong phiên đấu giá.
    Điều kiện sau: Phương thức sẽ xóa đối tượng Client được cung cấp khỏi biến clientList của phiên đấu giá.
    Việc này được thực hiện bất kể client có sở hữu giá thầu lớn nhất hay không. Phương thức sẽ xử lý cập nhật
    giá thầu cao nhất bằng giá trị lớn tiếp theo nếu giá thầu cao nhất hiện tại thuộc về client bị xóa.
    Trong trường hợp này, tất cả người tham gia sẽ được gửi một packet message để cập nhật về chủ sở hữu
    giá thầu cao nhất mới.
    Phương thức không trả về giá trị nào.
    LƯU Ý:
    Nếu client chưa được đăng ký trong phiên đấu giá thì sẽ ném ra ngoại lệ AuctionNotRegisteredException.
*/

    public void forcefullyRemoveClient(AuctionClient client) throws AuctionNotRegisteredException {

        AuctionServer server = AuctionServer.getInstance();

        // Kiểm tra xem client đã được đăng ký trong phiên đấu giá chưa
        if (clientList.contains(client)) {

            // Kiểm tra xem client có giá thầu cao nhất hay không
            if (bidHistory.getFirst().getBidderId().equals
                    (client.getSocket().getInetAddress().getHostAddress()) && !bidHistory.isEmpty()) {
                bidHistory.remove(0);

                double highestBid = Item.getCurrentItem().getStartingPrice();

                if (!bidHistory.isEmpty()) {
                    highestBid = bidHistory.getFirst().getBidAmount();
                }

                // Cập nhật cho những người tham gia về giá thầu cao nhất và chủ sở hữu mới
                AuctionUpdatePayload auctionUpdate = new AuctionUpdatePayload(this.getId(), getStartTime(), highestBid,
                        Item.getCurrentItem().getName(), client.getSocketAddress().getAddress().getHostAddress(),
                        Item.getCurrentItem().getDescription());
                server.sendPackets(clientList, new PacketMessage(HIGHEST_BID_OWNER_LOST, auctionUpdate));
            }

            // Hủy đăng ký client khỏi phiên đấu giá
            clientList.remove(client);

        } else {
            throw new AuctionNotRegisteredException("Chưa được đăng ký trong phiên đấu giá");
        }
    }
    /*
    Điều kiện trước: Phương thức yêu cầu nhận một đối tượng Client đã được đăng ký trong phiên đấu giá
    và một đối tượng Bid đại diện cho giá thầu được thực hiện bởi đối tượng Client đó.

    Điều kiện sau: Phương thức thêm đối tượng Bid được cung cấp như một giá thầu hợp lệ
    vào biến bidList. Đồng thời, biến numberOfHighBids của Client được cung cấp
    cũng sẽ được tăng thêm 1 nếu thao tác thành công.

    Những thao tác này chỉ có thể xảy ra nếu đối tượng Bid có giá trị lớn hơn
    giá thầu cao nhất hiện tại (hoặc giá khởi điểm) và đối tượng Client đã được đăng ký
    trong phiên đấu giá và không phải là chủ sở hữu của phiên đấu giá.

    Phương thức không trả về giá trị nào.

    LƯU Ý:
    Nếu Client được cung cấp chưa được đăng ký thì sẽ ném ra ngoại lệ AuctionNotRegisteredException.

    Nếu Bid được cung cấp thấp hơn giá thầu hiện tại thì sẽ ném ra ngoại lệ AuctionLowBidException.

    Nếu Client được cung cấp là chủ sở hữu của phiên đấu giá thì sẽ ném ra ngoại lệ
    AuctionClientIsOwnerException.
*/

    public BidTransaction findHighestBid() {
        //Check if there are any bids in the auction
        if (!bidHistory.isEmpty()) {
            return this.getBidHistory().getFirst();
        } else {
            return new BidTransaction(null, null, 0);
        }
    }
    /*
    Điều kiện trước: Không có.

    Điều kiện sau:
    - Phương thức trả về một giá trị double.
    - Nếu danh sách bidHistory có phần tử, giá trị trả về là mức giá đấu cao nhất hiện tại.
    - Nếu bidHistory rỗng, giá trị trả về là 0.
*/
    public double findHighestItemPrice() {

        double highestBid = 0;

        if (!bidHistory.isEmpty()) {
            highestBid = bidHistory.getFirst().getBidAmount();
        }

        return highestBid;
    }
}

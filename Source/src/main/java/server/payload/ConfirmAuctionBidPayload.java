package server.payload;

import java.util.Objects;

//Class được dùng để đóng gói thông tin khi nâng giá thành công trong một phiên đấu giá
public class ConfirmAuctionBidPayload {

    //Attributes
    private int auctionID;
    // 👉 Controller (client) dùng để xác định bid thuộc auction nào
    // 👉 Thường dùng khi server xác nhận bid đã được chấp nhận

    //Constructors
    public ConfirmAuctionBidPayload(int auctionID) {
        this.auctionID = auctionID;
    }

    //Setters and Getters
    public int getAuctionID() {
        return auctionID;
        // 👉 Controller dùng để cập nhật UI của auction tương ứng (bid thành công)
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
        // 👉 Controller có thể cập nhật lại trước khi gửi nếu cần
    }

    //Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfirmAuctionBidPayload)) return false;
        ConfirmAuctionBidPayload that = (ConfirmAuctionBidPayload) o;
        return Objects.equals(getAuctionID(), that.getAuctionID());
        // 👉 So sánh 2 payload dựa trên auctionID (dùng trong backend)
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuctionID());
        // 👉 Dùng khi object nằm trong HashMap/HashSet
    }

    @Override
    public String toString() {
        // 👉 Controller có thể log payload này khi nhận xác nhận bid thành công
        return "ConfirmCancelingAuctionPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
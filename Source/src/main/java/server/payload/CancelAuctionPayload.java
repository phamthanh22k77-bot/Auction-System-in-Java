package server.payload;

import java.io.Serializable;
import java.util.Objects;

//Class này dùng để đóng gói dữ liệu khi phiên đấu giá bị dừng
public class CancelAuctionPayload implements Serializable {

    //Attributes
    private int auctionID;
    // 👉 Controller (client) sẽ set giá trị này khi user chọn huỷ auction
    // 👉 Server sẽ dùng giá trị này để xác định auction cần bị cancel

    //Constructors
    public CancelAuctionPayload(int auctionID){
        this.auctionID = auctionID;
    }

    //Setters and Getters
    public int getAuctionID() {
        return auctionID;
        // 👉 Server-side sẽ gọi để xử lý: cancelAuction(auctionID)
        // 👉 Controller (server) có thể dùng để log hoặc kiểm tra request
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
        // 👉 Controller có thể cập nhật lại trước khi gửi request nếu cần
    }

    //Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancelAuctionPayload)) return false;
        CancelAuctionPayload that = (CancelAuctionPayload) o;
        return Objects.equals(auctionID, that.auctionID);
        // 👉 So sánh 2 payload dựa trên auctionID (dùng trong logic backend)
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
        // 👉 Dùng khi object nằm trong HashMap/HashSet
    }

    @Override
    public String toString() {
        // 👉 Controller có thể log payload này khi gửi/nhận để debug
        return "CancelAuctionPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
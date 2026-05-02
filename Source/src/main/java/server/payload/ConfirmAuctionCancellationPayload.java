package server.payload;

import java.io.Serializable;
import java.util.Objects;

//Class dùng để đóng gói dữ liệu khi xác định hủy một phiên đấu giá
public class ConfirmAuctionCancellationPayload implements Serializable {

    //Attributes
    private int auctionID;
    // 👉 Controller dùng để xác định auction nào đã được huỷ thành công
    // 👉 Dùng để cập nhật UI (xóa khỏi list / đổi trạng thái)

    //Constructors
    public ConfirmAuctionCancellationPayload(int auctionID) {
        this.auctionID = auctionID;
    }

    //Setters and Getters
    public int getAuctionID() {
        return auctionID;
        // 👉 Controller gọi để tìm auction tương ứng và cập nhật giao diện
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
        // 👉 Controller có thể set trước khi gửi nếu cần
    }

    //Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfirmAuctionCancellationPayload)) return false;
        ConfirmAuctionCancellationPayload that = (ConfirmAuctionCancellationPayload) o;
        return Objects.equals(getAuctionID(), that.getAuctionID());
        // 👉 So sánh 2 payload dựa trên auctionID
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuctionID());
        // 👉 Dùng trong HashMap/HashSet
    }

    @Override
    public String toString() {
        // 👉 Controller có thể log khi nhận xác nhận huỷ auction
        return "ConfirmCancelingAuctionPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
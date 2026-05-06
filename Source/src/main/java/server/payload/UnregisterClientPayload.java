package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để client rời khỏi (unregister) một auction
// 👉 Client gửi payload này lên server khi:
//    - User thoát khỏi màn hình auction
//    - Hoặc không muốn theo dõi auction nữa
// 👉 Server nhận để:
//    - Xóa client khỏi danh sách subscriber
//    - Ngừng gửi update cho client đó
public class UnregisterClientPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction mà client muốn rời khỏi
    // 👉 Dùng để xác định đúng auction cần unregister
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID
    public UnregisterClientPayload(int auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Getter & Setter =====

    // Lấy auctionID
    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (!(o instanceof UnregisterClientPayload)) return false;

        UnregisterClientPayload that = (UnregisterClientPayload) o;

        // So sánh theo auctionID
        return Objects.equals(getAuctionID(), that.getAuctionID());
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID
        return Objects.hash(getAuctionID());
    }

    @Override
    public String toString() {
        // ⚠️ Tên trong string khác tên class (ClientLeavesAuctionPayload)
        // 👉 Chỉ là label hiển thị, không ảnh hưởng logic
        return "ClientLeavesAuctionPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
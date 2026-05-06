package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi auctionID giữa client ↔ server
// 👉 Thường dùng khi:
//    - Client chọn 1 auction và gửi ID lên server
//    - Server cần xác định auction để xử lý (join, view, cancel,...)
public class SendAuctionIDPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction
    // 👉 Là thông tin duy nhất để xác định auction trong hệ thống
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID
    public SendAuctionIDPayload(int auctionID) {
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
        if (o == null || getClass() != o.getClass()) return false;

        SendAuctionIDPayload that = (SendAuctionIDPayload) o;

        // So sánh theo auctionID
        return auctionID == that.auctionID;
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in ID auction được gửi đi
        return "SendAuctionIDPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi tín hiệu countdown giai đoạn 2 của auction
// 👉 Server gửi payload này khi chuyển sang bước đếm ngược tiếp theo
// 👉 Client nhận để tiếp tục cập nhật UI countdown (ví dụ: 3...2...1...)
public class SecondCountdownPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction đang countdown
    // 👉 Dùng để xác định đúng auction trên UI
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID
    public SecondCountdownPayload(int auctionID) {
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
    public String toString() {
        // ⚠️ Tên class trong string đang sai (FirstCountdownPayload)
        // 👉 Nên sửa lại thành SecondCountdownPayload
        return "FirstCountdownPayload{" +
                "auctionID=" + auctionID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        SecondCountdownPayload that = (SecondCountdownPayload) o;

        // So sánh theo auctionID
        return auctionID == that.auctionID;
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID
        return Objects.hash(auctionID);
    }
}
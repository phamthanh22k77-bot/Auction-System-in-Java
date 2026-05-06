package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để yêu cầu lấy giá bid cao nhất từ client → server
// 👉 Client gửi payload này khi cần cập nhật giá mới nhất của auction
// 👉 Server nhận và trả về highestBid tương ứng
public class RequestHighestBidPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction cần lấy highest bid
    // 👉 Server dùng để tìm đúng auction trong hệ thống
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID
    public RequestHighestBidPayload(int auctionID) {
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
        if (!(o instanceof RequestHighestBidPayload)) return false;

        RequestHighestBidPayload that = (RequestHighestBidPayload) o;

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
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in yêu cầu lấy highest bid
        return "RequestHighestBid{" +
                "auctionID=" + auctionID +
                '}';
    }
}
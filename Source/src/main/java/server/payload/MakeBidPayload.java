package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi hành động đặt giá (bid) từ client → server
// 👉 Client tạo object này khi user nhập giá và bấm "Bid"
// 👉 Server nhận để xử lý logic đấu giá (kiểm tra hợp lệ, cập nhật highest bid,...)
public class MakeBidPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction mà user đang bid
    // 👉 Server dùng để xác định đúng phiên đấu giá
    private int auctionID;

    // Giá mà user muốn đặt (bid mới)
    // 👉 Phải lớn hơn highestBid hiện tại (server sẽ validate)
    private float highestBid;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID và giá bid
    public MakeBidPayload(int auctionID, float highestBid) {
        this.auctionID = auctionID;
        this.highestBid = highestBid;
    }

    // ===== Getter & Setter =====

    // Lấy auctionID
    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    // Lấy giá bid user gửi lên
    public float getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        MakeBidPayload that = (MakeBidPayload) o;

        // So sánh cả auctionID và giá bid
        return auctionID == that.auctionID &&
                Float.compare(that.highestBid, highestBid) == 0;
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID + giá bid
        return Objects.hash(auctionID, highestBid);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in ra thông tin bid được gửi lên server
        return "BidCreatePayload{" +
                "auctionID=" + auctionID +
                ", bidPrice=" + highestBid +
                '}';
    }
}
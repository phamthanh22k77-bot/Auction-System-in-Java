package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi thông tin bắt đầu countdown của auction
// 👉 Server gửi payload này khi auction chuẩn bị bắt đầu (countdown phase)
// 👉 Client nhận để hiển thị:
//    - Tên item
//    - Giá cao nhất hiện tại
//    - Bắt đầu đếm ngược
public class FirstCountdownPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction
    // 👉 Dùng để xác định auction nào đang countdown
    private int auctionID;

    // Tên item đang được đấu giá
    // 👉 Hiển thị trên UI cho user
    private String itemName;

    // Giá bid cao nhất hiện tại
    // 👉 Giúp user biết mức giá đang dẫn đầu trước khi đấu
    private float highestBid;

    // ===== Constructor =====

    // Khởi tạo payload với thông tin countdown
    public FirstCountdownPayload(int auctionID, String itemName, float highestBid) {
        this.auctionID = auctionID;
        this.itemName = itemName;
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

    // Lấy tên item
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // Lấy giá cao nhất hiện tại
    public float getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    // ===== Methods =====

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in ra auction đang countdown
        return "FirstCountdownPayload{" +
                "auctionID=" + auctionID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        FirstCountdownPayload that = (FirstCountdownPayload) o;

        // So sánh dựa trên auctionID (coi mỗi auction là duy nhất)
        return auctionID == that.auctionID;
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID
        return Objects.hash(auctionID);
    }
}
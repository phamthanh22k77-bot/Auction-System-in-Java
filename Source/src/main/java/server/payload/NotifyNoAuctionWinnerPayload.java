package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để thông báo auction kết thúc nhưng KHÔNG có người thắng
// 👉 Server gửi payload này khi:
//    - Không có ai bid
//    - Hoặc không ai đạt điều kiện thắng
// 👉 Client nhận để hiển thị thông báo cho user
public class NotifyNoAuctionWinnerPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction đã kết thúc
    // 👉 Dùng để xác định auction nào không có winner
    private int auctionID;

    // Tên item của auction
    // 👉 Hiển thị cho user biết item nào không bán được
    private String itemName;

    // Giá khởi điểm ban đầu
    // 👉 Có thể hiển thị lại để user tham khảo
    private float itemStartingPrice;

    // ===== Constructor =====

    // Khởi tạo payload với thông tin auction không có winner
    public NotifyNoAuctionWinnerPayload(int auctionID, String itemName, float itemStartingPrice) {
        this.auctionID = auctionID;
        this.itemName = itemName;
        this.itemStartingPrice = itemStartingPrice;
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

    // Lấy giá khởi điểm
    public float getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(float itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        NotifyNoAuctionWinnerPayload that = (NotifyNoAuctionWinnerPayload) o;

        // So sánh toàn bộ thông tin auction
        return auctionID == that.auctionID &&
                Float.compare(that.itemStartingPrice, itemStartingPrice) == 0 &&
                Objects.equals(itemName, that.itemName);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên các field
        return Objects.hash(auctionID, itemName, itemStartingPrice);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in thông tin auction không có người thắng
        return "NoAuctionWinnerPayload{" +
                "auctionID=" + auctionID +
                ", itemName='" + itemName + '\'' +
                ", itemStartingPrice=" + itemStartingPrice +
                '}';
    }
}
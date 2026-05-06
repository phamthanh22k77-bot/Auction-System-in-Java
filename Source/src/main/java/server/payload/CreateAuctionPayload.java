package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi thông tin tạo auction từ client → server
// 👉 Client tạo object này khi user nhập form "Create Auction"
// 👉 Server nhận payload này để tạo auction mới trong hệ thống
public class CreateAuctionPayload implements Serializable {

    // ===== Attributes =====

    // Loại auction (ví dụ: NORMAL, DUTCH, SEALED,...)
    // 👉 Server có thể dùng để quyết định logic đấu giá tương ứng
    private String auctionType;

    // Giá khởi điểm của item
    // 👉 Đây là giá bắt đầu cho các bid
    private float itemStartingPrice;

    // Tên item được đấu giá
    // 👉 Hiển thị trong danh sách auction
    private String itemName;

    // Mô tả chi tiết item
    // 👉 Giúp người dùng hiểu rõ hơn về sản phẩm
    private String itemDescription;

    // Thời gian diễn ra auction (đơn vị tùy hệ thống: phút / giây)
    // 👉 Server sẽ dùng để tính thời điểm kết thúc auction
    private int auctionDuration;

    // ===== Constructor =====

    // Khởi tạo payload với đầy đủ thông tin để tạo auction
    public CreateAuctionPayload(String auctionType, float itemStartingPrice, String itemName,
                                String itemDescription, int auctionDuration) {
        this.auctionType = auctionType;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionDuration = auctionDuration;
    }

    // ===== Getter & Setter =====

    // Controller/Server dùng để lấy loại auction
    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    // Lấy giá khởi điểm
    public float getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(float itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    // Lấy tên item
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // Lấy mô tả item
    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    // Lấy thời gian auction
    // 👉 Lưu ý: method trả về long nhưng field là int (có thể gây không nhất quán)
    public long getAuctionDuration() {
        return auctionDuration;
    }

    public void setAuctionDuration(int auctionDuration) {
        this.auctionDuration = auctionDuration;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        CreateAuctionPayload that = (CreateAuctionPayload) o;

        // So sánh toàn bộ thông tin của auction
        return Float.compare(that.itemStartingPrice, itemStartingPrice) == 0 &&
                Objects.equals(auctionType, that.auctionType) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(itemDescription, that.itemDescription) &&
                Objects.equals(auctionDuration, that.auctionDuration);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên toàn bộ field
        return Objects.hash(auctionType, itemStartingPrice, itemName, itemDescription, auctionDuration);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in toàn bộ thông tin auction chuẩn bị tạo
        return "AuctionCreatePayload{" +
                "type='" + auctionType + '\'' +
                ", startingPrice=" + itemStartingPrice +
                ", itemName='" + itemName + '\'' +
                ", description='" + itemDescription + '\'' +
                ", endAt=" + auctionDuration +
                '}';
    }
}
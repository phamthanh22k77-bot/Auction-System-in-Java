package server.payload;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import server.models.item.ItemCategory;

// Payload dùng để gửi thông tin tạo auction từ client → server
// 👉 Client tạo object này khi user nhập form "Create Auction"
// 👉 Server nhận payload này để tạo auction mới trong hệ thống
public class CreateAuctionPayload implements Serializable {

    // ===== Attributes =====

    // Loại auction (ví dụ: NORMAL, DUTCH, SEALED,...)
    // 👉 Server có thể dùng để quyết định logic đấu giá tương ứng
    private String auctionType;

    // Loại item trong auction (Arts, Elec, Vehi)
    // 👉 Đầu ra cần thiết do có sự khác biệt rõ ràng giữa các item được đấu giá
    private ItemCategory itemCategory;

    // Giá khởi điểm của item
    // 👉 Đây là giá bắt đầu cho các bid
    private double itemStartingPrice;

    // Tên item được đấu giá
    // 👉 Hiển thị trong danh sách auction
    private String itemName;

    // Mô tả chi tiết item
    // 👉 Giúp người dùng hiểu rõ hơn về sản phẩm
    private String itemDescription;

    // Thời gian diễn ra auction (đơn vị tùy hệ thống: phút / giây)
    // 👉 Server sẽ dùng để tính thời điểm kết thúc auction
    private int auctionDuration;

    // Thời gian bắt đầu auction (Dùng LocalDateTime)
    // 👉 Cộng với Duration để lấy endTime
    private LocalDateTime startTime;

    // Giá thầu nhỏ nhất
    // 👉 Emoji chỉ tay sang bên phải
    private double minimumBidIncrement;


    // Các variable riêng biệt của từng loại item trong auction
    // 👉 Hiển thị riêng biệt cho từng loại item
    // ===== ELECTRONICS =====
    private String brand;
    private String model;
    private int warranty;

    // ===== ART =====
    private String artist;
    private String medium;
    private int year;

    // ===== VEHICLE =====
    private String engineType;
    private int modelYear;
    private double mileage;
    private String licensePlate;


    public CreateAuctionPayload(
            String auctionType,
            double itemStartingPrice,
            String itemName,
            String itemDescription,
            int auctionDuration,
            ItemCategory itemCategory,

            // ELECTRONICS
            String brand,
            String model,
            int warranty,

            // ART
            String artist,
            String medium,
            int year,

            // VEHICLE
            String engineType,
            int modelYear,
            double mileage,
            String licensePlate
    ) {

        this.auctionType = auctionType;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionDuration = auctionDuration;
        this.itemCategory = itemCategory;
        this.minimumBidIncrement = minimumBidIncrement;

        // ELECTRONICS
        this.brand = brand;
        this.model = model;
        this.warranty = warranty;

        // ART
        this.artist = artist;
        this.medium = medium;
        this.year = year;

        // VEHICLE
        this.engineType = engineType;
        this.modelYear = modelYear;
        this.mileage = mileage;
        this.licensePlate = licensePlate;
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
    public double getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(double itemStartingPrice) {
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
    public int getAuctionDuration() {
        return auctionDuration;
    }

    public void setAuctionDuration(int auctionDuration) {
        this.auctionDuration = auctionDuration;
    }

    // Lấy loại item đang được đấu giá
    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    // Lấy thời điểm bắt đầu
    public LocalDateTime getStartTime() {
        return startTime;
    }

    // Lấy giá thầu nhỏ nhất
    public double getMinimumBidIncrement(){
        return minimumBidIncrement;
    }

// ===== ELECTRONICS =====

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getWarranty() {
        return warranty;
    }

// ===== ART =====

    public String getArtist() {
        return artist;
    }

    public String getMedium() {
        return medium;
    }

    public int getYear() {
        return year;
    }

// ===== VEHICLE =====

    public String getEngineType() {
        return engineType;
    }

    public int getModelYear() {
        return modelYear;
    }

    public double getMileage() {
        return mileage;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

// ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreateAuctionPayload that = (CreateAuctionPayload) o;

        return Double.compare(that.itemStartingPrice, itemStartingPrice) == 0 &&
                auctionDuration == that.auctionDuration &&
                warranty == that.warranty &&
                year == that.year &&
                modelYear == that.modelYear &&
                Double.compare(that.mileage, mileage) == 0 &&
                Objects.equals(auctionType, that.auctionType) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(itemDescription, that.itemDescription) &&
                Double.compare(that.minimumBidIncrement, minimumBidIncrement) == 0 &&
                itemCategory == that.itemCategory &&
                Objects.equals(brand, that.brand) &&
                Objects.equals(model, that.model) &&
                Objects.equals(artist, that.artist) &&
                Objects.equals(medium, that.medium) &&
                Objects.equals(engineType, that.engineType) &&
                Objects.equals(licensePlate, that.licensePlate) &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                auctionType,
                itemStartingPrice,
                itemName,
                itemDescription,
                auctionDuration,
                itemCategory,
                startTime,
                minimumBidIncrement,

                brand,
                model,
                warranty,

                artist,
                medium,
                year,

                engineType,
                modelYear,
                mileage,
                licensePlate
        );
    }

    @Override
    public String toString() {
        return "AuctionCreatePayload{" +
                "type='" + auctionType + '\'' +
                ", startingPrice=" + itemStartingPrice +
                ", itemCategory=" + itemCategory +
                ", itemName='" + itemName + '\'' +
                ", description='" + itemDescription + '\'' +
                ", startAt=" + startTime + '\'' +
                ", endIn=" + auctionDuration +

                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", warranty=" + warranty +

                ", artist='" + artist + '\'' +
                ", medium='" + medium + '\'' +
                ", year=" + year +

                ", engineType='" + engineType + '\'' +
                ", modelYear=" + modelYear +
                ", mileage=" + mileage +
                ", licensePlate='" + licensePlate + '\'' +

                '}';
    }
}
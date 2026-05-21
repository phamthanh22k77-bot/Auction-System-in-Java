package server.payload;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import server.models.item.ItemCategory;

// Payload dùng để gửi thông tin tạo auction từ client → server
public class CreateAuctionPayload implements Serializable {

    private String auctionType;
    private ItemCategory itemCategory;
    private double itemStartingPrice;
    private String itemName;
    private String itemDescription;
    private int auctionDuration;
    private LocalDateTime startTime;
    private double minimumBidIncrement;

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

    public CreateAuctionPayload(String auctionType, double itemStartingPrice, String itemName, String itemDescription,
            int auctionDuration, ItemCategory itemCategory,

            // ELECTRONICS
            String brand, String model, int warranty,

            // ART
            String artist, String medium, int year,

            // VEHICLE
            String engineType, int modelYear, double mileage, String licensePlate) {

        this.auctionType = auctionType;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionDuration = auctionDuration;
        this.itemCategory = itemCategory;
        this.minimumBidIncrement = 0.0;

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


    public String getAuctionType() {
        return auctionType;
    }
    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public double getItemStartingPrice() {
        return itemStartingPrice;
    }
    public void setItemStartingPrice(double itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public int getAuctionDuration() {
        return auctionDuration;
    }
    public void setAuctionDuration(int auctionDuration) {
        this.auctionDuration = auctionDuration;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public double getMinimumBidIncrement() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CreateAuctionPayload that = (CreateAuctionPayload) o;

        return Double.compare(that.itemStartingPrice, itemStartingPrice) == 0 && auctionDuration == that.auctionDuration
                && warranty == that.warranty && year == that.year && modelYear == that.modelYear
                && Double.compare(that.mileage, mileage) == 0 && Objects.equals(auctionType, that.auctionType)
                && Objects.equals(itemName, that.itemName) && Objects.equals(itemDescription, that.itemDescription)
                && Double.compare(that.minimumBidIncrement, minimumBidIncrement) == 0
                && itemCategory == that.itemCategory && Objects.equals(brand, that.brand)
                && Objects.equals(model, that.model) && Objects.equals(artist, that.artist)
                && Objects.equals(medium, that.medium) && Objects.equals(engineType, that.engineType)
                && Objects.equals(licensePlate, that.licensePlate) && Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionType, itemStartingPrice, itemName, itemDescription, auctionDuration, itemCategory,
                startTime, minimumBidIncrement,
                brand, model, warranty,
                artist, medium, year,
                engineType, modelYear, mileage, licensePlate);
    }

    @Override
    public String toString() {
        return "AuctionCreatePayload{" + "type='" + auctionType + '\'' + ", startingPrice=" + itemStartingPrice
                + ", itemCategory=" + itemCategory + ", itemName='" + itemName + '\'' + ", description='"
                + itemDescription + '\'' + ", startAt=" + startTime + '\'' + ", endIn=" + auctionDuration +
                ", brand='" + brand + '\'' + ", model='" + model + '\'' + ", warranty=" + warranty +
                ", artist='" + artist + '\'' + ", medium='" + medium + '\'' + ", year=" + year +
                ", engineType='" + engineType + '\'' + ", modelYear=" + modelYear + ", mileage=" + mileage
                + ", licensePlate='" + licensePlate + '\'' +
                '}';
    }
}
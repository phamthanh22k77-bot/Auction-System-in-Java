package server.models.item;

import server.models.Entity;
import java.time.LocalDateTime;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sellerId;
    private String status; // PENDING, ACTIVE, CLOSED, SOLD

    // 1. Constructor cho việc tạo mới vật phẩm (ID tự sinh)
    public Item(String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.status = "PENDING";
    }

    // 2. Constructor cho việc nạp dữ liệu từ Database/Network (ID có sẵn)
    public Item(String id, String name, String description, double startingPrice, double currentPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId, String status) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.status = status;
    }

    public abstract ItemCategory getCategory();

    public abstract void printInfo();

    // Kiểm tra và cập nhật giá khi có người đặt giá mới.
    public boolean placeBid(double amount, double minIncrease) {
        if (isAuctionActive() && amount >= currentPrice + minIncrease) {
            this.currentPrice = amount;
            return true;
        }
        return false;
    }

    // Kiểm tra xem phiên đấu giá của vật phẩm này có đang diễn ra không.
    public boolean isAuctionActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(status) && now.isAfter(startTime) && now.isBefore(endTime);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (Current: $" + currentPrice + ")";
    }
}

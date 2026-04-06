package model.item;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private String model;
    private int warranty; // Thời gian bảo hành (tháng)

    // 1. Constructor cho việc tạo mới vật phẩm
    public Electronics(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String brand, String model, int warranty) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.brand = brand;
        this.model = model;
        this.warranty = warranty;
    }

    // 2. Constructor cho việc nạp dữ liệu từ Database
    public Electronics(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String brand, String model, int warranty) {
        super(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status);
        this.brand = brand;
        this.model = model;
        this.warranty = warranty;
    }

    @Override
    public String getCategory() {
        return "ELECTRONICS";
    }

    @Override
    public void printInfo() {
        System.out.println("=== ELECTRONICS DETAILS ===");
        System.out.println("Name: " + getName());
        System.out.println("Brand: " + brand + " | Model: " + model);
        System.out.println("Warranty: " + warranty + " months");
        System.out.println("Current Bid: $" + getCurrentPrice());
        System.out.println("End Time: " + getEndTime());
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getWarranty() {
        return warranty;
    }

    // Hỗ trợ Người 4: Kiểm tra xem hàng còn bảo hành không
    public boolean hasWarranty() {
        return warranty > 0;
    }
}

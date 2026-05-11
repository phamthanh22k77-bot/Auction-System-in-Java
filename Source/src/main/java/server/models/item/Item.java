package server.models.item;

import server.models.Entity;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;

    // Thêm biến static để lưu item hiện tại
    private static Item currentItem;


    // 1. Constructor cho việc tạo mới vật phẩm (ID tự sinh)
    public Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;

        // Gán item hiện tại
        currentItem = this;
    }

    // 2. Constructor cho việc nạp dữ liệu từ Database/Network (ID có sẵn)
    public Item(String id, String name, String description, double startingPrice, double currentPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;

        // Gán item hiện tại
        currentItem = this;
    }

    public abstract ItemCategory getCategory();

    public abstract void printInfo();

    // Thêm static getter
    public static Item getCurrentItem() {
        return currentItem;
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

    @Override
    public String toString() {
        return name + " (Current: $" + currentPrice + ")";
    }
}

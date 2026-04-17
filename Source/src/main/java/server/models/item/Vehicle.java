package server.models.item;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String engineType;
    private int modelYear;
    private double mileage; // Số km đã đi
    private String licensePlate;

    public Vehicle(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String engineType, int modelYear, double mileage, String licensePlate) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.engineType = engineType;
        this.modelYear = modelYear;
        this.mileage = mileage;
        this.licensePlate = licensePlate;
    }

    public Vehicle(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String engineType, int modelYear,
            double mileage, String licensePlate) {
        super(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status);
        this.engineType = engineType;
        this.modelYear = modelYear;
        this.mileage = mileage;
        this.licensePlate = licensePlate;
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.VEHICLE;
    }

    @Override
    public void printInfo() {
        System.out.println("=== VEHICLE DETAILS ===");
        System.out.println("Vehicle: " + getName());
        System.out.println("Year: " + modelYear + " | Engine: " + engineType);
        System.out.println("Mileage: " + mileage + " km");
        System.out.println("Plate: " + licensePlate);
        System.out.println("Current Price: $" + getCurrentPrice());
    }

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
}

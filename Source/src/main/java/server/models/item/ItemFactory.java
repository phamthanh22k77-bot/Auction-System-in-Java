package server.models.item;

import java.time.LocalDateTime;

public class ItemFactory {

    // ELECTRONICS
    public static Electronics createElectronics(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String brand, String model, int warranty) {
        return new Electronics(name, description, startingPrice, startTime, endTime, sellerId, brand, model, warranty);
    }

    public static Electronics createElectronics(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String brand, String model, int warranty) {
        return new Electronics(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status,
                brand, model, warranty);
    }

    // ART
    public static Art createArt(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String artist, String medium, int year) {
        return new Art(name, description, startingPrice, startTime, endTime, sellerId, artist, medium, year);
    }

    public static Art createArt(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String artist, String medium, int year) {
        return new Art(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status, artist,
                medium, year);
    }

    // VEHICLE
    public static Vehicle createVehicle(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String engineType, int modelYear, double mileage, String licensePlate) {
        return new Vehicle(name, description, startingPrice, startTime, endTime, sellerId, engineType, modelYear,
                mileage, licensePlate);
    }

    public static Vehicle createVehicle(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String engineType, int modelYear,
            double mileage, String licensePlate) {
        return new Vehicle(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status,
                engineType, modelYear, mileage, licensePlate);
    }
}

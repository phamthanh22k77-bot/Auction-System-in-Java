package server.models.item;

import java.time.LocalDateTime;

public class ItemFactory {

    // Tạo mới Electronics (chưa có ID, ID sẽ được tự sinh trong constructor)
    public static Electronics createElectronics(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String brand, String model, int warranty) {
        return new Electronics(name, description, startingPrice, startTime, endTime, sellerId, brand, model, warranty);
    }

    /** Nạp Electronics từ dữ liệu (đã có ID và thông tin đấu giá). */
    public static Electronics createElectronics(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String brand, String model, int warranty) {
        return new Electronics(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status,
                brand, model, warranty);
    }

    // Tạo mới Art
    public static Art createArt(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String artist, String medium, int year) {
        return new Art(name, description, startingPrice, startTime, endTime, sellerId, artist, medium, year);
    }

    // Nạp Art từ dữ liệu
    public static Art createArt(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String artist, String medium, int year) {
        return new Art(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status, artist,
                medium, year);
    }

    // Tạo mới Vehicle
    public static Vehicle createVehicle(String name, String description, double startingPrice,
            LocalDateTime startTime, LocalDateTime endTime, String sellerId,
            String engineType, int modelYear, double mileage, String licensePlate) {
        return new Vehicle(name, description, startingPrice, startTime, endTime, sellerId, engineType, modelYear,
                mileage, licensePlate);
    }

    /// Nạp Vehicle từ dữ liệu.
    public static Vehicle createVehicle(String id, String name, String description, double startingPrice,
            double currentPrice, LocalDateTime startTime, LocalDateTime endTime,
            String sellerId, String status, String engineType, int modelYear,
            double mileage, String licensePlate) {
        return new Vehicle(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, status,
                engineType, modelYear, mileage, licensePlate);
    }

    public static Item createItem(String category, Object... args) {
        if (category == null)
            return null;

        // Gợi ý: Với hàm tổng quát này, bạn có thể bổ sung logic parse ép kiểu args[]
        // để gọi đến các hàm create cụ thể ở trên nếu cần dùng trong DAO.
        return switch (category.toUpperCase()) {
            case "ELECTRONICS" -> null;
            case "ART" -> null;
            case "VEHICLE" -> null;
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
    }
}

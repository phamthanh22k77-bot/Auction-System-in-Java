package server.models.item;


public class ItemFactory {

    // ELECTRONICS
    public static Electronics createElectronics(String name, String description, double startingPrice,
            String brand, String model, int warranty) {
        return new Electronics(name, description, startingPrice, brand, model, warranty);
    }

    public static Electronics createElectronics(String id, String name, String description, double startingPrice,
            double currentPrice, String brand, String model, int warranty) {
        return new Electronics(id, name, description, startingPrice, currentPrice,
                brand, model, warranty);
    }

    // ART
    public static Art createArt(String name, String description, double startingPrice,
            String artist, String medium, int year) {
        return new Art(name, description, startingPrice, artist, medium, year);
    }

    public static Art createArt(String id, String name, String description, double startingPrice,
            double currentPrice, String artist, String medium, int year) {
        return new Art(id, name, description, startingPrice, currentPrice, artist,
                medium, year);
    }

    // VEHICLE
    public static Vehicle createVehicle(String name, String description, double startingPrice,
            String engineType, int modelYear, double mileage, String licensePlate) {
        return new Vehicle(name, description, startingPrice, engineType, modelYear,
                mileage, licensePlate);
    }

    public static Vehicle createVehicle(String id, String name, String description, double startingPrice,
            double currentPrice, String engineType, int modelYear,
            double mileage, String licensePlate) {
        return new Vehicle(id, name, description, startingPrice, currentPrice,
                engineType, modelYear, mileage, licensePlate);
    }
}

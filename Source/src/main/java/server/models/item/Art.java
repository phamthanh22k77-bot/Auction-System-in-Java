package server.models.item;


public class Art extends Item {
    private String artist;
    private String medium; // Chất liệu (Sơn dầu, Acrylic, ...)
    private int year; // Năm sáng tác

    public Art(String name, String description, double startingPrice,
            String artist, String medium, int year) {
        super(name, description, startingPrice);
        this.artist = artist;
        this.medium = medium;
        this.year = year;
    }

    public Art(String id, String name, String description, double startingPrice,
            double currentPrice, String artist, String medium, int year) {
        super(id, name, description, startingPrice, currentPrice);
        this.artist = artist;
        this.medium = medium;
        this.year = year;
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.ART;
    }

    @Override
    public void printInfo() {
        System.out.println("ARTWORK DETAILS");
        System.out.println("Title: " + getName());
        System.out.println("Artist: " + artist);
        System.out.println("Medium: " + medium + " (" + year + ")");
        System.out.println("Estimated Value: $" + getCurrentPrice());
    }

    public String getArtist() {
        return artist;
    }

    public String getMedium() {
        return medium;
    }

    public int getYear() {
        return year;
    }
}

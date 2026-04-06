package model.user;

public class Seller extends User {
    private String companyName;
    private double rating;

    public Seller(String username, String email, String password, String companyName) {
        super(username, email, password);
        this.companyName = companyName;
        this.rating = 5.0; // Mặc định cho người mới
    }

    public Seller(String id, String username, String email, String password, String companyName, double rating) {
        super(id, username, email, password);
        this.companyName = companyName;
        this.rating = rating;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String getRole() {
        return "SELLER";
    }

    @Override
    public void printInfo() {
        System.out.println("ID: " + getId());
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: " + getRole());
        System.out.println("Company: " + companyName);
        System.out.println("Rating: " + rating + " stars");
    }
}

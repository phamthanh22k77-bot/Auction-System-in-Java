package server.models.user;

public class Bidder extends User {
    private double balance;

    // Tạo Bidder mới
    public Bidder(String username, String email, String password, double balance) {
        super(username, email, password);
        this.balance = balance;
    }

    // Nạp Bidder từ Database
    public Bidder(String id, String username, String email, String password, double balance) {
        super(id, username, email, password);
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }

    @Override
    public void printInfo() {
        System.out.println("ID: " + getId());
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: " + getRole());
        System.out.println("Balance: $" + balance);
    }
}

package model.user;

public class Admin extends User {
    private String department;

    public Admin(String username, String email, String password, String department) {
        super(username, email, password);
        this.department = department;
    }

    public Admin(String id, String username, String email, String password, String department) {
        super(id, username, email, password);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public void printInfo() {
        System.out.println("ID: " + getId());
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: " + getRole());
        System.out.println("Department: " + department);
    }
}

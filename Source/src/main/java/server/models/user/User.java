package server.models.user;

import server.models.Entity;

public abstract class User extends Entity {
    private String username;
    private String email;
    private String password; // Trong thực tế nên được mã hóa
    private boolean isLocked = false; // Trạng thái khóa tài khoản

    public User(String username, String email, String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.isLocked = false;
    }

    // cái thứ 2 dành cho DataBase và Networking
    public User(String id, String username, String email, String password) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.isLocked = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean login(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }

    public void logout() {
        System.out.println("User " + username + " has logged out.");
    }

    public abstract String getRole();

    public abstract void printInfo();

    // Getter cho password (chỉ dùng nội bộ hoặc cho DAO)
    public String getPassword() {
        return password;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}

package server.payload;

import java.io.Serializable;

public class RegisterPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String email;
    private String password;
    private String role;

    public RegisterPayload(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}

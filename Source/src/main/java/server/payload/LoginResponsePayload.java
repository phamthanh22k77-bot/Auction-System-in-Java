package server.payload;

import java.io.Serializable;
import server.models.user.User;

public class LoginResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private User user;

    public LoginResponsePayload(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}

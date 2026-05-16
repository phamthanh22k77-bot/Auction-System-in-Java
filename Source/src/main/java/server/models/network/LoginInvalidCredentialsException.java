package server.models.network;

/**
 * LoginInvalidCredentialsException — ném ra khi username hoặc password không khớp.
 */
public class LoginInvalidCredentialsException extends LoginException {

    public LoginInvalidCredentialsException() {
        super("Sai tên đăng nhập hoặc mật khẩu.");
    }

    public LoginInvalidCredentialsException(String username) {
        super("Xác thực thất bại cho tài khoản: '" + username + "'. Sai tên đăng nhập hoặc mật khẩu.");
    }
}

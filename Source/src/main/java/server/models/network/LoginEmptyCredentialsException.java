package server.models.network;

/**
 * LoginEmptyCredentialsException — ném ra khi username hoặc password trống/null.
 */
public class LoginEmptyCredentialsException extends LoginException {

    public LoginEmptyCredentialsException() {
        super("Tên đăng nhập và mật khẩu không được để trống.");
    }

    public LoginEmptyCredentialsException(String field) {
        super("Trường '" + field + "' không được để trống.");
    }
}

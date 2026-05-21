package server.models.network;

/**
 * LoginDataAccessException — ném ra khi không thể đọc dữ liệu người dùng từ file/database.
 */
public class LoginDataAccessException extends LoginException {

    public LoginDataAccessException() {
        super("Không thể truy xuất dữ liệu người dùng từ hệ thống lưu trữ.");
    }

    public LoginDataAccessException(String detail) {
        super("Lỗi truy xuất dữ liệu: " + detail);
    }
}

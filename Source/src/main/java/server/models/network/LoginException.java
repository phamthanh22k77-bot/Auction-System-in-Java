package server.models.network;

/**
 * LoginException — ngoại lệ gốc cho tất cả lỗi liên quan đến xác thực.
 * Kế thừa từ ServerException để đồng nhất với hệ thống exception hiện tại.
 */
public class LoginException extends ServerException {

    public LoginException(String message) {
        super(message);
    }

    public LoginException() {
        super("Lỗi xác thực không xác định.");
    }
}

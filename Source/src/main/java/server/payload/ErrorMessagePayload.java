package server.payload;

import java.io.Serializable;

// Payload dùng để gửi thông báo lỗi từ server → client
// 👉 Server tạo object này khi có lỗi xảy ra (logic, validation, system,...)
// 👉 Client nhận và hiển thị message lỗi cho user
public class ErrorMessagePayload implements Serializable {

    // ===== Attributes =====

    // Nội dung lỗi
    // 👉 Ví dụ:
    //    - "Auction not found"
    //    - "Invalid bid amount"
    //    - "User not authorized"
    private String errorMessage;

    // ===== Constructor =====

    // Khởi tạo payload với message lỗi
    public ErrorMessagePayload(String error) {
        this.errorMessage = error;
    }

    // ===== Getter & Setter =====

    // Controller/UI gọi để lấy message lỗi
    // 👉 Sau đó hiển thị lên màn hình (popup, label,...)
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object object) {
        if (this == object) return true; // cùng object
        if (object == null || getClass() != object.getClass()) return false;

        // ⚠️ super.equals(object) ở đây không cần thiết vì class không kế thừa custom equals
        ErrorMessagePayload that = (ErrorMessagePayload) object;

        // So sánh nội dung message lỗi
        return java.util.Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        // ⚠️ super.hashCode() cũng không cần thiết
        return java.util.Objects.hash(errorMessage);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in ra message lỗi
        return "ErrorMessagePayload{" +
                "error='" + errorMessage +
                "'}";
    }
}
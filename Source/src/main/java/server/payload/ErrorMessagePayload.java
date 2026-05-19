package server.payload;

import java.io.Serializable;

// Payload dùng để gửi thông báo lỗi từ server → client
// Server tạo object này khi có lỗi xảy ra (logic, validation, system,...)
// Client nhận và hiển thị message lỗi cho user
public class ErrorMessagePayload implements Serializable {

    private String errorMessage;

    // Khởi tạo payload với message lỗi
    public ErrorMessagePayload(String error) {
        this.errorMessage = error;
    }

    // Controller/UI gọi để lấy message lỗi
    // Sau đó hiển thị lên màn hình
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true; // cùng object
        if (object == null || getClass() != object.getClass())
            return false;

        ErrorMessagePayload that = (ErrorMessagePayload) object;

        return java.util.Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(errorMessage);
    }

    @Override
    public String toString() {
        return "ErrorMessagePayload{" + "error='" + errorMessage + "'}";
    }
}

package server.payload;

import java.util.Objects;

// Payload dùng để xác nhận việc đăng ký tham gia auction
// 👉 Server gửi payload này về client sau khi user đăng ký thành công
// 👉 Controller có thể đọc class này để biết auction nào đã được đăng ký
public class ConfirmAuctionRegistrationPayload {

    // ===== Attributes =====

    // ID của auction mà user vừa đăng ký
    // 👉 Controller có thể dùng ID này để:
    //    - Hiển thị thông báo "Đăng ký thành công"
    //    - Hoặc cập nhật UI (ví dụ: disable nút Register)
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với auctionID cụ thể
    public ConfirmAuctionRegistrationPayload(int auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Getter & Setter =====

    // Controller gọi hàm này để lấy auctionID
    // 👉 Dùng để xác định auction nào đã được đăng ký
    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (!(o instanceof ConfirmAuctionRegistrationPayload)) return false;

        ConfirmAuctionRegistrationPayload that = (ConfirmAuctionRegistrationPayload) o;

        // So sánh 2 payload có cùng auctionID không
        return Objects.equals(getAuctionID(), that.getAuctionID());
    }

    @Override
    public int hashCode() {
        // Hash dựa trên auctionID
        return Objects.hash(getAuctionID());
    }

    @Override
    public String toString() {
        // 👉 Nếu controller in trực tiếp object này:
        // System.out.println(payload);
        // → sẽ in ra thông tin auction đã đăng ký
        return "ConfirmCancelingAuctionPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
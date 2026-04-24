package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Class này dùng để đóng gói dữ liệu khi client gửi request tham gia auction
// Implement Serializable để có thể gửi qua socket (ObjectOutputStream)
public class RegisterClientPayload implements Serializable {

    // Thuộc tính: lưu ID của auction mà client muốn tham gia
    private int auctionID;
    // 👉 Controller (client-side) sẽ gán giá trị này khi user chọn auction để join
    // 👉 Ví dụ: new RegisterClientPayload(auctionID)
    // 👉 Server sẽ đọc giá trị này để xử lý: joinAuction(auctionID, client)

    // Constructor: khởi tạo payload với auctionID
    public RegisterClientPayload(int auctionID) {
        this.auctionID = auctionID;
    }

    // Setter: cho phép thay đổi auctionID
    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
        // 👉 Controller có thể dùng nếu cần cập nhật lại auctionID trước khi gửi
    }

    // Getter: lấy ra auctionID
    public int getAuctionID() {
        return auctionID;
        // 👉 Server-side (ClientHandler/Controller) sẽ gọi để biết client muốn join auction nào
    }

    // Override equals:
    // So sánh 2 object RegisterClientPayload có bằng nhau không
    // → bằng nếu cùng auctionID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng vùng nhớ → chắc chắn bằng
        if (o == null || getClass() != o.getClass()) return false; // khác kiểu → không bằng
        RegisterClientPayload that = (RegisterClientPayload) o;
        return auctionID == that.auctionID; // so sánh theo auctionID
    }

    // Override hashCode:
    // Dùng khi object nằm trong HashMap, HashSet
    // Phải consistent với equals (cùng auctionID → cùng hash)
    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    // Override toString:
    // Dùng để debug/log → in ra dạng chuỗi dễ đọc
    @Override
    public String toString() {
        // 👉 Controller có thể log payload này để kiểm tra request gửi đi
        return "ClientRegisterPayload{" +
                "auctionID=" + auctionID +
                '}';
    }
}
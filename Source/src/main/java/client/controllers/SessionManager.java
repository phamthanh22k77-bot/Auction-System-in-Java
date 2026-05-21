package client.controllers;

import server.models.user.Admin;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;

/**
 * SessionManager.
 * Lưu thông tin người dùng đang đăng nhập để tất cả controller dùng chung.
 * Sử dụng Singleton Pattern để đảm bảo tính duy nhất và an toàn dữ liệu.
 */
public class SessionManager {

    // 1. Instance duy nhất của SessionManager (Khởi tạo Eager để đảm bảo an toàn đa luồng cực hạn)
    private static final SessionManager instance = new SessionManager();

    // 2. Đối tượng User đang trong phiên làm việc
    private User currentUser;

    // 3. Danh sách lưu trữ lịch sử thông báo tinh gọn
    private final java.util.List<String> notifications = new java.util.ArrayList<>();

    public java.util.List<String> getNotifications() {
        return notifications;
    }

    public void addNotification(String msg) {
        if (!notifications.contains(msg)) {
            notifications.add(0, msg); // Đưa thông báo mới nhất lên đầu danh sách
        }
    }

    // 3. Private Constructor: Ngăn không cho tạo đối tượng bằng 'new' từ bên ngoài
    private SessionManager() {
    }

    // Phương thức để lấy instance duy nhất của SessionManager.
    public static SessionManager getInstance() {
        return instance;
    }




    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    public User getCurrentUser() {
        return currentUser;
    }

    // Xóa thông tin phiên (Đăng xuất).
    public void clear() {
        currentUser = null;
        notifications.clear();
    }


    public Bidder asBidder() {
        return (currentUser instanceof Bidder b) ? b : null;
    }

    public Seller asSeller() {
        return (currentUser instanceof Seller s) ? s : null;
    }

    public Admin asAdmin() {
        return (currentUser instanceof Admin a) ? a : null;
    }


    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }

    public String getEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }

    public String getRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    /**
     * Lấy số dư tài khoản (chỉ dành cho Bidder).
     */
    public double getBalance() {
        Bidder b = asBidder();
        return b != null ? b.getBalance() : 0;
    }

    /**
     * Kiểm tra trạng thái đã đăng nhập hay chưa.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}

package client.controllers;

import server.models.user.Admin;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;

/**
 * SessionManager
 *
 * Lưu thông tin người dùng đang đăng nhập để tất cả controller dùng chung.
 * Sử dụng Singleton Pattern để đảm bảo tính duy nhất và an toàn dữ liệu.
 */
public class SessionManager {

    // 1. Instance duy nhất của SessionManager
    private static SessionManager instance;

    // 2. Đối tượng User đang trong phiên làm việc
    private User currentUser;

    // 3. Private Constructor: Ngăn không cho tạo đối tượng bằng 'new' từ bên ngoài
    private SessionManager() {
    }

    /**
     * Phương thức để lấy instance duy nhất của SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Quản lý phiên làm việc (Set / Get) ───────────────────────────

    /** Thiết lập người dùng hiện tại sau khi đăng nhập thành công. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /** Lấy thông tin User hiện tại. */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Xóa thông tin phiên (Đăng xuất). */
    public void clear() {
        currentUser = null;
    }

    // ── Ép kiểu nhanh (Pattern Matching) ─────────────────────────────

    /** Trả về Bidder hiện tại. Null nếu user không phải Bidder. */
    public Bidder asBidder() {
        return (currentUser instanceof Bidder b) ? b : null;
    }

    /** Trả về Seller hiện tại. Null nếu user không phải Seller. */
    public Seller asSeller() {
        return (currentUser instanceof Seller s) ? s : null;
    }

    /** Trả về Admin hiện tại. Null nếu user không phải Admin. */
    public Admin asAdmin() {
        return (currentUser instanceof Admin a) ? a : null;
    }

    // ── Các hàm tiện ích lấy dữ liệu nhanh ───────────────────────────

    /** Lấy tên đăng nhập của người dùng. */
    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }

    /** Lấy email của người dùng. */
    public String getEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }

    /** Lấy vai trò (Role) của người dùng. */
    public String getRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    /** Lấy số dư tài khoản (chỉ dành cho Bidder). */
    public double getBalance() {
        Bidder b = asBidder();
        return b != null ? b.getBalance() : 0;
    }

    /** Kiểm tra trạng thái đã đăng nhập hay chưa. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
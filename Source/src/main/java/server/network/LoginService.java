package server.network;

import server.dao.UserDAO;
import server.models.network.LoginDataAccessException;
import server.models.network.LoginEmptyCredentialsException;
import server.models.network.LoginException;
import server.models.network.LoginInvalidCredentialsException;
import server.models.user.User;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoginService — xử lý toàn bộ logic xác thực người dùng ở phía Server.
 *
 * <p>Tách biệt hoàn toàn với ClientHandler để dễ bảo trì và test.
 * Ném ra các LoginException cụ thể thay vì trả về null.</p>
 *
 * <p>Hierarchy exception:</p>
 * <pre>
 * LoginException (gốc)
 * ├── LoginEmptyCredentialsException  — username/password trống
 * ├── LoginInvalidCredentialsException — sai username hoặc password
 * └── LoginDataAccessException        — không đọc được file dữ liệu
 * </pre>
 */
public class LoginService {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());
    private final UserDAO userDAO;

    /**
     * Constructor mặc định — dùng UserDAO chuẩn để đọc từ file data/users.json.
     */
    public LoginService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Constructor dùng để inject UserDAO tùy chỉnh (ví dụ khi viết unit test).
     *
     * @param userDAO DAO được inject từ bên ngoài
     */
    public LoginService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Xác thực thông tin đăng nhập.
     *
     * <p>Quy trình:</p>
     * <ol>
     *   <li>Validate đầu vào (không được trống).</li>
     *   <li>Đọc danh sách user từ UserDAO.</li>
     *   <li>Tìm user khớp username (không phân biệt chữ hoa/thường) và password (phân biệt).</li>
     *   <li>Trả về User nếu khớp, hoặc ném LoginInvalidCredentialsException.</li>
     * </ol>
     *
     * @param username tên đăng nhập do client cung cấp
     * @param password mật khẩu do client cung cấp
     * @return User đã được xác thực thành công
     * @throws LoginEmptyCredentialsException  nếu username hoặc password trống
     * @throws LoginInvalidCredentialsException nếu tài khoản/mật khẩu không khớp
     * @throws LoginDataAccessException        nếu không đọc được dữ liệu user
     */
    public User login(String username, String password) throws LoginException {

        // ── Bước 1: Validate đầu vào ─────────────────────────────────
        LOGGER.info("[LoginService] Bắt đầu xác thực cho username='" + username + "'");

        if (username == null || username.isBlank()) {
            LOGGER.warning("[LoginService] Username trống.");
            throw new LoginEmptyCredentialsException("username");
        }
        if (password == null || password.isEmpty()) {
            LOGGER.warning("[LoginService] Password trống.");
            throw new LoginEmptyCredentialsException("password");
        }

        // ── Bước 2: Đọc dữ liệu user từ DAO ─────────────────────────
        List<User> users;
        try {
            users = userDAO.loadAll();
            LOGGER.info("[LoginService] Đã tải " + users.size() + " user từ DAO.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[LoginService] Lỗi khi đọc dữ liệu user.", e);
            throw new LoginDataAccessException(e.getMessage());
        }

        // ── Bước 3: Tìm user khớp ────────────────────────────────────
        User matchedUser = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (matchedUser == null) {
            LOGGER.warning("[LoginService] Xác thực thất bại cho username='" + username + "'.");
            throw new LoginInvalidCredentialsException(username);
        }

        LOGGER.info("[LoginService] Xác thực thành công. Role: " + matchedUser.getRole()
                + ", ID: " + matchedUser.getId());
        return matchedUser;
    }
}

import server.dao.UserDAO;
import server.models.user.Bidder;
import server.models.user.User;

import java.util.Scanner;

/**
 * UserLoginTest - Chạy thử đăng nhập và đăng ký từ bàn phím.
 * Cách chạy: bấm Run vào hàm main().
 */
public class UserLoginTest {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        // Kiểm tra username đã tồn tại chưa
        boolean daCoTaiKhoan = false;
        for (User u : userDAO.loadAll()) {
            if (u.getUsername().equals(username)) {
                daCoTaiKhoan = true;
                break;
            }
        }

        if (daCoTaiKhoan) {
            // Username đã có → đăng nhập
            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (dangNhap(username, password)) {
                System.out.println("Login successfully! Hello " + username);
            } else {
                System.out.println("Wrong password.");
            }

        } else {
            // Username chưa có → đăng ký
            System.out.println("Username not found. Registering new account...");

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            System.out.print("Starting balance: ");
            double balance = Double.parseDouble(scanner.nextLine());

            dangKy(username, email, password, balance);
        }

        scanner.close();
    }

    /** Kiểm tra đăng nhập — duyệt từng user, dùng login() để so khớp. */
    public static boolean dangNhap(String username, String password) throws Exception {
        UserDAO userDAO = new UserDAO();

        for (User u : userDAO.loadAll()) {
            if (u.login(username, password)) {
                return true; // Khớp rồi
            }
        }

        return false; // Không tìm thấy
    }

    /** Đăng ký user mới (loại Bidder) rồi lưu vào file. */
    public static void dangKy(String username, String email, String password, double balance) throws Exception {
        UserDAO userDAO = new UserDAO();

        // Kiểm tra username đã tồn tại chưa
        for (User u : userDAO.loadAll()) {
            if (u.getUsername().equals(username)) {
                System.out.println("Username already exists, choose another username.");
                return; // Dừng lại, không đăng ký
            }
        }

        // Tạo Bidder mới (ID tự sinh trong constructor)
        Bidder bidderMoi = new Bidder(username, email, password, balance);

        // Lưu vào file
        userDAO.them(bidderMoi);

        System.out.println("Login successfully! Hello " + username);
    }
}

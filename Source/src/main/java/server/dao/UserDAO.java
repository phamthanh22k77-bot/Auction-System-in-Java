package server.dao;

// Import các class User cần dùng
import server.models.user.Admin;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;

// Import công cụ đọc/ghi file của Java
import java.io.IOException;
import java.nio.file.Files; // Dùng để đọc và ghi file
import java.nio.file.Path; // Dùng để tạo đường dẫn file
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Lưu và tải danh sách User từ file JSON.
 * File lưu tại: data/users.json
 */
public class UserDAO {

    // Đường dẫn tới file JSON sẽ lưu dữ liệu
    private static final String FILE_PATH = "data/users.json";

    public void saveAll(List<User> users) throws IOException {
        // Bắt đầu xây dựng nội dung file JSON dạng mảng [...]
        StringBuffer ketQua = new StringBuffer();
        ketQua.append("[\n"); // Mở mảng JSON

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i); // Lấy từng user trong danh sách

            // Chuyển user thành 1 object JSON { ... }
            ketQua.append(userToJson(u));

            // Nếu không phải phần tử cuối cùng thì thêm dấu phẩy ","
            if (i < users.size() - 1) {
                ketQua.append(",");
            }
            ketQua.append("\n"); // Xuống dòng cho đẹp
        }

        ketQua.append("]"); // Đóng mảng JSON

        // Ghi toàn bộ chuỗi vừa tạo vào file (ghi đè nếu file đã có)
        Files.writeString(Path.of(FILE_PATH), ketQua.toString());

        System.out.println("Đã lưu " + users.size() + " user vào " + FILE_PATH);
    }

    // =====================================================================
    // loadAll() - Đọc file JSON và trả về danh sách User
    // =====================================================================
    public List<User> loadAll() throws IOException {

        // Tạo danh sách rỗng để chứa kết quả
        List<User> users = new ArrayList<>();

        // Nếu file chưa tồn tại thì trả về danh sách rỗng luôn
        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return users;
        }

        // Đọc toàn bộ nội dung file thành một chuỗi
        String content = Files.readString(Path.of(FILE_PATH)).strip();

        // Bỏ dấu "[" ở đầu và "]" ở cuối (vì JSON mảng bao bên ngoài)
        content = content.substring(1, content.length() - 1);

        // Tách chuỗi ra thành danh sách các object JSON { ... }
        List<String> objects = tachCacObject(content);

        // Duyệt từng object JSON, tạo User tương ứng
        for (String obj : objects) {
            String role = layGiaTri(obj, "role"); // Lấy loại user (ADMIN/BIDDER/SELLER)

            String id = layGiaTri(obj, "id");
            String username = layGiaTri(obj, "username");
            String email = layGiaTri(obj, "email");
            String password = layGiaTri(obj, "password");

            // Tùy theo role mà tạo đúng loại đối tượng
            if ("ADMIN".equals(role)) {
                String department = layGiaTri(obj, "department");
                users.add(new Admin(id, username, email, password, department));

            } else if ("BIDDER".equals(role)) {
                double balance = Double.parseDouble(layGiaTri(obj, "balance"));
                users.add(new Bidder(id, username, email, password, balance));

            } else if ("SELLER".equals(role)) {
                String companyName = layGiaTri(obj, "companyName");
                double rating = Double.parseDouble(layGiaTri(obj, "rating"));
                users.add(new Seller(id, username, email, password, companyName, rating));
            }
        }

        System.out.println("Đã tải " + users.size() + " user từ " + FILE_PATH);
        return users;
    }

    /** Thêm 1 user mới vào file (load → thêm → save lại). */
    public void them(User user) throws IOException {
        List<User> ds = loadAll(); // Tải danh sách hiện tại
        ds.add(user); // Thêm user mới vào
        saveAll(ds); // Ghi lại toàn bộ
    }

    /** Xóa user theo id khỏi file. */
    public void xoaTheoId(String id) throws IOException {
        List<User> ds = loadAll();
        ds.removeIf(u -> u.getId().equals(id)); // Xóa user có id trùng
        saveAll(ds);
    }

    /** Tìm user theo id. Trả về null nếu không thấy. */
    public User timTheoId(String id) throws IOException {
        List<User> danhSach = loadAll(); // Tải toàn bộ danh sách

        for (User u : danhSach) { // Duyệt từng user một
            if (u.getId().equals(id)) { // Nếu id khớp thì trả về luôn
                return u; // Tìm thấy
            }
        }

        System.out.println("Không tìm thấy user với id: " + id);
        return null; // Duyệt hết mà không thấy
    }

    private String userToJson(User u) {
        StringBuffer sb = new StringBuffer();
        sb.append("  {\n");

        // Ghi các trường chung của mọi User
        sb.append("    \"id\": \"" + u.getId() + "\",\n");
        sb.append("    \"role\": \"" + u.getRole() + "\",\n"); // Quan trọng! loadAll() dùng cái này để phân biệt
                                                               // Admin/Bidder/Seller
        sb.append("    \"username\": \"" + u.getUsername() + "\",\n");
        sb.append("    \"email\": \"" + u.getEmail() + "\",\n");
        sb.append("    \"password\": \"" + u.getPassword() + "\"");

        // Ghi thêm trường riêng tùy loại User
        if (u instanceof Admin) {
            Admin a = (Admin) u;
            sb.append(",\n    \"department\": \"" + a.getDepartment() + "\"");

        } else if (u instanceof Bidder) {
            Bidder b = (Bidder) u;
            sb.append(",\n    \"balance\": " + b.getBalance());

        } else if (u instanceof Seller) {
            Seller s = (Seller) u;
            sb.append(",\n    \"companyName\": \"" + s.getCompanyName() + "\"");
            sb.append(",\n    \"rating\": " + s.getRating());
        }

        sb.append("\n  }"); // Đóng object JSON
        return sb.toString();
    }

    /**
     * Tách chuỗi JSON mảng thành danh sách các object { ... }.
     * Ví dụ: "{...}, {...}" → ["{ ... }", "{ ... }"]
     */
    private List<String> tachCacObject(String content) {
        List<String> result = new ArrayList<>();
        int depth = 0; // Đếm số dấu { đang mở
        int start = -1; // Vị trí bắt đầu của object hiện tại

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '{') {
                if (depth == 0)
                    start = i; // Ghi nhớ vị trí bắt đầu
                depth++; // Mở thêm 1 cấp
            } else if (c == '}') {
                depth--; // Đóng 1 cấp
                if (depth == 0) { // Nếu đã đóng hết thì lấy object
                    result.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    /**
     * Lấy giá trị của một trường trong chuỗi JSON.
     * Ví dụ: layGiaTri("{\"role\": \"ADMIN\"}", "role") → "ADMIN"
     */
    private String layGiaTri(String json, String tenTruong) {
        // Tìm chuỗi "tenTruong": (gộp luôn dấu :)
        String timKiem = "\"" + tenTruong + "\":";
        int viTri = json.indexOf(timKiem);
        if (viTri == -1)
            return ""; // Không có trường này

        // Lấy phần chuỗi BÊN PHẢI dấu :, bỏ khoảng trắng
        String phanSau = json.substring(viTri + timKiem.length()).trim();

        if (phanSau.charAt(0) == '"') {
            // Giá trị là chuỗi → lấy nội dung trong ""
            return phanSau.substring(1, phanSau.indexOf('"', 1));
        } else {
            // Giá trị là số → lấy đến dấu , hoặc }
            int cuoi = 0;
            while (cuoi < phanSau.length() && ",}\n".indexOf(phanSau.charAt(cuoi)) == -1)
                cuoi++;
            return phanSau.substring(0, cuoi).trim();
        }
    }

}

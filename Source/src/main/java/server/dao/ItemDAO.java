package server.dao;

// Import các class Item cần dùng
import server.models.item.Art;
import server.models.item.Electronics;
import server.models.item.Item;
import server.models.item.Vehicle;

// Import công cụ đọc/ghi file và xử lý ngày giờ
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ItemDAO - Lưu và tải danh sách Item từ file JSON.
 * File lưu tại: data/items.json
 * Hỗ trợ 3 loại: Art (Nghệ thuật), Electronics (Điện tử), Vehicle (Xe)
 */
public class ItemDAO {

    // Đường dẫn tới file JSON
    private static final String FILE_PATH = "data/items.json";

    // =====================================================================
    // saveAll() - Ghi toàn bộ danh sách Item ra file JSON
    // =====================================================================
    public void saveAll(List<Item> items) throws IOException {

        // Tạo thư mục "data/" nếu chưa có
        Files.createDirectories(Path.of("data"));

        // Xây dựng nội dung JSON
        StringBuilder sb = new StringBuilder();
        sb.append("[\n"); // Mở mảng

        for (int i = 0; i < items.size(); i++) {
            sb.append(itemToJson(items.get(i))); // Chuyển mỗi item thành JSON

            if (i < items.size() - 1) {
                sb.append(","); // Thêm dấu phẩy giữa các phần tử
            }
            sb.append("\n");
        }

        sb.append("]"); // Đóng mảng

        // Ghi ra file
        Files.writeString(Path.of(FILE_PATH), sb.toString());
        System.out.println("Đã lưu " + items.size() + " item vào " + FILE_PATH);
    }

    // =====================================================================
    // loadAll() - Đọc file JSON và trả về danh sách Item
    // =====================================================================
    public List<Item> loadAll() throws IOException {

        List<Item> items = new ArrayList<>();

        // Nếu file chưa tồn tại thì trả về rỗng
        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return items;
        }

        // Đọc toàn bộ file thành chuỗi, bỏ khoảng trắng đầu/cuối
        String content = Files.readString(Path.of(FILE_PATH)).strip();

        // Bỏ dấu "[" đầu và "]" cuối
        content = content.substring(1, content.length() - 1);

        // Tách thành từng object { ... }
        List<String> objects = tachCacObject(content);

        for (String obj : objects) {
            String category = layGiaTri(obj, "category"); // Loại item

            // Lấy các trường chung của mọi Item
            String id = layGiaTri(obj, "id");
            String name = layGiaTri(obj, "name");
            String description = layGiaTri(obj, "description");
            double startingPrice = Double.parseDouble(layGiaTri(obj, "startingPrice"));
            double currentPrice = Double.parseDouble(layGiaTri(obj, "currentPrice"));
            LocalDateTime startTime = LocalDateTime.parse(layGiaTri(obj, "startTime"));
            LocalDateTime endTime = LocalDateTime.parse(layGiaTri(obj, "endTime"));
            String sellerId = layGiaTri(obj, "sellerId");
            String status = layGiaTri(obj, "status");

            // Tạo đúng loại Item theo category
            if ("ART".equals(category)) {
                String artist = layGiaTri(obj, "artist");
                String medium = layGiaTri(obj, "medium");
                int year = Integer.parseInt(layGiaTri(obj, "year"));
                items.add(new Art(id, name, description, startingPrice, currentPrice,
                        startTime, endTime, sellerId, status, artist, medium, year));

            } else if ("ELECTRONICS".equals(category)) {
                String brand = layGiaTri(obj, "brand");
                String model = layGiaTri(obj, "model");
                int warranty = Integer.parseInt(layGiaTri(obj, "warranty"));
                items.add(new Electronics(id, name, description, startingPrice, currentPrice,
                        startTime, endTime, sellerId, status, brand, model, warranty));

            } else if ("VEHICLE".equals(category)) {
                String engineType = layGiaTri(obj, "engineType");
                int modelYear = Integer.parseInt(layGiaTri(obj, "modelYear"));
                double mileage = Double.parseDouble(layGiaTri(obj, "mileage"));
                String licensePlate = layGiaTri(obj, "licensePlate");
                items.add(new Vehicle(id, name, description, startingPrice, currentPrice,
                        startTime, endTime, sellerId, status,
                        engineType, modelYear, mileage, licensePlate));
            }
        }

        System.out.println("Đã tải " + items.size() + " item từ " + FILE_PATH);
        return items;
    }

    // =====================================================================
    // CÁC TIỆN ÍCH NHỎ
    // =====================================================================

    /** Thêm 1 item mới vào file. */
    public void them(Item item) throws IOException {
        List<Item> ds = loadAll();
        ds.add(item);
        saveAll(ds);
    }

    /** Xóa item theo id. */
    public void xoaTheoId(String id) throws IOException {
        List<Item> ds = loadAll();
        ds.removeIf(item -> item.getId().equals(id)); // Xóa item có id khớp
        saveAll(ds);
    }

    /** Tìm item theo id. */
    public Optional<Item> timTheoId(String id) throws IOException {
        return loadAll().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    /** Cập nhật giá hiện tại của item. */
    public void capNhatGia(String itemId, double giaMoi) throws IOException {
        List<Item> ds = loadAll();
        for (Item item : ds) {
            if (item.getId().equals(itemId)) {
                item.setCurrentPrice(giaMoi); // Đặt giá mới
                break;
            }
        }
        saveAll(ds); // Lưu lại
    }

    /** Chuyển 1 Item thành chuỗi JSON. */
    private String itemToJson(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");

        // Các trường chung
        sb.append("    \"id\": \"" + item.getId() + "\",\n");
        sb.append("    \"category\": \"" + item.getCategory() + "\",\n");
        sb.append("    \"name\": \"" + item.getName() + "\",\n");
        sb.append("    \"description\": \"" + item.getDescription() + "\",\n");
        sb.append("    \"startingPrice\": " + item.getStartingPrice() + ",\n"); // Số → không có ""
        sb.append("    \"currentPrice\": " + item.getCurrentPrice() + ",\n");
        sb.append("    \"startTime\": \"" + item.getStartTime() + "\",\n");
        sb.append("    \"endTime\": \"" + item.getEndTime() + "\",\n");
        sb.append("    \"sellerId\": \"" + item.getSellerId() + "\",\n");
        sb.append("    \"status\": \"" + item.getStatus() + "\"");

        // Các trường riêng tùy loại
        if (item instanceof Art) {
            Art art = (Art) item;
            sb.append(",\n    \"artist\": \"" + art.getArtist() + "\"");
            sb.append(",\n    \"medium\": \"" + art.getMedium() + "\"");
            sb.append(",\n    \"year\": " + art.getYear()); // Số nguyên

        } else if (item instanceof Electronics) {
            Electronics elec = (Electronics) item;
            sb.append(",\n    \"brand\": \"" + elec.getBrand() + "\"");
            sb.append(",\n    \"model\": \"" + elec.getModel() + "\"");
            sb.append(",\n    \"warranty\": " + elec.getWarranty()); // Số nguyên

        } else if (item instanceof Vehicle) {
            Vehicle v = (Vehicle) item;
            sb.append(",\n    \"engineType\": \"" + v.getEngineType() + "\"");
            sb.append(",\n    \"modelYear\": " + v.getModelYear());
            sb.append(",\n    \"mileage\": " + v.getMileage());
            sb.append(",\n    \"licensePlate\": \"" + v.getLicensePlate() + "\"");
        }

        sb.append("\n  }");
        return sb.toString();
    }

    /** Tách chuỗi JSON thành danh sách các object { ... }. */
    private List<String> tachCacObject(String content) {
        List<String> result = new ArrayList<>();
        int depth = 0; // Đếm số dấu { đang mở
        int start = -1; // Vị trí bắt đầu object

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0)
                    start = i; // Bắt đầu object mới
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) { // Kết thúc object
                    result.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    /** Lấy giá trị của một trường trong JSON. */
    private String layGiaTri(String json, String tenTruong) {
        String khoa = "\"" + tenTruong + "\""; // Tên trường có dấu ""
        int viTriKhoa = json.indexOf(khoa); // Tìm vị trí tên trường
        if (viTriKhoa == -1)
            return ""; // Không tìm thấy → trả rỗng

        int viTriHaiCham = json.indexOf(':', viTriKhoa);// Tìm dấu ":" sau tên trường

        int viTriBatDau = viTriHaiCham + 1; // Bước qua dấu ":"
        while (Character.isWhitespace(json.charAt(viTriBatDau))) {
            viTriBatDau++; // Bỏ qua khoảng trắng
        }

        if (json.charAt(viTriBatDau) == '"') {
            // Giá trị là chuỗi: tìm dấu " đóng
            int viTriKet = json.indexOf('"', viTriBatDau + 1);
            return json.substring(viTriBatDau + 1, viTriKet); // Bỏ dấu " đầu và cuối
        } else {
            // Giá trị là số: đọc đến khi gặp "," hoặc "}"
            int viTriKet = viTriBatDau;
            while (viTriKet < json.length() && ",}\n".indexOf(json.charAt(viTriKet)) == -1) {
                viTriKet++;
            }
            return json.substring(viTriBatDau, viTriKet).trim();
        }
    }
}

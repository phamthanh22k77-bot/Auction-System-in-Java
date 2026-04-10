package server.dao;

import server.models.item.Art;
import server.models.item.Electronics;
import server.models.item.Item;
import server.models.item.Vehicle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO - Lưu và tải danh sách Item từ file JSON.
 * File lưu tại: data/items.json
 */
public class ItemDAO {

    // Đường dẫn tới file JSON sẽ lưu dữ liệu
    private static final String FILE_PATH = "data/items.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void saveAll(List<Item> items) throws IOException {
        StringBuffer ketQua = new StringBuffer();
        ketQua.append("[\n");

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            ketQua.append(itemToJson(item));

            if (i < items.size() - 1) {
                ketQua.append(",");
            }
            ketQua.append("\n");
        }

        ketQua.append("]");

        Files.writeString(Path.of(FILE_PATH), ketQua.toString());
        System.out.println("Đã lưu " + items.size() + " item vào " + FILE_PATH);
    }

    public List<Item> loadAll() throws IOException {
        List<Item> items = new ArrayList<>();

        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return items;
        }

        String content = Files.readString(Path.of(FILE_PATH)).strip();
        if (content.isEmpty() || content.equals("[]")) {
            return items;
        }

        // Bỏ dấu "[" ở đầu và "]" ở cuối
        content = content.substring(1, content.length() - 1);
        List<String> objects = tachCacObject(content);

        for (String obj : objects) {
            String category = layGiaTri(obj, "category");

            String id = layGiaTri(obj, "id");
            String name = layGiaTri(obj, "name");
            String description = layGiaTri(obj, "description");
            double startingPrice = Double.parseDouble(layGiaTri(obj, "startingPrice"));
            double currentPrice = Double.parseDouble(layGiaTri(obj, "currentPrice"));
            LocalDateTime startTime = LocalDateTime.parse(layGiaTri(obj, "startTime"), formatter);
            LocalDateTime endTime = LocalDateTime.parse(layGiaTri(obj, "endTime"), formatter);
            String sellerId = layGiaTri(obj, "sellerId");
            String status = layGiaTri(obj, "status");

            if ("ART".equals(category)) {
                String artist = layGiaTri(obj, "artist");
                String medium = layGiaTri(obj, "medium");
                int year = Integer.parseInt(layGiaTri(obj, "year"));

                items.add(new Art(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId,
                        status, artist, medium, year));

            } else if ("ELECTRONICS".equals(category)) {
                String brand = layGiaTri(obj, "brand");
                String model = layGiaTri(obj, "model");
                int warranty = Integer.parseInt(layGiaTri(obj, "warranty"));

                items.add(new Electronics(id, name, description, startingPrice, currentPrice, startTime, endTime,
                        sellerId, status, brand, model, warranty));

            } else if ("VEHICLE".equals(category)) {
                String engineType = layGiaTri(obj, "engineType");
                int modelYear = Integer.parseInt(layGiaTri(obj, "modelYear"));
                double mileage = Double.parseDouble(layGiaTri(obj, "mileage"));
                String licensePlate = layGiaTri(obj, "licensePlate");

                items.add(new Vehicle(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId,
                        status, engineType, modelYear, mileage, licensePlate));
            }
        }

        System.out.println("Đã tải " + items.size() + " item từ " + FILE_PATH);
        return items;
    }

    public void them(Item item) throws IOException {
        List<Item> ds = loadAll();
        ds.add(item);
        saveAll(ds);
    }

    public void xoaTheoId(String id) throws IOException {
        List<Item> ds = loadAll();
        ds.removeIf(i -> i.getId().equals(id));
        saveAll(ds);
    }

    public Item timTheoId(String id) throws IOException {
        List<Item> danhSach = loadAll();
        for (Item i : danhSach) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        System.out.println("Không tìm thấy item với id: " + id);
        return null;
    }

    private String itemToJson(Item item) {
        StringBuffer sb = new StringBuffer();
        sb.append("  {\n");

        sb.append("    \"id\": \"" + item.getId() + "\",\n");
        sb.append("    \"category\": \"" + item.getCategory() + "\",\n");
        sb.append("    \"name\": \"" + item.getName() + "\",\n");
        sb.append("    \"description\": \"" + item.getDescription() + "\",\n");
        sb.append("    \"startingPrice\": " + item.getStartingPrice() + ",\n");
        sb.append("    \"currentPrice\": " + item.getCurrentPrice() + ",\n");
        sb.append("    \"startTime\": \"" + item.getStartTime().format(formatter) + "\",\n");
        sb.append("    \"endTime\": \"" + item.getEndTime().format(formatter) + "\",\n");
        sb.append("    \"sellerId\": \"" + item.getSellerId() + "\",\n");
        sb.append("    \"status\": \"" + item.getStatus() + "\"");

        if (item instanceof Art) {
            Art a = (Art) item;
            sb.append(",\n    \"artist\": \"" + a.getArtist() + "\",\n");
            sb.append("    \"medium\": \"" + a.getMedium() + "\",\n");
            sb.append("    \"year\": " + a.getYear());

        } else if (item instanceof Electronics) {
            Electronics e = (Electronics) item;
            sb.append(",\n    \"brand\": \"" + e.getBrand() + "\",\n");
            sb.append("    \"model\": \"" + e.getModel() + "\",\n");
            sb.append("    \"warranty\": " + e.getWarranty());

        } else if (item instanceof Vehicle) {
            Vehicle v = (Vehicle) item;
            sb.append(",\n    \"engineType\": \"" + v.getEngineType() + "\",\n");
            sb.append("    \"modelYear\": " + v.getModelYear() + ",\n");
            sb.append("    \"mileage\": " + v.getMileage() + ",\n");
            sb.append("    \"licensePlate\": \"" + v.getLicensePlate() + "\"");
        }

        sb.append("\n  }");
        return sb.toString();
    }

    private List<String> tachCacObject(String content) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '{') {
                if (depth == 0)
                    start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    result.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    private String layGiaTri(String json, String tenTruong) {
        String timKiem = "\"" + tenTruong + "\":";
        int viTri = json.indexOf(timKiem);
        if (viTri == -1)
            return "";

        String phanSau = json.substring(viTri + timKiem.length()).trim();

        if (phanSau.charAt(0) == '"') {
            return phanSau.substring(1, phanSau.indexOf('"', 1));
        } else {
            int cuoi = 0;
            while (cuoi < phanSau.length() && ",}\n".indexOf(phanSau.charAt(cuoi)) == -1)
                cuoi++;
            return phanSau.substring(0, cuoi).trim();
        }
    }
}

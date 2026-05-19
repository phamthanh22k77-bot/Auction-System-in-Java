package server.dao;

import server.models.item.Art;
import server.models.item.Electronics;
import server.models.item.Item;
import server.models.item.Vehicle;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//DAO xử lý lưu trữ dữ liệu cho các vật phẩm (Item).
public class ItemDAO {

    public static String FILE_PATH = "data/items.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson gson = new GsonBuilder()
            // Adapter xử lý thời gian (LocalDateTime)
            .registerTypeHierarchyAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                            context) -> new JsonPrimitive(src.format(formatter)))
            .registerTypeHierarchyAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                            .parse(json.getAsString(), formatter))

            // Adapter xử lý đa hình (Item) dựa trên trường "category"
            .registerTypeHierarchyAdapter(Item.class, (JsonDeserializer<Item>) (json, typeOfT, context) -> {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement categoryElement = jsonObject.get("category");
                if (categoryElement == null) {
                    throw new JsonParseException("Lỗi thiếu trường 'category' trong Item JSON");
                }
                String category = categoryElement.getAsString();
                
                String id = jsonObject.has("id") ? jsonObject.get("id").getAsString() : java.util.UUID.randomUUID().toString();
                String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
                String description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : "";
                double startingPrice = jsonObject.has("startingPrice") ? jsonObject.get("startingPrice").getAsDouble() : 0.0;
                double currentPrice = jsonObject.has("currentPrice") ? jsonObject.get("currentPrice").getAsDouble() : startingPrice;

                return switch (category) {
                    case "ART" -> {
                        String artist = jsonObject.has("artist") ? jsonObject.get("artist").getAsString() : "";
                        String medium = jsonObject.has("medium") ? jsonObject.get("medium").getAsString() : "";
                        int year = jsonObject.has("year") ? jsonObject.get("year").getAsInt() : 0;
                        yield new Art(id, name, description, startingPrice, currentPrice, artist, medium, year);
                    }
                    case "ELECTRONICS" -> {
                        String brand = jsonObject.has("brand") ? jsonObject.get("brand").getAsString() : "";
                        String model = jsonObject.has("model") ? jsonObject.get("model").getAsString() : "";
                        int warranty = jsonObject.has("warranty") ? jsonObject.get("warranty").getAsInt() : 0;
                        yield new Electronics(id, name, description, startingPrice, currentPrice, brand, model, warranty);
                    }
                    case "VEHICLE" -> {
                        String engineType = jsonObject.has("engineType") ? jsonObject.get("engineType").getAsString() : "";
                        int modelYear = jsonObject.has("modelYear") ? jsonObject.get("modelYear").getAsInt() : 0;
                        double mileage = jsonObject.has("mileage") ? jsonObject.get("mileage").getAsDouble() : 0.0;
                        String licensePlate = jsonObject.has("licensePlate") ? jsonObject.get("licensePlate").getAsString() : "";
                        yield new Vehicle(id, name, description, startingPrice, currentPrice, engineType, modelYear, mileage, licensePlate);
                    }
                    default -> throw new JsonParseException("Category không hợp lệ: " + category);
                };
            })
            .registerTypeHierarchyAdapter(Item.class, (JsonSerializer<Item>) (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();
                
                if (src instanceof Electronics e) {
                    jsonObject.addProperty("brand", e.getBrand());
                    jsonObject.addProperty("model", e.getModel());
                    jsonObject.addProperty("warranty", e.getWarranty());
                } else if (src instanceof Art a) {
                    jsonObject.addProperty("artist", a.getArtist());
                    jsonObject.addProperty("medium", a.getMedium());
                    jsonObject.addProperty("year", a.getYear());
                } else if (src instanceof Vehicle v) {
                    jsonObject.addProperty("engineType", v.getEngineType());
                    jsonObject.addProperty("modelYear", v.getModelYear());
                    jsonObject.addProperty("mileage", v.getMileage());
                    jsonObject.addProperty("licensePlate", v.getLicensePlate());
                }
                
                jsonObject.addProperty("name", src.getName());
                jsonObject.addProperty("description", src.getDescription());
                jsonObject.addProperty("startingPrice", src.getStartingPrice());
                jsonObject.addProperty("currentPrice", src.getCurrentPrice());
                jsonObject.addProperty("id", src.getId());
                jsonObject.addProperty("category", src.getCategory().name());
                
                return jsonObject;
            })
            .setPrettyPrinting()
            .create();

    public synchronized void saveAll(List<Item> items) throws IOException {
        String json = gson.toJson(items);
        Files.writeString(Path.of(FILE_PATH), json);
        System.out.println("Đã lưu " + items.size() + " item vào " + FILE_PATH);
    }

    public synchronized List<Item> loadAll() throws IOException {
        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return new ArrayList<>();
        }

        String content = Files.readString(Path.of(FILE_PATH)).strip();
        if (content.isEmpty() || content.equals("[]")) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<Item>>() {
        }.getType();
        List<Item> items = gson.fromJson(content, listType);

        System.out.println("Đã tải " + items.size() + " item từ " + FILE_PATH);
        return items;
    }

    public synchronized void them(Item item) throws IOException {
        List<Item> ds = loadAll();
        ds.add(item);
        saveAll(ds);
    }

    public synchronized void xoaTheoId(String id) throws IOException {
        List<Item> ds = loadAll();
        boolean removed = ds.removeIf(i -> i.getId().equals(id));
        if (removed) {
            saveAll(ds);
        }
    }

    public synchronized Item timTheoId(String id) throws IOException {
        List<Item> danhSach = loadAll();
        for (Item i : danhSach) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        System.out.println("Không tìm thấy item với id: " + id);
        return null;
    }
}

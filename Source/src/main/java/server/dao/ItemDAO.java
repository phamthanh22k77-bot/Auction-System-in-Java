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

/**
 * ItemDAO - Lưu và tải danh sách Item từ file JSON.
 * File lưu tại: data/items.json
 */
public class ItemDAO {

    // Đường dẫn tới file JSON sẽ lưu dữ liệu
    private static final String FILE_PATH = "data/items.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson gson = new GsonBuilder()
            // Adapter xử lý thời gian (LocalDateTime)
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                            context) -> new JsonPrimitive(src.format(formatter)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                            .parse(json.getAsString(), formatter))
            // Adapter xử lý đa hình (Item) dựa trên trường "category"
            .registerTypeAdapter(Item.class, (JsonDeserializer<Item>) (json, typeOfT, context) -> {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement categoryElement = jsonObject.get("category");
                if (categoryElement == null) {
                    throw new JsonParseException("Lỗi thiếu trường 'category' trong Item JSON");
                }
                String category = categoryElement.getAsString();
                return switch (category) {
                    case "ART" -> context.deserialize(jsonObject, Art.class);
                    case "ELECTRONICS" -> context.deserialize(jsonObject, Electronics.class);
                    case "VEHICLE" -> context.deserialize(jsonObject, Vehicle.class);
                    default -> throw new JsonParseException("Category không hợp lệ: " + category);
                };
            })
            .registerTypeAdapter(Item.class, (JsonSerializer<Item>) (src, typeOfSrc, context) -> {
                JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
                jsonObject.addProperty("category", src.getCategory());
                return jsonObject;
            })
            .setPrettyPrinting()
            .create();

    public void saveAll(List<Item> items) throws IOException {
        String json = gson.toJson(items);
        Files.writeString(Path.of(FILE_PATH), json);
        System.out.println("Đã lưu " + items.size() + " item vào " + FILE_PATH);
    }

    public List<Item> loadAll() throws IOException {
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

    public void them(Item item) throws IOException {
        List<Item> ds = loadAll();
        ds.add(item);
        saveAll(ds);
    }

    public void xoaTheoId(String id) throws IOException {
        List<Item> ds = loadAll();
        boolean removed = ds.removeIf(i -> i.getId().equals(id));
        if (removed) {
            saveAll(ds);
        }
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
}

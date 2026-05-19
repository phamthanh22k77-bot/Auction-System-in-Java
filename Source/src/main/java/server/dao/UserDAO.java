package server.dao;

import server.models.user.Admin;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//DAO xử lý lưu trữ dữ liệu cho người dùng (User).
public class UserDAO {

    public static String FILE_PATH = "data/users.json";

    private static final Gson baseGson = new GsonBuilder().setPrettyPrinting().create();

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(User.class, (JsonDeserializer<User>) (json, typeOfT, context) -> {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement roleElement = jsonObject.get("role");
                if (roleElement == null) {
                    throw new JsonParseException("Lỗi thiếu trường 'role' trong User JSON");
                }
                String role = roleElement.getAsString();
                return switch (role.toUpperCase()) {
                    case "ADMIN" -> baseGson.fromJson(jsonObject, Admin.class);
                    case "BIDDER" -> baseGson.fromJson(jsonObject, Bidder.class);
                    case "SELLER" -> baseGson.fromJson(jsonObject, Seller.class);
                    default -> throw new JsonParseException("Role không hợp lệ: " + role);
                };
            })
            .registerTypeHierarchyAdapter(User.class, (JsonSerializer<User>) (src, typeOfSrc, context) -> {
                JsonObject jsonObject = baseGson.toJsonTree(src).getAsJsonObject();
                jsonObject.addProperty("role", src.getRole());
                return jsonObject;
            })
            .setPrettyPrinting()
            .create();

    public synchronized void saveAll(List<User> users) throws IOException {
        String json = gson.toJson(users);
        Files.writeString(Path.of(FILE_PATH), json);
        System.out.println("Đã lưu " + users.size() + " user vào " + FILE_PATH);
    }

    public synchronized List<User> loadAll() throws IOException {
        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return new ArrayList<>();
        }

        String content = Files.readString(Path.of(FILE_PATH)).strip();
        if (content.isEmpty() || content.equals("[]")) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<User>>() {
        }.getType();
        List<User> users = gson.fromJson(content, listType);

        System.out.println("Đã tải " + users.size() + " user từ " + FILE_PATH);
        return users;
    }

    public synchronized void them(User user) throws IOException {
        List<User> ds = loadAll();
        ds.add(user);
        saveAll(ds);
    }

    public synchronized void xoaTheoId(String id) throws IOException {
        List<User> ds = loadAll();
        boolean removed = ds.removeIf(u -> u.getId().equals(id));
        if (removed) {
            saveAll(ds);
        }
    }

    public synchronized User timTheoId(String id) throws IOException {
        List<User> danhSach = loadAll();
        for (User u : danhSach) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        System.out.println("Không tìm thấy user với id: " + id);
        return null;
    }
}

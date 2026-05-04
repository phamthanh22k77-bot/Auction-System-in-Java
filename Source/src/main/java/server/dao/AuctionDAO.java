package server.dao;

import server.models.auction.Auction;
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
 * DAO xử lý lưu trữ dữ liệu cho các phiên đấu giá (Auction).
 */
public class AuctionDAO {

    private static final String FILE_PATH = "data/auctions.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Cấu hình Gson CỰC KỲ GỌN NHẸ cho Auction (Không có đa hình rườm rà như Item)
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                                                     context) -> new JsonPrimitive(src.format(FORMATTER)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                            .parse(json.getAsString(), FORMATTER))
            .setPrettyPrinting()
            .create();

    public void saveAll(List<Auction> auctions) throws IOException {
        Path path = Path.of(FILE_PATH);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        String json = gson.toJson(auctions);
        Files.writeString(path, json);
    }

    public List<Auction> loadAll() throws IOException {
        if (!Files.exists(Path.of(FILE_PATH)))
            return new ArrayList<>();
        String content = Files.readString(Path.of(FILE_PATH)).strip();
        if (content.isEmpty() || content.equals("[]"))
            return new ArrayList<>();

        Type listType = new TypeToken<List<Auction>>() {
        }.getType();
        return gson.fromJson(content, listType);
    }

    public void them(Auction auction) throws IOException {
        List<Auction> ds = loadAll();
        ds.add(auction);
        saveAll(ds);
    }

    public void capNhat(Auction auction) throws IOException {
        List<Auction> ds = loadAll();
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getId().equals(auction.getId())) {
                ds.set(i, auction);
                break;
            }
        }
        saveAll(ds);
    }

    public void xoaTheoId(String id) throws IOException {
        List<Auction> ds = loadAll();
        if (ds.removeIf(a -> a.getId().equals(id))) {
            saveAll(ds);
        }
    }

    public Auction timTheoId(String id) throws IOException {
        for (Auction a : loadAll()) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }
}

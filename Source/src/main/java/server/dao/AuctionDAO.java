package server.dao;

import server.models.auction.Auction;
import server.models.auction.Auction.AuctionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionDAO - Lưu và tải danh sách Auction từ file JSON.
 * File lưu tại: data/auctions.json
 *
 * Phong cách nhất quán với ItemDAO và UserDAO:
 * - Không dùng thư viện JSON ngoài
 * - Tự serialize / deserialize thủ công
 * - winnerId rỗng được lưu là chuỗi "null"
 */
public class AuctionDAO {

    private static final String FILE_PATH = "data/auctions.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // saveAll - Ghi toàn bộ danh sách Auction ra file JSON
    // =========================================================================
    public void saveAll(List<Auction> auctions) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");

        for (int i = 0; i < auctions.size(); i++) {
            sb.append(auctionToJson(auctions.get(i)));
            if (i < auctions.size() - 1)
                sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        Files.writeString(Path.of(FILE_PATH), sb.toString());
        System.out.println("Đã lưu " + auctions.size() + " auction vào " + FILE_PATH);
    }

    // =========================================================================
    // loadAll - Đọc file JSON và trả về danh sách Auction
    // =========================================================================
    public List<Auction> loadAll() throws IOException {
        List<Auction> auctions = new ArrayList<>();

        if (!Files.exists(Path.of(FILE_PATH))) {
            System.out.println("File " + FILE_PATH + " chưa có. Trả về danh sách rỗng.");
            return auctions;
        }

        String content = Files.readString(Path.of(FILE_PATH)).strip();
        if (content.isEmpty() || content.equals("[]")) {
            return auctions;
        }

        // Bỏ dấu '[' đầu và ']' cuối
        content = content.substring(1, content.length() - 1);
        List<String> objects = tachCacObject(content);

        for (String obj : objects) {
            String id = layGiaTri(obj, "id");
            String itemId = layGiaTri(obj, "itemId");
            String sellerId = layGiaTri(obj, "sellerId");
            String bidderRaw = layGiaTri(obj, "highestBidderId");
            String highestBidderId = "null".equals(bidderRaw) ? null : bidderRaw;
            double startingPrice = Double.parseDouble(layGiaTri(obj, "startingPrice"));
            double currentHighestBid = Double.parseDouble(layGiaTri(obj, "currentHighestBid"));
            double minimumBidIncrement = Double.parseDouble(layGiaTri(obj, "minimumBidIncrement"));
            LocalDateTime startTime = LocalDateTime.parse(layGiaTri(obj, "startTime"), FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(layGiaTri(obj, "endTime"), FORMATTER);
            AuctionStatus status = AuctionStatus.valueOf(layGiaTri(obj, "status"));

            auctions.add(new Auction(id, itemId, sellerId, startTime, endTime, startingPrice, currentHighestBid, highestBidderId, minimumBidIncrement, status));
        }

        System.out.println("Đã tải " + auctions.size() + " auction từ " + FILE_PATH);
        return auctions;
    }

    // =========================================================================
    // them - Thêm một Auction mới vào file
    // =========================================================================
    public void them(Auction auction) throws IOException {
        List<Auction> ds = loadAll();
        ds.add(auction);
        saveAll(ds);
    }

    // =========================================================================
    // capNhat - Cập nhật một Auction đã có (tìm theo id rồi thay thế)
    // =========================================================================
    public void capNhat(Auction auction) throws IOException {
        List<Auction> ds = loadAll();
        boolean found = false;
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getId().equals(auction.getId())) {
                ds.set(i, auction);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Không tìm thấy auction để cập nhật: " + auction.getId());
            return;
        }
        saveAll(ds);
    }

    // =========================================================================
    // xoaTheoId - Xóa Auction theo id
    // =========================================================================
    public void xoaTheoId(String id) throws IOException {
        List<Auction> ds = loadAll();
        ds.removeIf(a -> a.getId().equals(id));
        saveAll(ds);
    }

    // =========================================================================
    // timTheoId - Tìm Auction theo id, trả về null nếu không thấy
    // =========================================================================
    public Auction timTheoId(String id) throws IOException {
        for (Auction a : loadAll()) {
            if (a.getId().equals(id))
                return a;
        }
        System.out.println("Không tìm thấy auction với id: " + id);
        return null;
    }

    // =========================================================================
    // timTheoSeller - Lấy tất cả auction của một seller
    // =========================================================================
    public List<Auction> timTheoSeller(String sellerId) throws IOException {
        List<Auction> result = new ArrayList<>();
        for (Auction a : loadAll()) {
            if (a.getSellerId().equals(sellerId))
                result.add(a);
        }
        return result;
    }

    // =========================================================================
    // timTheoTrangThai - Lấy tất cả auction theo trạng thái
    // =========================================================================
    public List<Auction> timTheoTrangThai(AuctionStatus status) throws IOException {
        List<Auction> result = new ArrayList<>();
        for (Auction a : loadAll()) {
            if (a.getStatus() == status)
                result.add(a);
        }
        return result;
    }

    // =========================================================================
    // Private helpers - nhất quán với ItemDAO / UserDAO
    // =========================================================================

    /** Chuyển một Auction thành chuỗi JSON. */
    private String auctionToJson(Auction a) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("\"id\": \"" + a.getId() + "\",\n");
        sb.append("\"itemId\": \"" + a.getItemId() + "\",\n");
        sb.append("\"sellerId\": \"" + a.getSellerId() + "\",\n");
        sb.append("\"startTime\": \"" + a.getStartTime().format(FORMATTER) + "\",\n");
        sb.append("\"endTime\": \"" + a.getEndTime().format(FORMATTER) + "\",\n");
        sb.append("\"startingPrice\": " + a.getStartingPrice() + ",\n");
        sb.append("\"currentHighestBid\": " + a.getCurrentHighestBid() + ",\n");
        // highestBidderId có thể null → lưu là chuỗi "null"
        sb.append("\"highestBidderId\": \"" + (a.getHighestBidderId() == null ? "null" : a.getHighestBidderId()) + "\",\n");
        sb.append("\"minimumBidIncrement\": " + a.getMinimumBidIncrement() + ",\n");
        sb.append("\"status\": \"" + a.getStatus().name() + "\"\n");
        sb.append("}");
        return sb.toString();
    }

    /** Tách chuỗi JSON mảng thành danh sách object { ... } — giống hệt ItemDAO. */
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

    /** Lấy giá trị chuỗi hoặc số của một trường JSON — giống hệt ItemDAO. */
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
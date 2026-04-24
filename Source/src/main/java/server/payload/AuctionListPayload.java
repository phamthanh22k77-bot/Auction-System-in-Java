package server.payload;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

// Payload chứa danh sách auction để server gửi về client
// Controller sẽ đọc class này để hiển thị danh sách auction
public class AuctionListPayload implements Serializable {

    // ===== Attributes =====

    // Danh sách các auction item
    // 👉 Controller sẽ duyệt list này và in ra từng auction:
    //    - auctionID
    //    - itemName
    //    - itemStartingPrice
    //    - highestBid
    private LinkedList<AuctionListItem> auctionList;

    // ===== Constructor =====

    // Khởi tạo payload với danh sách auction
    public AuctionListPayload(LinkedList<AuctionListItem> auctionList) {
        this.auctionList = auctionList;
    }

    // ===== Getter & Setter =====

    // Controller gọi hàm này để lấy danh sách auction
    // 👉 Sau đó sẽ loop qua từng phần tử để in thông tin chi tiết
    public LinkedList<AuctionListItem> getAuctionList() {
        return auctionList;
    }

    public void setAuctionList(LinkedList<AuctionListItem> auctionList) {
        this.auctionList = auctionList;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        AuctionListPayload that = (AuctionListPayload) o;

        // So sánh 2 payload có cùng danh sách auction không
        return Objects.equals(auctionList, that.auctionList);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên danh sách auction
        return Objects.hash(auctionList);
    }

    @Override
    public String toString() {
        // 👉 Nếu controller in trực tiếp object này:
        // System.out.println(payload);
        // → sẽ in toàn bộ danh sách auction (dạng raw)
        return "AuctionListPayload{" +
                "auctionList=" + auctionList +
                '}';
    }
}
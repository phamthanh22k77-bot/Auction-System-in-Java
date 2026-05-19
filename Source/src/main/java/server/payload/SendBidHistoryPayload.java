package server.payload;

import java.io.Serializable;
import java.util.List;
import server.models.auction.BidTransaction;
import server.models.item.Item;

// Payload chứa thông tin lịch sử đấu giá và chi tiết cấu hình của vật phẩm. Phục vụ cho giao tiếp không đồng bộ mạng thay vì gọi DAO trực tiếp từ Client.

public class SendBidHistoryPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionID;
    private List<BidTransaction> bidHistory;
    private Item item;

    public SendBidHistoryPayload(String auctionID, List<BidTransaction> bidHistory, Item item) {
        this.auctionID = auctionID;
        this.bidHistory = bidHistory;
        this.item = item;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidTransaction> bidHistory) {
        this.bidHistory = bidHistory;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "SendBidHistoryPayload{" + "auctionID='" + auctionID + '\'' + ", historySize="
                + (bidHistory != null ? bidHistory.size() : 0) + ", item=" + (item != null ? item.getName() : "null")
                + '}';
    }
}

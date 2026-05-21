package server.payload;

import java.io.Serializable;
import java.util.List;
import server.models.auction.BidTransaction;

// Payload dùng để xác nhận việc đăng ký tham gia auction. Chứa thêm lịch sử và trạng thái hiện tại để đồng bộ UI ngay khi vào phòng.

public class ConfirmAuctionRegistrationPayload implements Serializable {

    private String auctionID;
    private double currentPrice;
    private String highestBidderId;
    private List<BidTransaction> bidHistory;

    public ConfirmAuctionRegistrationPayload(String auctionID) {
        this.auctionID = auctionID;
    }

    public ConfirmAuctionRegistrationPayload(String auctionID, double currentPrice, String highestBidderId,
                                             List<BidTransaction> bidHistory) {
        this.auctionID = auctionID;
        this.currentPrice = currentPrice;
        this.highestBidderId = highestBidderId;
        this.bidHistory = bidHistory;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(String highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidTransaction> bidHistory) {
        this.bidHistory = bidHistory;
    }

    @Override
    public String toString() {
        return "ConfirmAuctionRegistrationPayload{" + "auctionID='" + auctionID + '\'' + ", currentPrice="
                + currentPrice + ", highestBidderId='" + highestBidderId + '\'' + ", historySize="
                + (bidHistory != null ? bidHistory.size() : 0) + '}';
    }
}

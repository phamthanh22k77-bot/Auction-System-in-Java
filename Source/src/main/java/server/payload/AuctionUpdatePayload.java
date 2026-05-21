package server.payload;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Class dùng để đóng gói dữ liệu truyền mạng khi phiên đấu giá có cập nhật lượt đặt giá mới
public class AuctionUpdatePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auctionID;
    private LocalDateTime bidCreationDate; // Thời điểm đặt giá mới nhất
    private double highestBid;
    private String itemName;
    private String highestBidderIP;  // Người/IP đang giữ giá cao nhất
    private String itemDescription;
    private LocalDateTime endTime;   // Thời gian kết thúc
    private int antiSnipeCount;      // Số lần đã tự động gia hạn đấu giá

    public AuctionUpdatePayload(String auctionID, LocalDateTime bidCreationDate, double highestBid, String itemName,
                                String highestBidderIP, String itemDescription, LocalDateTime endTime, int antiSnipeCount) {
        this.auctionID = auctionID;
        this.bidCreationDate = bidCreationDate;
        this.highestBid = highestBid;
        this.itemName = itemName;
        this.highestBidderIP = highestBidderIP;
        this.itemDescription = itemDescription;
        this.endTime = endTime;
        this.antiSnipeCount = antiSnipeCount;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public LocalDateTime getBidCreationDate() {
        return bidCreationDate;
    }

    public void setBidCreationDate(LocalDateTime bidCreationDate) {
        this.bidCreationDate = bidCreationDate;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getAntiSnipeCount() {
        return antiSnipeCount;
    }

    public void setAntiSnipeCount(int antiSnipeCount) {
        this.antiSnipeCount = antiSnipeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuctionUpdatePayload that = (AuctionUpdatePayload) o;
        return antiSnipeCount == that.antiSnipeCount &&
                Double.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(auctionID, that.auctionID) &&
                Objects.equals(bidCreationDate, that.bidCreationDate) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(highestBidderIP, that.highestBidderIP) &&
                Objects.equals(itemDescription, that.itemDescription) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, bidCreationDate, highestBid, itemName,
                highestBidderIP, itemDescription, endTime, antiSnipeCount);
    }

    @Override
    public String toString() {
        return "AuctionUpdatePayload{" +
                "auctionID='" + auctionID + '\'' +
                ", bidCreationDate=" + bidCreationDate +
                ", highestBid=" + highestBid +
                ", itemName='" + itemName + '\'' +
                ", highestBidderIP='" + highestBidderIP + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", endTime=" + endTime +
                ", antiSnipeCount=" + antiSnipeCount +
                '}';
    }
}

package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Lớp đại diện cho 1 phiên đấu giá trong danh sách truyền tải qua Socket
public class AuctionListItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auctionID;
    private double itemStartingPrice;
    private String itemName;
    private String itemDescription;
    private String auctionOwnerIP;
    private double highestBid;
    private String category;
    private String startTime;
    private String endTime;
    private String status;
    private String highestBidderId;

    public AuctionListItem(String auctionID, double itemStartingPrice, String itemName, String itemDescription,
                           String auctionOwnerIP, double highestBid, String category, String startTime, String endTime, String status,
                           String highestBidderId) {
        this.auctionID = auctionID;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionOwnerIP = auctionOwnerIP;
        this.highestBid = highestBid;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.highestBidderId = highestBidderId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public double getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(double itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getAuctionOwnerIP() {
        return auctionOwnerIP;
    }

    public void setAuctionOwnerIP(String auctionOwnerIP) {
        this.auctionOwnerIP = auctionOwnerIP;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(String highestBidderId) {
        this.highestBidderId = highestBidderId;
    }


    // So sánh 2 đối tượng đồng nhất dựa trên TẤT CẢ các thuộc tính dữ liệu
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuctionListItem that = (AuctionListItem) o;

        return Double.compare(that.itemStartingPrice, itemStartingPrice) == 0 &&
                Double.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(auctionID, that.auctionID) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(itemDescription, that.itemDescription) &&
                Objects.equals(auctionOwnerIP, that.auctionOwnerIP) &&
                Objects.equals(category, that.category) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(highestBidderId, that.highestBidderId);
    }

    // Đồng bộ mã băm (hashCode) với equals để tránh lỗi khi dùng Map/Set
    @Override
    public int hashCode() {
        return Objects.hash(auctionID, itemStartingPrice, itemName, itemDescription,
                auctionOwnerIP, highestBid, category, endTime, status, highestBidderId);
    }

    // Định dạng chuỗi đại diện hiển thị đầy đủ thông tin nhất để phục vụ in log debug
    @Override
    public String toString() {
        return "AuctionListItem{" +
                "auctionID='" + auctionID + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", itemStartingPrice=" + itemStartingPrice +
                ", highestBid=" + highestBid +
                ", highestBidderId='" + highestBidderId + '\'' +
                ", status='" + status + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}

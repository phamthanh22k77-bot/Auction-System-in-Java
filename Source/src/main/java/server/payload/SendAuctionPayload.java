package server.payload;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// Payload dùng để gửi TOÀN BỘ thông tin của một auction từ server → client
// 👉 Đây là payload "full data"
// 👉 Client dùng để hiển thị chi tiết auction (UI, màn hình detail, realtime update,...)
public class SendAuctionPayload implements Serializable {

    // ===== Attributes =====

    // ID của auction
    // 👉 Dùng để định danh duy nhất auction
    private int auctionID;

    // Loại auction (NORMAL, DUTCH,...)
    // 👉 Client có thể hiển thị hoặc xử lý UI khác nhau
    private String auctionType;

    // Thời điểm tạo auction
    private Date auctionCreationDate;

    // Thời điểm kết thúc auction
    private Date auctionTerminationDate;

    // Thời điểm bid gần nhất được tạo
    // 👉 Dùng để hiển thị realtime hoặc lịch sử
    private Date bidCreationDate;

    // Giá cao nhất hiện tại
    private float highestBid;

    // Giá khởi điểm
    private float itemStartingPrice;

    // Tên item
    private String itemName;

    // Mô tả item
    private String itemDescription;

    // IP của người tạo auction
    // 👉 Có thể dùng để phân quyền hoặc hiển thị owner
    private String auctionOwnerIP;

    // IP của người đang giữ bid cao nhất
    private String highestBidderIP;

    // ===== Constructor =====

    // Khởi tạo payload với đầy đủ thông tin auction
    public SendAuctionPayload(int auctionID, String auctionType, Date auctionCreationDate,
                              Date auctionTerminationDate, Date bidCreationDate,
                              float highestBid, float itemStartingPrice,
                              String itemName, String itemDescription,
                              String auctionOwnerIP, String highestBidderIP) {

        this.auctionID = auctionID;
        this.auctionType = auctionType;
        this.auctionCreationDate = auctionCreationDate;
        this.auctionTerminationDate = auctionTerminationDate;
        this.bidCreationDate = bidCreationDate;
        this.highestBid = highestBid;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionOwnerIP = auctionOwnerIP;
        this.highestBidderIP = highestBidderIP;
    }

    // ===== Getter & Setter =====

    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public Date getAuctionCreationDate() {
        return auctionCreationDate;
    }

    public void setAuctionCreationDate(Date auctionCreationDate) {
        this.auctionCreationDate = auctionCreationDate;
    }

    public Date getAuctionTerminationDate() {
        return auctionTerminationDate;
    }

    public void setAuctionTerminationDate(Date auctionTerminationDate) {
        this.auctionTerminationDate = auctionTerminationDate;
    }

    public Date getBidCreationDate() {
        return bidCreationDate;
    }

    public void setBidCreationDate(Date bidCreationDate) {
        this.bidCreationDate = bidCreationDate;
    }

    public float getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    public float getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(float itemStartingPrice) {
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

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        SendAuctionPayload that = (SendAuctionPayload) o;

        // So sánh toàn bộ dữ liệu auction
        return auctionID == that.auctionID &&
                Float.compare(that.highestBid, highestBid) == 0 &&
                Float.compare(that.itemStartingPrice, itemStartingPrice) == 0 &&
                Objects.equals(auctionType, that.auctionType) &&
                Objects.equals(auctionCreationDate, that.auctionCreationDate) &&
                Objects.equals(auctionTerminationDate, that.auctionTerminationDate) &&
                Objects.equals(bidCreationDate, that.bidCreationDate) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(itemDescription, that.itemDescription) &&
                Objects.equals(auctionOwnerIP, that.auctionOwnerIP) &&
                Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên toàn bộ field
        return Objects.hash(auctionID, auctionType, auctionCreationDate,
                auctionTerminationDate, bidCreationDate,
                highestBid, itemStartingPrice,
                itemName, itemDescription,
                auctionOwnerIP, highestBidderIP);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in toàn bộ thông tin auction
        return "SendAuctionPayload{" +
                "auctionID=" + auctionID +
                ", auctionType='" + auctionType + '\'' +
                ", auctionCreationDate=" + auctionCreationDate +
                ", auctionTerminationDate=" + auctionTerminationDate +
                ", bidCreationDate=" + bidCreationDate +
                ", highestBid=" + highestBid +
                ", itemStartingPrice=" + itemStartingPrice +
                ", itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", auctionOwnerIP='" + auctionOwnerIP + '\'' +
                ", highestBidderIP='" + highestBidderIP + '\'' +
                '}';
    }
}
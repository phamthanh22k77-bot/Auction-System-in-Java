package server.payload;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

//Class này dùng để đóng gói dữ liệu khi phiên đấu giá update thêm lượt đấu giá
public class AuctionUpdatePayload implements Serializable {

    //Attributes
    private int auctionID;
    // 👉 Controller dùng để xác định auction nào cần update UI

    private Date bidCreationDate;
    // 👉 Controller dùng để hiển thị thời điểm đặt giá mới nhất

    private float highestBid;
    // 👉 Controller dùng để hiển thị giá cao nhất hiện tại

    private String itemName;
    // 👉 Controller dùng để hiển thị tên item

    private String highestBidderIP;
    // 👉 Controller dùng để hiển thị người đang giữ giá cao nhất

    private String itemDescription;
    // 👉 Controller có thể dùng để hiển thị mô tả item

    //Constructors
    public AuctionUpdatePayload(int auctionID, Date bidCreationDate, float highestBid, String itemName, String highestBidderIP, String itemDescription) {
        this.auctionID = auctionID;
        this.bidCreationDate = bidCreationDate;
        this.highestBid = highestBid;
        this.itemName = itemName;
        this.highestBidderIP = highestBidderIP;
        this.itemDescription = itemDescription;
    }

    //Setters and Getters
    public int getAuctionID() {
        return auctionID;
        // 👉 Controller gọi để biết cần update auction nào
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    public Date getBidCreationDate() {
        return bidCreationDate;
        // 👉 Controller dùng để hiển thị thời gian bid mới nhất
    }

    public void setBidCreationDate(Date bidCreationDate) {
        this.bidCreationDate = bidCreationDate;
    }

    public float getHighestBid() {
        return highestBid;
        // 👉 Controller dùng để cập nhật giá cao nhất trên UI
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    public String getItemName() {
        return itemName;
        // 👉 Controller hiển thị tên item
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
        // 👉 Controller hiển thị người đang dẫn đầu
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    public String getItemDescription() {
        return itemDescription;
        // 👉 Controller hiển thị mô tả nếu cần
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    //Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionUpdatePayload that = (AuctionUpdatePayload) o;
        return auctionID == that.auctionID &&
                Float.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(bidCreationDate, that.bidCreationDate) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(highestBidderIP, that.highestBidderIP) &&
                Objects.equals(itemDescription, that.itemDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, bidCreationDate, highestBid, itemName, highestBidderIP, itemDescription);
    }

    @Override
    public String toString() {
        // 👉 Controller có thể log payload này để debug khi nhận update từ server
        return "AuctionUpdatePayload{" +
                "auctionID=" + auctionID +
                ", createdAt=" + bidCreationDate +
                ", bidPrice=" + highestBid +
                ", itemName='" + itemName + '\'' +
                ", highestBidderIP='" + highestBidderIP + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                '}';
    }
}
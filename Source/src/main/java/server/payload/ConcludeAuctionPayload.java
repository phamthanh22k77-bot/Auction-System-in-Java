package server.payload;

import java.io.Serializable;

//Class này dùng để đóng gói dữ liệu khi phiên đấu giá đã được kết thúc
public class ConcludeAuctionPayload implements Serializable {

    //Attributes
    private int auctionID;
    // 👉 Controller dùng để xác định auction nào đã kết thúc

    private float highestBid;
    // 👉 Controller dùng để hiển thị giá thắng cuối cùng

    private String itemName;
    // 👉 Controller dùng để hiển thị tên item đã đấu giá

    private String highestBidderIP;
    // 👉 Controller dùng để hiển thị người thắng (winner)

    //Constructors
    public ConcludeAuctionPayload(int auctionID, float highestBid, String itemName, String highestBidderIP) {
        this.auctionID = auctionID;
        this.highestBid = highestBid;
        this.itemName = itemName;
        this.highestBidderIP = highestBidderIP;
    }

    //Setters and Getters
    public int getAuctionID() {
        return auctionID;
        // 👉 Controller gọi để xác định auction cần update trạng thái (kết thúc)
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    public float getHighestBid() {
        return highestBid;
        // 👉 Controller dùng để hiển thị giá thắng cuối cùng
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
        // 👉 Controller hiển thị người thắng (winner)
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    //Methods
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        ConcludeAuctionPayload that = (ConcludeAuctionPayload) object;
        return Float.compare(that.highestBid, highestBid) == 0 &&
                java.util.Objects.equals(auctionID, that.auctionID) &&
                java.util.Objects.equals(itemName, that.itemName) &&
                java.util.Objects.equals(highestBidderIP, that.highestBidderIP);
        // 👉 So sánh 2 payload dựa trên toàn bộ dữ liệu (dùng trong backend)
    }

    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), auctionID, highestBid, itemName, highestBidderIP);
        // 👉 Dùng trong HashMap/HashSet
    }

    @Override
    public String toString() {
        // 👉 Controller có thể log payload khi nhận kết quả auction
        return "ConcludeAuctionPayload{" +
                "auctionID=" + auctionID +
                ", biggestBid=" + highestBid +
                ", itemName='" + itemName + '\'' +
                ", WinnerIpAddress='" + highestBidderIP + '\'' +
                '}';
    }
}
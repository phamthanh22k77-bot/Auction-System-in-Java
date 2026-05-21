package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Lớp này dùng để đóng gói dữ liệu khi phiên đấu giá đã được kết thúc thành công
public class ConcludeAuctionPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auctionID;        // ID phiên đấu giá đã kết thúc
    private double highestBid;       // Mức giá thắng cuối cùng
    private String itemName;         // Tên vật phẩm đã đấu giá
    private String highestBidderIP;  // ID/IP người thắng cuộc (winner)

    public ConcludeAuctionPayload(String auctionID, double highestBid, String itemName, String highestBidderIP) {
        this.auctionID = auctionID;
        this.highestBid = highestBid;
        this.itemName = itemName;
        this.highestBidderIP = highestBidderIP;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
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

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        ConcludeAuctionPayload that = (ConcludeAuctionPayload) object;

        // So sánh chính xác dựa trên giá trị thực tế của các thuộc tính
        return Double.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(auctionID, that.auctionID) &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        // Tạo mã băm chuẩn xác dựa trên các thuộc tính của đối tượng
        return Objects.hash(auctionID, highestBid, itemName, highestBidderIP);
    }

    @Override
    public String toString() {
        return "ConcludeAuctionPayload{" +
                "auctionID='" + auctionID + '\'' +
                ", highestBid=" + highestBid +
                ", itemName='" + itemName + '\'' +
                ", highestBidderIP='" + highestBidderIP + '\'' +
                '}';
    }
}

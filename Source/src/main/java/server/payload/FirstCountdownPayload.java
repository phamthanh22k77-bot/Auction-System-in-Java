package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi thông tin bắt đầu countdown của auction
// Server gửi payload này khi auction chuẩn bị bắt đầu
public class FirstCountdownPayload implements Serializable {

    private String auctionID;
    private String itemName;
    private double highestBid;

    public FirstCountdownPayload(String auctionID, String itemName, double highestBid) {
        this.auctionID = auctionID;
        this.itemName = itemName;
        this.highestBid = highestBid;
    }



    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }


    @Override
    public String toString() {
        return "FirstCountdownPayload{" +
                "auctionID='" + auctionID + '\'' +
                ", itemName='" + itemName + '\'' +
                ", highestBid=" + highestBid +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FirstCountdownPayload that = (FirstCountdownPayload) o;

        // So sánh dựa trên auctionID
        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }
}

package server.payload;

import java.io.Serializable;
import java.util.Objects;

public class NotifyAuctionWinnerPayload implements Serializable {

    private String auctionID;
    private double highestBid;
    private String itemName;

    public NotifyAuctionWinnerPayload(String auctionID, double highestBid, String itemName) {
        this.auctionID = auctionID;
        this.highestBid = highestBid;
        this.itemName = itemName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NotifyAuctionWinnerPayload that = (NotifyAuctionWinnerPayload) o;
        return Objects.equals(auctionID, that.auctionID) && Double.compare(that.highestBid, highestBid) == 0
                && Objects.equals(itemName, that.itemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, highestBid, itemName);
    }

    @Override
    public String toString() {
        return "NotifyAuctionWinnerPayload{" + "auctionID=" + auctionID + ", highestBid=" + highestBid + ", itemName='"
                + itemName + '\'' + '}';
    }
}

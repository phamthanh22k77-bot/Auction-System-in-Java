package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi hành động đặt giá (bid) từ client → server
public class MakeBidPayload implements Serializable {

    private String auctionID;
    private double highestBid;

    public MakeBidPayload(String auctionID, double highestBid) {
        this.auctionID = auctionID;
        this.highestBid = highestBid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MakeBidPayload that = (MakeBidPayload) o;

        // So sánh cả auctionID và giá bid
        return Objects.equals(auctionID, that.auctionID) && Double.compare(that.highestBid, highestBid) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, highestBid);
    }

    @Override
    public String toString() {
        return "MakeBidPayload{" + "auctionID='" + auctionID + '\'' + ", highestBid=" + highestBid + '}';
    }
}

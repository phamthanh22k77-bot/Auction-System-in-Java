package server.payload;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Payload dùng để gửi thông tin giá bid cao nhất từ server → client
public class SendHighestBidPayload implements Serializable {

    private LocalDateTime bidCreationTime;
    private double highestBid;
    private String highestBidderIP;
    private String auctionID;

    public SendHighestBidPayload(LocalDateTime bidCreationTime, double highestBid, String highestBidderIP,
            String auctionID) {
        this.bidCreationTime = bidCreationTime;
        this.highestBid = highestBid;
        this.highestBidderIP = highestBidderIP;
        this.auctionID = auctionID;
    }

    public LocalDateTime getBidCreationTime() {
        return bidCreationTime;
    }

    public void setBidCreationTime(LocalDateTime bidCreationTime) {
        this.bidCreationTime = bidCreationTime;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SendHighestBidPayload that = (SendHighestBidPayload) o;

        return Double.compare(that.highestBid, highestBid) == 0 && Objects.equals(auctionID, that.auctionID)
                && Objects.equals(bidCreationTime, that.bidCreationTime)
                && Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bidCreationTime, highestBid, highestBidderIP, auctionID);
    }

    @Override
    public String toString() {

        return "SendHighestBidPayload{" + "bidCreationDate=" + bidCreationTime + ", bid=" + highestBid + ", bidderIP='"
                + highestBidderIP + '\'' + ", auctionID=" + auctionID + '}';
    }
}

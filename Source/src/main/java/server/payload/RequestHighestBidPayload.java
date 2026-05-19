package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để yêu cầu lấy giá bid cao nhất từ client → server
public class RequestHighestBidPayload implements Serializable {

    private String auctionID;

    public RequestHighestBidPayload(String auctionID) {
        this.auctionID = auctionID;
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

        RequestHighestBidPayload that = (RequestHighestBidPayload) o;

        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        return "RequestHighestBidPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }
}

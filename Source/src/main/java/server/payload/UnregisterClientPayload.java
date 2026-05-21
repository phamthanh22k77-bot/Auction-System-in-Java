package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để client rời khỏi (unregister) một auction
public class UnregisterClientPayload implements Serializable {

    private String auctionID;

    public UnregisterClientPayload(String auctionID) {
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

        UnregisterClientPayload that = (UnregisterClientPayload) o;

        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        return "UnregisterClientPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }
}

package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để gửi tín hiệu countdown giai đoạn 2 của auction
public class SecondCountdownPayload implements Serializable {

    private String auctionID;

    public SecondCountdownPayload(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    @Override
    public String toString() {
        return "SecondCountdownPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SecondCountdownPayload that = (SecondCountdownPayload) o;

        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }
}

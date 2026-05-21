package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Class được dùng để đóng gói thông tin khi nâng giá thành công trong một phiên đấu giá
public class ConfirmAuctionBidPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auctionID; // ID phiên đấu giá mà bid được chấp nhận

    public ConfirmAuctionBidPayload(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfirmAuctionBidPayload that = (ConfirmAuctionBidPayload) o;
        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        return "ConfirmAuctionBidPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }
}

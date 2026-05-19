package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Class dùng để đóng gói dữ liệu khi xác nhận hủy một phiên đấu giá
public class ConfirmAuctionCancellationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auctionID; // ID phiên đấu giá đã được hủy thành công

    public ConfirmAuctionCancellationPayload(String auctionID) {
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
        ConfirmAuctionCancellationPayload that = (ConfirmAuctionCancellationPayload) o;
        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        return "ConfirmAuctionCancellationPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }
}

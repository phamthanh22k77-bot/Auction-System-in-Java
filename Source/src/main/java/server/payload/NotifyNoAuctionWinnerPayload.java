package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Payload dùng để thông báo auction kết thúc nhưng KHÔNG có người thắng
public class NotifyNoAuctionWinnerPayload implements Serializable {

    private String auctionID;
    private String itemName;
    private double itemStartingPrice;

    public NotifyNoAuctionWinnerPayload(String auctionID, String itemName, double itemStartingPrice) {
        this.auctionID = auctionID;
        this.itemName = itemName;
        this.itemStartingPrice = itemStartingPrice;
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

    public double getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(double itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NotifyNoAuctionWinnerPayload that = (NotifyNoAuctionWinnerPayload) o;

        return Objects.equals(auctionID, that.auctionID)
                && Double.compare(that.itemStartingPrice, itemStartingPrice) == 0
                && Objects.equals(itemName, that.itemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, itemName, itemStartingPrice);
    }

    @Override
    public String toString() {
        return "NotifyNoAuctionWinnerPayload{" + "auctionID='" + auctionID + '\'' + ", itemName='" + itemName + '\''
                + ", itemStartingPrice=" + itemStartingPrice + '}';
    }
}

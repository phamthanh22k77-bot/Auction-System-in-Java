package server.payload;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// Payload dùng để gửi TOÀN BỘ thông tin của một auction từ server → client
public class SendAuctionPayload implements Serializable {

    private String auctionID;
    private String auctionType;
    private Date auctionCreationDate;
    private Date auctionTerminationDate;
    private Date bidCreationDate;
    private double highestBid;
    private double itemStartingPrice;
    private String itemName;
    private String itemDescription;
    private String auctionOwnerIP;
    private String highestBidderIP;

    public SendAuctionPayload(String auctionID, String auctionType, Date auctionCreationDate,
            Date auctionTerminationDate, Date bidCreationDate, double highestBid, double itemStartingPrice,
            String itemName, String itemDescription, String auctionOwnerIP, String highestBidderIP) {

        this.auctionID = auctionID;
        this.auctionType = auctionType;
        this.auctionCreationDate = auctionCreationDate;
        this.auctionTerminationDate = auctionTerminationDate;
        this.bidCreationDate = bidCreationDate;
        this.highestBid = highestBid;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionOwnerIP = auctionOwnerIP;
        this.highestBidderIP = highestBidderIP;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public Date getAuctionCreationDate() {
        return auctionCreationDate;
    }

    public void setAuctionCreationDate(Date auctionCreationDate) {
        this.auctionCreationDate = auctionCreationDate;
    }

    public Date getAuctionTerminationDate() {
        return auctionTerminationDate;
    }

    public void setAuctionTerminationDate(Date auctionTerminationDate) {
        this.auctionTerminationDate = auctionTerminationDate;
    }

    public Date getBidCreationDate() {
        return bidCreationDate;
    }

    public void setBidCreationDate(Date bidCreationDate) {
        this.bidCreationDate = bidCreationDate;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public double getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(double itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getAuctionOwnerIP() {
        return auctionOwnerIP;
    }

    public void setAuctionOwnerIP(String auctionOwnerIP) {
        this.auctionOwnerIP = auctionOwnerIP;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SendAuctionPayload that = (SendAuctionPayload) o;

        return Objects.equals(auctionID, that.auctionID) && Double.compare(that.highestBid, highestBid) == 0
                && Double.compare(that.itemStartingPrice, itemStartingPrice) == 0
                && Objects.equals(auctionType, that.auctionType)
                && Objects.equals(auctionCreationDate, that.auctionCreationDate)
                && Objects.equals(auctionTerminationDate, that.auctionTerminationDate)
                && Objects.equals(bidCreationDate, that.bidCreationDate) && Objects.equals(itemName, that.itemName)
                && Objects.equals(itemDescription, that.itemDescription)
                && Objects.equals(auctionOwnerIP, that.auctionOwnerIP)
                && Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID, auctionType, auctionCreationDate, auctionTerminationDate, bidCreationDate,
                highestBid, itemStartingPrice, itemName, itemDescription, auctionOwnerIP, highestBidderIP);
    }

    @Override
    public String toString() {
        return "SendAuctionPayload{" + "auctionID=" + auctionID + ", auctionType='" + auctionType + '\''
                + ", auctionCreationDate=" + auctionCreationDate + ", auctionTerminationDate=" + auctionTerminationDate
                + ", bidCreationDate=" + bidCreationDate + ", highestBid=" + highestBid + ", itemStartingPrice="
                + itemStartingPrice + ", itemName='" + itemName + '\'' + ", itemDescription='" + itemDescription + '\''
                + ", auctionOwnerIP='" + auctionOwnerIP + '\'' + ", highestBidderIP='" + highestBidderIP + '\'' + '}';
    }
}

package server.payload;

import java.io.Serializable;
import java.util.Objects;


// Class này đại diện cho 1 item trong danh sách auction (dùng để hiển thị list cho client)
// Implement Serializable để gửi qua mạng (socket)
public class AuctionListItem implements Serializable {

    // ===== Attributes =====

    // ID của auction
    private int auctionID;

    // Giá khởi điểm của item
    private float itemStartingPrice;

    // Tên item
    private String itemName;

    // Mô tả item
    private String itemDescription;

    // IP của người tạo auction
    private String auctionOwnerIP;

    // Giá bid cao nhất hiện tại
    private float highestBid;

    // ===== Constructor =====

    // Khởi tạo đầy đủ thông tin của 1 auction item
    public AuctionListItem(int auctionID, float itemStartingPrice, String itemName,
                           String itemDescription, String auctionOwnerIP, float highestBid) {
        this.auctionID = auctionID;
        this.itemStartingPrice = itemStartingPrice;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.auctionOwnerIP = auctionOwnerIP;
        this.highestBid = highestBid;
    }

    // ===== Getter & Setter =====

    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    public float getItemStartingPrice() {
        return itemStartingPrice;
    }

    public void setItemStartingPrice(float itemStartingPrice) {
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

    public float getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    // ===== Methods =====

    // So sánh 2 object có giống nhau không
    // → phải giống toàn bộ thuộc tính
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false; // khác kiểu

        AuctionListItem that = (AuctionListItem) o;

        return auctionID == that.auctionID && // so ID
                Float.compare(that.itemStartingPrice, itemStartingPrice) == 0 && // so float an toàn
                Float.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(itemName, that.itemName) && // so String
                Objects.equals(itemDescription, that.itemDescription) &&
                Objects.equals(auctionOwnerIP, that.auctionOwnerIP);
    }

    // HashCode dùng cho HashMap, HashSet
    // Phải đồng bộ với equals
    @Override
    public int hashCode() {
        return Objects.hash(auctionID, itemStartingPrice, itemName,
                itemDescription, auctionOwnerIP, highestBid);
    }

    // Dùng debug/log → in object ra dạng chuỗi
    @Override
    public String toString() {
        return "AuctionListItem{" +
                "auctionID=" + auctionID +
                ", itemStartingPrice=" + itemStartingPrice +
                ", itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", auctionOwnerIP='" + auctionOwnerIP + '\'' +
                ", highestBid=" + highestBid +
                '}';
    }
}
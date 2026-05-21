package server.payload;

import java.io.Serializable;
import java.util.Objects;

// Class này dùng để đóng gói dữ liệu khi client gửi request tham gia auction
// Implement Serializable để có thể gửi qua socket (ObjectOutputStream)
public class RegisterClientPayload implements Serializable {

    private String auctionID;


    public RegisterClientPayload(String auctionID) {
        this.auctionID = auctionID;
    }


    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getAuctionID() {
        return auctionID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // cùng vùng nhớ → chắc chắn bằng
        if (o == null || getClass() != o.getClass())
            return false; // khác kiểu → không bằng
        RegisterClientPayload that = (RegisterClientPayload) o;
        return Objects.equals(auctionID, that.auctionID); // so sánh theo auctionID
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

    @Override
    public String toString() {
        return "RegisterClientPayload{" + "auctionID='" + auctionID + '\'' + '}';
    }
}

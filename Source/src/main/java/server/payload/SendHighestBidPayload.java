package server.payload;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// Payload dùng để gửi thông tin giá bid cao nhất từ server → client
// 👉 Server gửi mỗi khi có bid mới hoặc khi client request cập nhật
// 👉 Client dùng để update realtime UI (giá, người dẫn đầu,...)
public class SendHighestBidPayload implements Serializable {

    // ===== Attributes =====

    // Thời điểm bid mới nhất được tạo
    // 👉 Dùng để hiển thị thời gian cập nhật gần nhất
    private Date bidCreationDate;

    // Giá cao nhất hiện tại
    // 👉 Hiển thị cho user biết mức giá đang dẫn đầu
    private float highestBid;

    // IP của người đang giữ giá cao nhất
    // 👉 Có thể dùng để:
    //    - Xác định "you are leading"
    //    - Hiển thị người thắng tạm thời
    private String highestBidderIP;

    // ID của auction
    // 👉 Dùng để xác định auction nào đang được update
    private int auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với thông tin bid mới nhất
    public SendHighestBidPayload(Date bidCreationDate, float highestBid,
                                 String highestBidderIP, int auctionID) {
        this.bidCreationDate = bidCreationDate;
        this.highestBid = highestBid;
        this.highestBidderIP = highestBidderIP;
        this.auctionID = auctionID;
    }

    // ===== Getter & Setter =====

    public Date getBidCreationDate() {
        return bidCreationDate;
    }

    public void setBidCreationDate(Date bidCreationDate) {
        this.bidCreationDate = bidCreationDate;
    }

    public float getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(float highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    public int getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        SendHighestBidPayload that = (SendHighestBidPayload) o;

        // So sánh toàn bộ thông tin bid
        return Float.compare(that.highestBid, highestBid) == 0 &&
                auctionID == that.auctionID &&
                Objects.equals(bidCreationDate, that.bidCreationDate) &&
                Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên các field
        return Objects.hash(bidCreationDate, highestBid, highestBidderIP, auctionID);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in thông tin bid cao nhất hiện tại
        return "SendHighestBidPayload{" +
                "bidCreationDate=" + bidCreationDate +
                ", bid=" + highestBid +
                ", bidderIP='" + highestBidderIP + '\'' +
                ", auctionID=" + auctionID +
                '}';
    }
}
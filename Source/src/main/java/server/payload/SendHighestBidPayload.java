package server.payload;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

// Payload dùng để gửi thông tin giá bid cao nhất từ server → client
// 👉 Server gửi mỗi khi có bid mới hoặc khi client request cập nhật
// 👉 Client dùng để update realtime UI (giá, người dẫn đầu,...)
public class SendHighestBidPayload implements Serializable {

    // ===== Attributes =====

    // Thời điểm bid mới nhất được tạo
    // 👉 Dùng để hiển thị thời gian cập nhật gần nhất
    private LocalDateTime bidCreationTime;

    // Giá cao nhất hiện tại
    // 👉 Hiển thị cho user biết mức giá đang dẫn đầu
    private double highestBid;

    // IP của người đang giữ giá cao nhất
    // 👉 Có thể dùng để:
    //    - Xác định "you are leading"
    //    - Hiển thị người thắng tạm thời
    private String highestBidderIP;

    // ID của auction
    // 👉 Dùng để xác định auction nào đang được update
    private String auctionID;

    // ===== Constructor =====

    // Khởi tạo payload với thông tin bid mới nhất
    public SendHighestBidPayload(LocalDateTime bidCreationTime, double highestBid,
                                 String highestBidderIP, String auctionID) {
        this.bidCreationTime = bidCreationTime;
        this.highestBid = highestBid;
        this.highestBidderIP = highestBidderIP;
        this.auctionID = auctionID;
    }

    // ===== Getter & Setter =====

    public Date getBidCreationTime() {
        return bidCreationTime;
    }

    public void setBidCreationTime(LocalDateTime bidCreationTime) {
        this.bidCreationTime = bidCreationTime;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidderIP() {
        return highestBidderIP;
    }

    public void setHighestBidderIP(String highestBidderIP) {
        this.highestBidderIP = highestBidderIP;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    // ===== Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object
        if (o == null || getClass() != o.getClass()) return false;

        SendHighestBidPayload that = (SendHighestBidPayload) o;

        // So sánh toàn bộ thông tin bid
        return Double.compare(that.highestBid, highestBid) == 0 &&
                Objects.equals(auctionID, that.auctionID) &&
                Objects.equals(bidCreationTime, that.bidCreationTime) &&
                Objects.equals(highestBidderIP, that.highestBidderIP);
    }

    @Override
    public int hashCode() {
        // Hash dựa trên các field
        return Objects.hash(bidCreationTime, highestBid, highestBidderIP, auctionID);
    }

    @Override
    public String toString() {
        // 👉 Nếu log payload:
        // System.out.println(payload);
        // → sẽ in thông tin bid cao nhất hiện tại
        return "SendHighestBidPayload{" +
                "bidCreationDate=" + bidCreationTime +
                ", bid=" + highestBid +
                ", bidderIP='" + highestBidderIP + '\'' +
                ", auctionID=" + auctionID +
                '}';
    }
}

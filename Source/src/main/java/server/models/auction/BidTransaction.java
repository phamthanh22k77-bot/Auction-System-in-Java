package server.models.auction;

import server.models.Entity;
import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    private String auctionId; // Thuộc về phiên đấu giá nào
    private String bidderId; // Người nào đặt giá
    private double bidAmount; // Mức giá đặt
    private LocalDateTime timestamp; // Thời điểm đặt giá
    private BidStatus status; // Trạng thái của lượt trả giá

    public enum BidStatus {
        ACCEPTED, // Trả giá hợp lệ và được hệ thống chấp nhận
        REJECTED, // Bị từ chối (giá quá thấp, phiên đã kết thúc, lỗi, v.v.)
        WINNING // Sau cùng, đây là lượt giá chiến thắng phiên
    }

    // 1. Constructor cho lượt trả giá mới tạo
    public BidTransaction(String auctionId, String bidderId, double bidAmount) {
        super(); // Khởi tạo id tự sinh UUID
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
        this.status = BidStatus.ACCEPTED;
    }

    // 2. Constructor dùng khi load dữ liệu từ Database
    public BidTransaction(String id, String auctionId, String bidderId, double bidAmount,
            LocalDateTime timestamp, BidStatus status) {
        super(id);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BidTransaction{" +
                "id='" + getId() + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", bidAmount=" + bidAmount +
                ", time=" + timestamp +
                ", status=" + status +
                '}';
    }
}

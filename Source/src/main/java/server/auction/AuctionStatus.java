package server.auction;

/**
 * AuctionStatus - Enum mô tả các trạng thái của một phiên đấu giá.
 *
 * Vòng đời của Auction (theo class diagram):
 * OPEN → RUNNING → FINISHED → PAID
 * → CANCELED
 *
 * - OPEN : Phiên đã được tạo, chờ đến startTime để khai mạc.
 * - RUNNING : Phiên đang diễn ra, Bidder có thể đặt giá.
 * - FINISHED : Phiên đã kết thúc, xác định được người thắng (hoặc không có ai
 * đặt giá).
 * - PAID : Người thắng đã thanh toán, giao dịch hoàn tất.
 * - CANCELED : Phiên bị hủy (do Seller hủy hoặc Admin can thiệp).
 */
public enum AuctionStatus {
    OPEN,
    RUNNING,
    FINISHED,
    PAID,
    CANCELED
}

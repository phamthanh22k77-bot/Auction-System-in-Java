package server.auction;

import server.models.auction.Auction;

/**
 * Hỗ trợ các Component độc lập (Logger, Network Broadcaster) kết nối vào hệ thống
 * mà không gây ra vi phạm Tight-Coupling tới AuctionManager.
 */
public interface BidObserver {

    /**
     * Được kích hoạt tự động mỗi khi có giá thầu mới hợp lệ.
     *
     * @param auction   Phòng đấu giá mục tiêu
     * @param bidderId  Định danh người đấu giá
     * @param bidAmount Mức giá trúng
     */
    void onBidPlaced(Auction auction, String bidderId, double bidAmount);

    /**
     * [Optional] Lắng nghe sự kiện chuyển đổi trạng thái của phiên đấu.
     * (VD: OPEN -> RUNNING, RUNNING -> FINISHED)
     *
     * @param auction Phiên đấu bị tác động
     */
    default void onAuctionStatusChanged(Auction auction) {
        // Implement default để không gò ép Override ở cấp Subclass
    }
}

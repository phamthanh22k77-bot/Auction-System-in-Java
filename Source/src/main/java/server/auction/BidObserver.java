package server.auction;

import server.models.auction.Auction;

// Lắng nghe sự kiện đấu giá (Observer Pattern).

public interface BidObserver {

    // Kích hoạt tự động khi có lượt đặt giá (bid) mới hợp lệ.
    void onBidPlaced(Auction auction, String bidderId, double bidAmount);

    // Lắng nghe sự kiện thay đổi trạng thái phiên đấu giá.

    default void onAuctionStatusChanged(Auction auction) {
        // Mặc định không bắt buộc lớp con phải override
    }
}

package server.models.auction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void testConstructor_KhoiTaoTuDong() {
        BidTransaction bid = new BidTransaction("ID_auction", "ID_bidder", 500.0);

        assertNotNull(bid.getId(), "ID phải được tự động sinh ra");
        assertEquals("ID_auction", bid.getAuctionId(), "Gán sai auctionId");
        assertEquals("ID_bidder", bid.getBidderId(), "Gán sai bidderId");
        assertEquals(500.0, bid.getBidAmount(), "Gán sai bidAmount");
        assertNotNull(bid.getTimestamp(), "Thời gian (timestamp) phải tự động lấy giờ hiện tại");
        assertEquals(BidTransaction.BidStatus.ACCEPTED, bid.getStatus(),
                "Trạng thái mặc định khi vừa tạo phải là ACCEPTED");
    }

    @Test
    void testSetStatus_DoiTrangThaiThanhCong() {
        BidTransaction bid = new BidTransaction("auction", "bidder", 500.0);

        bid.setStatus(BidTransaction.BidStatus.WINNING);

        assertEquals(BidTransaction.BidStatus.WINNING, bid.getStatus(), "Phải đổi được trạng thái sang WINNING");
    }

    @Test
    void testValidate_GiaThapHonHienTai_TraVeFalse() {
        Auction auction = new Auction("item", "seller", java.time.LocalDateTime.now().minusHours(1), java.time.LocalDateTime.now().plusHours(1), 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);
        auction.setCurrentHighestBid(150.0);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder", 120.0); // Giá thấp hơn hiện tại
        boolean isValid = bid.validate(auction);

        assertFalse(isValid, "Đặt giá thấp hơn phải bị từ chối");
        assertEquals(BidTransaction.BidStatus.REJECTED, bid.getStatus());
    }

    @Test
    void testValidate_PhienKhongHoatDong_TraVeFalse() {
        Auction auction = new Auction("item", "seller", java.time.LocalDateTime.now().minusHours(2), java.time.LocalDateTime.now().minusHours(1), 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.FINISHED);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder", 120.0);
        boolean isValid = bid.validate(auction);

        assertFalse(isValid, "Phiên không hoạt động phải từ chối bid");
        assertEquals(BidTransaction.BidStatus.REJECTED, bid.getStatus());
    }

    @Test
    void testValidate_HopLe_TraVeTrue() {
        Auction auction = new Auction("item", "seller", java.time.LocalDateTime.now().minusHours(1), java.time.LocalDateTime.now().plusHours(1), 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder", 150.0);
        boolean isValid = bid.validate(auction);

        assertTrue(isValid, "Bid hợp lệ phải trả về true");
        assertEquals(BidTransaction.BidStatus.ACCEPTED, bid.getStatus());
    }
}

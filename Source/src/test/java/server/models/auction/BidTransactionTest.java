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
}

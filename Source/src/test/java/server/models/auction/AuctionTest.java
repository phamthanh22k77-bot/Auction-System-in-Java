package server.models.auction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class AuctionTest {

    @Test
    void testConstructor_KhoiTaoHopLe() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(2);
        Auction auction = new Auction("item", "seller", startTime, endTime, 100.0, 10.0);

        assertNotNull(auction.getId(), "ID phải được tự động sinh ra từ lớp Entity");
        assertEquals("item", auction.getItemId(), "Gán sai itemId");
        assertEquals("seller", auction.getSellerId(), "Gán sai sellerId");
        assertEquals(100.0, auction.getStartingPrice(), "Gán sai startingPrice");
        assertEquals(100.0, auction.getCurrentHighestBid(), "Giá hiện tại ban đầu phải bằng giá khởi điểm");
        assertEquals(10.0, auction.getMinimumBidIncrement(), "Gán sai minimumBidIncrement");
        assertEquals(Auction.AuctionStatus.PENDING, auction.getStatus(), "Phiên đấu giá mới tạo phải ở trạng thái PENDING");
    }

    @Test
    void testUpdateStatus_TrangThaiPending() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction("item1", "seller1", now.plusHours(1), now.plusHours(2), 100.0, 10.0);

        auction.updateStatus();

        assertEquals(Auction.AuctionStatus.PENDING, auction.getStatus(), "Chưa đến giờ bắt đầu thì phải là PENDING");
    }

    @Test
    void testUpdateStatus_TrangThaiRunning() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction("item2", "seller2", now.minusHours(1), now.plusHours(1), 100.0, 10.0);

        auction.updateStatus();

        assertEquals(Auction.AuctionStatus.RUNNING, auction.getStatus(),
                "Trong thời gian diễn ra phải chuyển thành RUNNING");
    }

    @Test
    void testUpdateStatus_TrangThaiFinished() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction("item3", "seller3", now.minusHours(2), now.minusHours(1), 100.0, 10.0);

        auction.updateStatus();

        assertEquals(Auction.AuctionStatus.FINISHED, auction.getStatus(),
                "Đã qua thời gian kết thúc phải chuyển thành FINISHED");
    }

    @Test
    void testUpdateStatus_TrangThaiKhongDoiNeuDaHuyHoacThanhToan() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction("item1", "seller1", now.minusHours(1), now.plusHours(1), 100.0, 10.0);

        auction.setStatus(Auction.AuctionStatus.CANCELED);

        auction.updateStatus();

        assertEquals(Auction.AuctionStatus.CANCELED, auction.getStatus(),
                "Phiên đã HỦY (CANCELED) thì hàm updateStatus không được phép ghi đè trạng thái");
    }

    @Test
    void testIsActive_ReturnTrue() {
        Auction auction = new Auction("item1", "seller1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 100.0,
                10.0);

        auction.setStatus(Auction.AuctionStatus.OPEN);
        assertTrue(auction.isActive(), "Trạng thái OPEN phải là active (true)");

        auction.setStatus(Auction.AuctionStatus.RUNNING);
        assertTrue(auction.isActive(), "Trạng thái RUNNING phải là active (true)");
    }

    @Test
    void testIsActive_ReturnFalse() {
        Auction auction = new Auction("item1", "seller1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 100.0,
                10.0);

        auction.setStatus(Auction.AuctionStatus.PENDING);
        assertFalse(auction.isActive(), "Trạng thái PENDING không được active (false)");

        auction.setStatus(Auction.AuctionStatus.FINISHED);
        assertFalse(auction.isActive(), "Trạng thái FINISHED không được active (false)");

        auction.setStatus(Auction.AuctionStatus.CANCELED);
        assertFalse(auction.isActive(), "Trạng thái CANCELED không được active (false)");
    }
}

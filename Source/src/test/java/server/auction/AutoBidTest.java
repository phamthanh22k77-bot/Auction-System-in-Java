package server.auction;

import org.junit.jupiter.api.Test;
import server.models.auction.Auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutoBidTest {

    @Test
    void testConstructor_HopLe() {
        AutoBid autoBid = new AutoBid("auction1", "bidder1", 1000.0, 10.0);
        assertEquals("auction1", autoBid.getAuctionId());
        assertEquals("bidder1", autoBid.getBidderId());
        assertEquals(1000.0, autoBid.getMaxBid());
        assertEquals(10.0, autoBid.getIncrement());
        assertTrue(autoBid.isActive());
    }

    @Test
    void testConstructor_KhongHopLe() {
        assertThrows(IllegalArgumentException.class, () -> new AutoBid("a1", "b1", -50.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new AutoBid("a1", "b1", 1000.0, -10.0));
    }

    @Test
    void testCalculateNextBid_HopLe() {
        AutoBid autoBid = new AutoBid("a1", "b1", 500.0, 20.0);
        double nextBid = autoBid.calculateNextBid(100.0);
        assertEquals(120.0, nextBid);
    }

    @Test
    void testCalculateNextBid_ChamNguongMaxBid() {
        AutoBid autoBid = new AutoBid("a1", "b1", 100.0, 20.0);
        double nextBid = autoBid.calculateNextBid(90.0);
        assertEquals(100.0, nextBid, "Giá tiếp theo không được vượt quá maxBid");
    }

    @Test
    void testCalculateNextBid_VuotMaxBid() {
        AutoBid autoBid = new AutoBid("a1", "b1", 100.0, 20.0);
        double nextBid = autoBid.calculateNextBid(120.0);
        assertEquals(-1.0, nextBid, "Khi giá hiện tại lớn hơn maxBid, trả về -1");
    }

    @Test
    void testCanCompete() {
        AutoBid autoBid = new AutoBid("a1", "b1", 200.0, 10.0);
        assertTrue(autoBid.canCompete(150.0));
        assertFalse(autoBid.canCompete(250.0));

        autoBid.deactivate();
        assertFalse(autoBid.canCompete(100.0), "Đã deactivate thì không thể cạnh tranh");
    }

    @Test
    void testProcessAutoBids_NhieuNguoiCungCanhTranh() {
        Auction auction = new Auction("item1", "seller", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), 100.0, 5.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        List<AutoBid> autoBids = new ArrayList<>();
        // bidder1: max 300, increment 10
        AutoBid ab1 = new AutoBid(auction.getId(), "bidder1", 300.0, 10.0);
        // bidder2: max 500, increment 20
        AutoBid ab2 = new AutoBid(auction.getId(), "bidder2", 500.0, 20.0);
        // bidder3: max 200, increment 15
        AutoBid ab3 = new AutoBid(auction.getId(), "bidder3", 200.0, 15.0);

        autoBids.add(ab1);
        autoBids.add(ab2);
        autoBids.add(ab3);

        String winner = AutoBid.processAutoBids(auction, autoBids);

        assertEquals("bidder2", winner, "Người có maxBid cao nhất phải thắng");
        // Người về nhì là bidder1 (max 300). Giá thầu cuối sẽ là max(runnerUp) + winner_increment = 300 + 20 = 320.
        assertEquals(320.0, auction.getCurrentHighestBid());
        assertEquals("bidder2", auction.getHighestBidderId());

        // Kểm tra trạng thái deactivate
        assertFalse(ab1.isActive(), "Bidder1 phải bị loại");
        assertFalse(ab3.isActive(), "Bidder3 phải bị loại");
        assertTrue(ab2.isActive(), "Bidder2 vẫn phải active");
    }

    @Test
    void testHandleManualBid_KichHoatAutoBid() {
        Auction auction = new Auction("item1", "seller", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        List<AutoBid> autoBids = new ArrayList<>();
        AutoBid ab1 = new AutoBid(auction.getId(), "bot1", 500.0, 20.0);
        autoBids.add(ab1);

        // Giả lập người thật vừa đặt giá 150
        auction.setCurrentHighestBid(150.0);
        auction.setHighestBidderId("real_human");

        AutoBid.handleManualBid(150.0, "real_human", auction, autoBids);

        // Bọn bot tự động đánh lên
        assertEquals("bot1", auction.getHighestBidderId(), "Bot phải tự động đấu giá lại");
        assertEquals(170.0, auction.getCurrentHighestBid(), "Bot đặt thêm increment của nó");
    }
}

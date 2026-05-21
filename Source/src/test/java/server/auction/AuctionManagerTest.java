package server.auction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.models.auction.Auction;
import server.models.auction.BidTransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionManagerTest {

    private AuctionManager manager;

    @BeforeAll
    static void beforeAll() throws IOException {
        server.dao.AuctionDAO.FILE_PATH = "data/auctions_test.json";
        server.dao.ItemDAO.FILE_PATH = "data/items_test.json";
        server.dao.UserDAO.FILE_PATH = "data/users_test.json";
        cleanUpFiles();
    }

    @BeforeEach
    void setUp() throws IOException {
        cleanUpFiles();
        manager = AuctionManager.getInstance();
        // Cần reset dữ liệu bộ nhớ trước mỗi test vì Singleton
        manager.khoiDong();
    }

    @AfterAll
    static void afterAll() throws IOException {
        cleanUpFiles();
    }

    private static void cleanUpFiles() throws IOException {
        Files.deleteIfExists(Path.of(server.dao.AuctionDAO.FILE_PATH));
        Files.deleteIfExists(Path.of(server.dao.ItemDAO.FILE_PATH));
        Files.deleteIfExists(Path.of(server.dao.UserDAO.FILE_PATH));
    }

    @Test
    void testTaoPhien() throws IOException {
        int initialSize = manager.soLuongPhien();

        Auction auction = manager.taoPhien("item123", "sellerA", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                100.0, 10.0);

        assertNotNull(auction);
        assertEquals("item123", auction.getItemId());
        assertEquals("sellerA", auction.getSellerId());
        assertEquals(initialSize + 1, manager.soLuongPhien());

        Auction found = manager.timTheoId(auction.getId());
        assertEquals(auction.getId(), found.getId());
    }

    @Test
    void testDatGia_HopLe() throws Exception {
        Auction auction = manager.taoPhien("item_test", "sellerX", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), 50.0, 5.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", 60.0);

        boolean result = manager.datGia(bid);

        assertTrue(result, "Giao dịch hợp lệ phải thành công");
        assertEquals(60.0, auction.getCurrentHighestBid());
        assertEquals("bidder1", auction.getHighestBidderId());
    }

    @Test
    void testDatGia_KhongHopLe() throws Exception {
        Auction auction = manager.taoPhien("item_test", "sellerX", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), 50.0, 5.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", 40.0); // Giá thấp hơn giá hiện tại

        // Trong unit test, vì giá thấp sẽ ném Exception nên ta bọc try-catch để assert
        try {
            manager.datGia(bid);
            fail("Lẽ ra phải ném AuctionLowBidException");
        } catch (server.auction.AuctionLowBidException e) {
            // Thành công (lỗi như mong đợi)
        }

        assertEquals(50.0, auction.getCurrentHighestBid(), "Giá phiên đấu giá không được thay đổi");
        assertNull(auction.getHighestBidderId());
    }

    @Test
    void testApplyAntiSniping_GiaHanThoiGian() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(10); // Chỉ còn 10 giây

        Auction auction = manager.taoPhien("item_snipe", "sellerY", startTime, endTime, 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder_sniper", 120.0);

        manager.datGia(bid);

        // Anti-sniping threshold là 30s. Nếu đặt trong 10s cuối, endTime sẽ tăng lên 30s từ thời điểm hiện tại.
        assertTrue(auction.getEndTime().isAfter(endTime), "Thời gian kết thúc phải được gia hạn");
    }

    @Test
    void testUpdateAllAuctionStatuses_ChuyenTrangThaiThoiGianThuc() throws Exception {
        Auction futureAuction = manager.taoPhien("item_future", "seller1", LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 10.0, 1.0);
        Auction runningAuction = manager.taoPhien("item_running", "seller2", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), 10.0, 1.0);
        Auction pastAuction = manager.taoPhien("item_past", "seller3", LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1), 10.0, 1.0);

        manager.updateAllAuctionStatuses();

        assertEquals(Auction.AuctionStatus.OPEN, futureAuction.getStatus());
        assertEquals(Auction.AuctionStatus.RUNNING, runningAuction.getStatus());
        assertEquals(Auction.AuctionStatus.FINISHED, pastAuction.getStatus());
    }

    @Test
    void testKetThucPhien() throws Exception {
        Auction auction = manager.taoPhien("item_test", "seller1", LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(1), 100.0, 10.0);

        boolean res = manager.ketThucPhien(auction.getId(), false);

        assertTrue(res);
        assertEquals(Auction.AuctionStatus.FINISHED, auction.getStatus());
    }
}

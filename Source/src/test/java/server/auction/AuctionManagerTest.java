package server.auction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.models.auction.Auction;
import server.models.auction.BidTransaction;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import server.models.network.AuctionClient;
import java.net.Socket;
import server.auction.AuctionLowBidException;
import server.auction.AuctionNotRegisteredException;
import server.auction.AuctionClientIsOwnerException;

class AuctionManagerTest {

    private AuctionManager manager;

    @BeforeEach
    void setUp() throws IOException {
        manager = AuctionManager.getInstance();
        // Cần reset dữ liệu bộ nhớ trước mỗi test vì Singleton
        manager.khoiDong(); 
    }

    @Test
    void testTaoPhien() throws IOException {
        int initialSize = manager.soLuongPhien();
        
        Auction auction = manager.taoPhien("item123", "sellerA", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 100.0, 10.0);
        
        assertNotNull(auction);
        assertEquals("item123", auction.getItemId());
        assertEquals("sellerA", auction.getSellerId());
        assertEquals(initialSize + 1, manager.soLuongPhien());
        
        Auction found = manager.timTheoId(auction.getId());
        assertEquals(auction.getId(), found.getId());
    }

    @Test
    void testDatGia_HopLe() throws IOException {
        Auction auction = manager.taoPhien("item_test", "sellerX", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 50.0, 5.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);
        
        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", 60.0);
        
        // Tạo dummy client
        AuctionClient dummyClient = new AuctionClient(new Socket());
        auction.addClient(dummyClient);

        boolean result = manager.datGia(bid, dummyClient);
        
        assertTrue(result, "Giao dịch hợp lệ phải thành công");
        assertEquals(60.0, auction.getCurrentHighestBid());
        assertEquals("bidder1", auction.getHighestBidderId());
    }

    @Test
    void testDatGia_KhongHopLe() throws IOException {
        Auction auction = manager.taoPhien("item_test", "sellerX", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 50.0, 5.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);
        
        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", 40.0); // Giá thấp hơn giá hiện tại
        
        // Tạo dummy client
        AuctionClient dummyClient = new AuctionClient(new Socket());
        auction.addClient(dummyClient);

        boolean result = manager.datGia(bid, dummyClient);
        
        assertFalse(result, "Giao dịch giá thấp hơn không được chấp nhận");
        assertEquals(50.0, auction.getCurrentHighestBid(), "Giá phiên đấu giá không được thay đổi");
        assertNull(auction.getHighestBidderId());
    }

    @Test
    void testApplyAntiSniping_GiaHanThoiGian() throws IOException {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(10); // Chỉ còn 10 giây
        
        Auction auction = manager.taoPhien("item_snipe", "sellerY", startTime, endTime, 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);
        
        BidTransaction bid = new BidTransaction(auction.getId(), "bidder_sniper", 120.0);
        
        // Tạo dummy client
        AuctionClient dummyClient = new AuctionClient(new Socket());
        auction.addClient(dummyClient);

        manager.datGia(bid, dummyClient);
        
        // Anti-sniping threshold là 30s. Nếu đặt trong 10s cuối, endTime sẽ tăng lên 30s từ thời điểm hiện tại.
        assertTrue(auction.getEndTime().isAfter(endTime), "Thời gian kết thúc phải được gia hạn");
    }

    @Test
    void testUpdateAllAuctionStatuses_ChuyenTrangThaiThoiGianThuc() throws IOException {
        Auction futureAuction = manager.taoPhien("item_future", "seller1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 10.0, 1.0);
        Auction runningAuction = manager.taoPhien("item_running", "seller2", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 10.0, 1.0);
        Auction pastAuction = manager.taoPhien("item_past", "seller3", LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), 10.0, 1.0);
        
        manager.updateAllAuctionStatuses();
        
        assertEquals(Auction.AuctionStatus.OPEN, futureAuction.getStatus());
        assertEquals(Auction.AuctionStatus.RUNNING, runningAuction.getStatus());
        assertEquals(Auction.AuctionStatus.FINISHED, pastAuction.getStatus());
    }

    @Test
    void testKetThucPhien() throws IOException {
        Auction auction = manager.taoPhien("item_test", "seller1", LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1), 100.0, 10.0);
        
        boolean res = manager.ketThucPhien(auction.getId(), false);
        
        assertTrue(res);
        assertEquals(Auction.AuctionStatus.FINISHED, auction.getStatus());
    }
}

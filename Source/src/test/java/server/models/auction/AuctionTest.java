package server.models.auction;

import org.junit.jupiter.api.*;
import server.models.network.AuctionClient;
import server.models.auction.Auction.AuctionStatus;
import server.auction.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static AuctionClient auctionClient;

    @BeforeAll
    static void setup() throws IOException {
        serverSocket = new ServerSocket(0); // Auto-assign port
        new Thread(() -> {
            try {
                serverSocket.accept();
            } catch (IOException ignored) {}
        }).start();
        clientSocket = new Socket("127.0.0.1", serverSocket.getLocalPort());
        auctionClient = new AuctionClient(clientSocket);
    }

    @AfterAll
    static void tearDown() throws IOException {
        clientSocket.close();
        serverSocket.close();
    }

    @Test
    @DisplayName("Test logic addBid và Anti-Sniping")
    void testAddBid() throws Exception {
        // 1. Setup Auction
        String sellerIp = "1.1.1.1"; // Seller khac IP client test
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusSeconds(10); // Chi con 10s
        
        Auction auction = new Auction("ITEM_TEST", sellerIp, start, end, 1000.0, 100.0);
        auction.setStatus(AuctionStatus.RUNNING);
        
        // Dang ky client vao phien
        auction.addClient(auctionClient);

        // 2. Tao Bid hop le
        BidTransaction validBid = new BidTransaction(auction.getId(), "USER_001", 1200.0);
        
        // 3. Thực hiện addBid
        auction.addBid(validBid, auctionClient);

        // 4. Assertions
        assertEquals(1200.0, auction.getCurrentHighestBid());
        assertEquals("USER_001", auction.getHighestBidderId());
        assertEquals(1, auction.getBidHistory().size());
        assertEquals(BidTransaction.BidStatus.ACCEPTED, auction.getBidHistory().get(0).getStatus());

        // 5. Check Anti-Sniping (thoi gian ket thuc phai duoc gia han)
        assertTrue(auction.getEndTime().isAfter(end), "Thoi gian ket thuc phai duoc gia han do < 30s");
        System.out.println("Anti-sniping OK. EndTime moi: " + auction.getEndTime());
    }

    @Test
    @DisplayName("Test loi dat gia qua thap")
    void testLowBid() throws Exception {
        Auction auction = new Auction("ITEM_LOW", "1.1.1.1", LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(5), 1000.0, 100.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.addClient(auctionClient);

        BidTransaction lowBid = new BidTransaction(auction.getId(), "USER_001", 1050.0); // Thieu (1000 + 100 = 1100)
        
        assertThrows(server.auction.AuctionLowBidException.class, () -> {
            auction.addBid(lowBid, auctionClient);
        });
    }
}

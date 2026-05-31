package server.models.auction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.auction.AuctionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch; // Bắt chạy chính xác tại 1 thời điểm
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentBidTest {

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
        // Reset singleton manager
        AuctionManager.getInstance().khoiDong();
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
    void testNhieuLuongCungGoiHamDatGia_KhongBiLoiRaceCondition() throws Exception {
        // GIẢ LẬP ĐA LUỒNG
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch sungLenhXuatPhat = new CountDownLatch(1);
        CountDownLatch choKetThuc = new CountDownLatch(100);

        // Đăng ký Auction vào Manager để test bằng cách khởi tạo trực tiếp (giống production)
        Auction createdAuction = new Auction("item", "seller",
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 100.0, 10.0);
        AuctionManager.getInstance().getAuctions().add(createdAuction);

        Auction managedAuction = AuctionManager.getInstance().timTheoId(createdAuction.getId());
        assertNotNull(managedAuction, "Phiên đấu giá phải được đăng ký trong bộ quản lý");
        managedAuction.setStatus(Auction.AuctionStatus.RUNNING);

        for (int i = 0; i < 100; i++) { // Nạp 100 luồng
            final int index = i;
            executor.submit(() -> {
                try {
                    // Tất cả các luồng vào đây sẽ bị chặn lại
                    sungLenhXuatPhat.await();

                    // GỌI HÀM CỐT LÕI QUA AUCTION MANAGER ĐỂ TEST LOCAL LOCK
                    BidTransaction tx = new BidTransaction(managedAuction.getId(), "ManualBidder" + index,
                            110.0 + index);
                    AuctionManager.getInstance().datGia(tx);

                } catch (Exception e) {
                    // Chấp nhận ngoại lệ vì một số luồng đặt giá thấp hơn giá hiện tại sau khi giá đã tăng
                } finally {
                    choKetThuc.countDown();
                }
            });
        }

        // 100 luồng gọi hàm ở cùng 1 mili-giây
        sungLenhXuatPhat.countDown();

        // Main Thread phải đứng chờ 100 luồng kia chạy xong hết mới được Assert
        choKetThuc.await();
        executor.shutdown();

        assertNotNull(managedAuction.getHighestBidderId(), "Phải có người chiến thắng");
        assertTrue(managedAuction.getCurrentHighestBid() > 100.0, "Giá chắc chắn phải bị đẩy lên cao hơn giá gốc");

        System.out.println("Giá cuối cùng: " + managedAuction.getCurrentHighestBid());
        System.out.println("Người thắng: " + managedAuction.getHighestBidderId());
    }
}

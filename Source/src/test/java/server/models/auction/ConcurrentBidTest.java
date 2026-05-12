package server.models.auction;

import org.junit.jupiter.api.Test;
import server.auction.AuctionManager;
import server.auction.AutoBid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch; // Bắt chạy chính xác tại 1 thời điểm
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentBidTest {
    @Test
    void testNhieuLuongCungGoiHamDatGia_KhongBiLoiRaceCondition() throws Exception {
        // GIẢ LẬP ĐA LUỒNG
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch sungLenhXuatPhat = new CountDownLatch(1);
        CountDownLatch choKetThuc = new CountDownLatch(100);

        // Đăng ký Auction vào Manager để test (Dùng taoPhien để đảm bảo ID và đăng ký khớp)
        Auction createdAuction = AuctionManager.getInstance().taoPhien(
                "item", "seller",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                100.0, 10.0
        );

        // Tạo 10 Auto-Bid (trả tới 5000$)
        List<AutoBid> autoBids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            autoBids.add(new AutoBid(createdAuction.getId(), "bot" + i, 5000.0, 10.0));
        }

        Auction managedAuction = AuctionManager.getInstance().timTheoId(createdAuction.getId());
        assertNotNull(managedAuction, "Auction should be registered in manager");
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
                    AuctionManager.getInstance().datGia(tx, autoBids);

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    choKetThuc.countDown();
                }
            });
        }

        // 100 luồng gọi hàm processAutoBids ở cùng 1 mili-giây
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

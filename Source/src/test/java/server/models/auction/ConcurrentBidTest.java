package server.models.auction;

import org.junit.jupiter.api.Test;
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
    void testNhieuLuongCungGoiHamDatGia_KhongBiLoiRaceCondition() throws InterruptedException {
        Auction auction = new Auction("item", "seller", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 100.0, 10.0);
        auction.setStatus(Auction.AuctionStatus.RUNNING);

        // Tạo 10 Auto-Bid (trả tới 5000$)
        List<AutoBid> autoBids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            autoBids.add(new AutoBid(auction.getId(), "bot" + i, 5000.0, 10.0));
        }

        // GIẢ LẬP ĐA LUỒNG
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch sungLenhXuatPhat = new CountDownLatch(1);
        CountDownLatch choKetThuc = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) { // Nạp 100 luồng
            executor.submit(() -> {
                try {
                    // Tất cả các luồng vào đây sẽ bị chặn lại
                    sungLenhXuatPhat.await();

                    // GỌI HÀM CỐT LÕI (Nơi xảy ra xung đột tranh giành bộ nhớ)
                    AutoBid.processAutoBids(auction, autoBids);

                } catch (InterruptedException e) {
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

        assertNotNull(auction.getHighestBidderId(), "Phải có người chiến thắng");
        assertTrue(auction.getCurrentHighestBid() > 100.0, "Giá chắc chắn phải bị đẩy lên cao hơn giá gốc");

        System.out.println("Giá cuối cùng: " + auction.getCurrentHighestBid());
        System.out.println("Người thắng: " + auction.getHighestBidderId());
    }
}

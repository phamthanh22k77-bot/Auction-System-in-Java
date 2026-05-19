package server.network;

import java.io.IOException;

//Phần này chỉ dùng để khởi động server
public class AuctionServerApp {
    public static void main(String[] args) throws IOException {

        // Đọc port từ cmd line
        int port = 9090;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Tham số '" + args[0] + "' phải là một số nguyên cổng Port hợp lệ.");
                System.exit(1);
            }
        }
        // Nếu nhập nhiều hơn một tham số = thoát
        if (args.length > 1) {
            System.err.println("Quá nhiều tham số truyền vào. Chỉ chấp nhận tối đa một tham số (cổng Port).");
            System.exit(1);
        }

        // Khởi động Managers để nạp dữ liệu từ file JSON
        try {
            server.auction.ItemManager.getInstance().khoiDong();
            server.auction.AuctionManager.getInstance().khoiDong();
        } catch (IOException e) {
            System.err.println("[Server] Không thể nạp dữ liệu: " + e.getMessage());
        }

        // Khởi tạo thực thể Server duy nhất (Singleton) với cổng Port đã xác định
        AuctionServer server = AuctionServer.getInstance(port);
        // Bắt đầu lắng nghe các yêu cầu kết nối Socket từ các Client
        server.listen();
    }
}

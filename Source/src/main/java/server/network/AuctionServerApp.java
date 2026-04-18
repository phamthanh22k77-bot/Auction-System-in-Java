package server.network;

import java.io.IOException;

//Phần này chỉ dùng để khởi động server
public class AuctionServerApp {
    public static void main(String[] args) throws IOException {

        //Đọc port từ cmd line
        int port = 0;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be a String.");
                System.exit(1);
            }
        }
        //Nếu nhập nhiều hơn một tham số = thoát
        if (args.length > 1) {
            System.err.println("There need to be only two arguments");
            System.exit(1);
        }

        //Tạo và lấy MỘT server instance (singleton)
        AuctionServer server = AuctionServer.getInstance(port);
        //Kích hoạt server và cho phép nó nghe các yêu cầu kết nối
        server.listen();
    }
}


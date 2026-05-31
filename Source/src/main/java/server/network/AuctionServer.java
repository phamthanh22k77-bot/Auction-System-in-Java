package server.network;

import server.models.auction.Auction;
import server.models.item.Item;
import server.models.network.*;
import server.models.auction.*;
import server.models.network.ServerNoAuctionException;
import server.auction.AuctionLowBidException;
import server.auction.*;
import client.message.PacketMessage;
import server.payload.*;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class AuctionServer {

    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static ServerSocket serverSocket;
    // Quản lý các Client kết nối bằng ConcurrentHashMap để an toàn đa luồng (Thread-safe)
    private static Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static AuctionServer instance;
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean isAcceptingAuctions = false;
    private static boolean isListening = false;

    private AuctionServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[Server] Đã mở cổng: " + port);
        } catch (IOException e) {
            System.err.println("[Server] Không thể mở cổng: " + port);
            e.printStackTrace();
        }
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        AuctionServer.pool = pool;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        AuctionServer.serverSocket = serverSocket;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setListening(boolean listening) {
        isListening = listening;
    }

    public Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public void setClientHandlers(Map<String, ClientHandler> clientHandlers) {
        AuctionServer.clientHandlers = clientHandlers;
    }

    // Mọi truy cập đều thông qua AuctionManager.
    public boolean isAcceptingAuctions() {
        return isAcceptingAuctions;
    }

    public void setAcceptingAuctions(boolean acceptingAuctions) {
        this.isAcceptingAuctions = acceptingAuctions;
    }

    public static synchronized AuctionServer getInstance(int port) {
        if (instance == null) {
            instance = new AuctionServer(port);
        }
        return instance;
    }

    public static synchronized AuctionServer getInstance() {
        if (instance == null) {
            instance = new AuctionServer(9090);
        }
        return instance;
    }

    public void listen() throws IOException {
        isAcceptingAuctions = true;
        isListening = true;
        System.out.println("[Server] Đang lắng nghe kết nối...");

        // Khởi động tác vụ tự động cập nhật trạng thái đấu giá mỗi giây
        scheduler.scheduleAtFixedRate(() -> {
            try {
                AuctionManager.getInstance().capNhatTrangThai();
            } catch (Exception e) {
                System.err.println("[Server] Lỗi cập nhật trạng thái: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);

        while (isListening) {
            Socket clientSocket = serverSocket.accept();
            AuctionClient client = new AuctionClient(clientSocket);
            ClientHandler clientThread = new ClientHandler(client);

            // Lưu handler theo địa chỉ IP/Port để quản lý
            String clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
            clientHandlers.put(clientKey, clientThread);

            pool.execute(clientThread);
            System.out.println("[Server] Client mới kết nối: " + clientKey);
        }
    }

    public synchronized void joinAuction(String auctionID, AuctionClient client) throws ServerNoAuctionException,
            server.auction.AuctionAlreadyRegisteredException, server.auction.AuctionClientIsOwnerException {
        // Lấy đúng instance duy nhất từ AuctionManager
        String targetID = auctionID;

        Auction auction = AuctionManager.getInstance().timTheoId(targetID);

        if (auction != null) {
            auction.addClient(client);
            System.out.println(
                    "[Server] Client " + client.getSocketAddress() + " đã tham gia vào PHIÊN THỰC TẾ: " + auctionID);
        } else {
            throw new ServerNoAuctionException("Không tìm thấy phiên: " + auctionID);
        }
    }

    // Gửi gói tin tới một Client cụ thể dựa trên thông tin IP:Port kết nối.
    // Ném ra IOException nếu kết nối socket bị lỗi.
    public void sendPacket(AuctionClient client, PacketMessage packet) throws IOException {
        // Check if the client connection exists
        String clientKey = client.getSocket().getInetAddress().getHostAddress() + ":" + client.getSocket().getPort();

        if (clientHandlers.containsKey(clientKey)) {
            clientHandlers.get(clientKey).sendPacket(packet);
        }
    }

    // Gửi gói tin tới danh sách các Client đang tham gia bằng cách lặp và gửi cho từng người.
    public void sendPackets(List<AuctionClient> clients, PacketMessage packet) {
        // Loop through all clients
        for (AuctionClient client : clients) {
            try {
                // Send client the provided packet parameter
                sendPacket(client, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Gửi gói tin tới tất cả các client đang kết nối
    public void broadcast(PacketMessage packet) {
        for (ClientHandler handler : clientHandlers.values()) {
            try {
                handler.sendPacket(packet);
            } catch (IOException e) {
                System.err.println("[Server] Lỗi broadcast tới " + handler.getClient().getIP());
            }
        }
    }

    // Trả về danh sách thông tin rút gọn của toàn bộ các phiên đấu giá đang hoạt động.
    public LinkedList<AuctionListItem> getAllAuctions() {
        LinkedList<AuctionListItem> auctionsItemsPayload = new LinkedList<>();

        // Luôn lấy từ AuctionManager (Nguồn duy nhất)
        for (Auction auction : AuctionManager.getInstance().getAuctions()) {
            auction.updateStatus();

            Item item = server.auction.ItemManager.getInstance().timTheoId(auction.getItemId());
            String itemName = item != null ? item.getName() : "Unknown Item";
            String itemDesc = item != null ? item.getDescription() : "No description";
            String category = item != null ? item.getCategory().name() : "Other";

            double highestBidPrice = auction.getCurrentHighestBid();

            auctionsItemsPayload.add(new AuctionListItem(auction.getId(), auction.getStartingPrice(), itemName,
                    itemDesc, auction.getSellerId(), highestBidPrice, category, auction.getStartTime().toString(), auction.getEndTime().toString(),
                    auction.getStatus().name(), auction.getHighestBidderId()));
        }

        return auctionsItemsPayload;
    }

    public LinkedList<AuctionListItem> getMyAuctions(String username) {
        LinkedList<AuctionListItem> myAuctionsItems = new LinkedList<>();

        for (Auction auction : AuctionManager.getInstance().getAuctions()) {
            // Kiểm tra xem user có phải seller hoặc là người bid cao nhất không
            boolean isSeller = auction.getSellerId() != null && auction.getSellerId().equals(username);
            boolean isBidder = auction.getHighestBidderId() != null && auction.getHighestBidderId().equals(username);

            if (isSeller || isBidder) {
                auction.updateStatus();
                Item item = server.auction.ItemManager.getInstance().timTheoId(auction.getItemId());

                myAuctionsItems.add(new AuctionListItem(auction.getId(), auction.getStartingPrice(),
                        item != null ? item.getName() : auction.getItemId(),
                        item != null ? item.getDescription() : "Đơn hàng/Phiên của tôi", auction.getSellerId(),
                        auction.getCurrentHighestBid(), item != null ? item.getCategory().name() : "PERSONAL",
                        auction.getStartTime().toString(), auction.getEndTime().toString(), auction.getStatus().name(), auction.getHighestBidderId()));
            }
        }
        return myAuctionsItems;
    }

    // Hủy đăng ký (rời phiên) của Client khỏi phiên đấu giá.
    public void leaveAuction(String auctionID, AuctionClient client)
            throws ServerNoAuctionException, AuctionHighBidException, AuctionNotRegisteredException {

        // [FIX] Kiểm tra từ AuctionManager
        Auction auction = AuctionManager.getInstance().timTheoId(auctionID);
        if (auction != null) {
            // Hủy đăng ký client khỏi phiên đấu giá
            auction.removeClient(client);
        } else {
            throw new ServerNoAuctionException("Phiên đấu giá không tồn tại. Hành động không được phép.");
        }
    }

    // Dừng luồng xử lý và giải phóng tài nguyên của Client khỏi Server.
    public void removeClient(AuctionClient client)
            throws IOException, ServerClientHandlerDoesNotExistException {

        String clientKey = client.getSocket().getInetAddress().getHostAddress() + ":" + client.getSocket().getPort();
        // Kiểm tra xem client có tồn tại hay không
        if (clientHandlers.containsKey(clientKey)) {

            // Dừng thực thi luồng ClientHandler
            clientHandlers.get(clientKey).stopRunning();

        } else {

            throw new ServerClientHandlerDoesNotExistException("ClientHandler không tồn tại");
        }
    }

    // Trả về lượt giao dịch đặt giá cao nhất hiện tại của phiên đấu giá.
    // Ném ra ngoại lệ nếu phiên đấu giá không tồn tại.
    public BidTransaction getHighestBid(String auctionID) throws ServerNoAuctionException {

        // [FIX] Lấy từ AuctionManager
        Auction auction = AuctionManager.getInstance().timTheoId(auctionID);

        // Kiểm tra xem phiên đấu giá có tồn tại hay không
        if (auction != null) {
            // Yêu cầu phiên đấu giá trả về mức giá đấu cao nhất
            return auction.findHighestBid();
        } else {
            throw new ServerNoAuctionException("Phiên đấu giá không tồn tại. Hành động không được phép.");
        }
    }

    // Thêm một phiên đấu giá mới vào hệ thống quản lý.
    public void addAuction(Auction auction) {
        // [FIX] Thêm vào AuctionManager để đồng bộ
        AuctionManager.getInstance().getAuctions().add(auction);
        try {
            new server.dao.AuctionDAO().them(auction);
            System.out.println("[Server] Đã lưu phiên đấu giá mới vào file JSON thành công: " + auction.getId());
        } catch (IOException e) {
            System.err.println("[Server] Lỗi khi lưu phiên đấu giá mới vào file JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tiến hành xử lý lượt đặt giá mới cho phiên đấu giá.
    // Kiểm tra đầy đủ các điều kiện (mức giá hợp lệ, tư cách tham gia, sở hữu) trước khi chấp nhận.
    public void auctionBid(String auctionID, BidTransaction bid, AuctionClient client)
            throws AuctionLowBidException, ServerNoAuctionException {

        // Lấy auction từ AuctionManager để đảm bảo đồng bộ
        Auction auction = AuctionManager.getInstance().timTheoId(auctionID);
        if (auction != null) {
            try {
                // Thực hiện đặt giá thông qua AuctionManager
                boolean success = AuctionManager.getInstance().datGia(bid);
                if (success) {
                    System.out.println(
                            "[Server] Đặt giá thành công: " + bid.getBidAmount() + " bởi " + bid.getBidderId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new ServerNoAuctionException("Auction doesn't exist: " + auctionID);
        }
    }
}
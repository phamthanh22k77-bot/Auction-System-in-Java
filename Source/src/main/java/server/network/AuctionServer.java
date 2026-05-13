package server.network;

import server.models.auction.Auction;
import server.models.item.Item;
import server.models.network.*;
import server.auction.*;
import client.message.PacketMessage;
import server.payload.*;
import server.models.auction.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {

    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static ServerSocket serverSocket;
    private static Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private static AuctionServer instance;
    private static Map<String, Auction> auctions = new HashMap<>();
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
    //Getters and Setters
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

    public Map<String, Auction> getAuctions() {
        return auctions;
    }

    public void setAuctions(Map<String, Auction> auctions) {
        this.auctions = auctions;
    }

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

    public synchronized void joinAuction(String auctionID, AuctionClient client) throws ServerNoAuctionException {
        // Lấy Auction từ AuctionManager (Nguồn dữ liệu duy nhất)
        Auction auction = AuctionManager.getInstance().timTheoId(auctionID);

        if (auction != null) {
            try {
                auction.addClient(client);
                System.out.println("[Server] Client " + client.getSocketAddress() + " đã tham gia phiên: " + auctionID);
            } catch (Exception e) {
                System.err.println("[Server] Lỗi khi tham gia phiên: " + e.getMessage());
            }
        } else {
            throw new ServerNoAuctionException("Không tìm thấy phiên đấu giá với ID: " + auctionID);
        }
    }

    /*
    Điều kiện trước: Phương thức yêu cầu nhận một đối tượng Client và một đối tượng PacketMessage.

    Điều kiện sau: Phương thức gửi packet PacketMessage được cung cấp tới client phù hợp
    dựa trên đối tượng Client. Packet sẽ được gửi thông qua instance ClientHandler tương ứng
    với đối tượng Client được dùng làm khóa trong cấu trúc dữ liệu clientHandlers.

    Phương thức không trả về giá trị nào.

    LƯU Ý:
    Nếu packet không thể được gửi qua socket thì sẽ ném ra ngoại lệ IOException.
*/
    public void sendPacket(AuctionClient client, PacketMessage packet)
            throws IOException {
        //Check if the client connection exists
        String clientKey =
                client.getSocket().getInetAddress().getHostAddress()
                        + ":" +
                        client.getSocket().getPort();

        if (clientHandlers.containsKey(clientKey)) {
            clientHandlers.get(clientKey).sendPacket(packet);
        }
    }

    /*
    Điều kiện trước: Phương thức yêu cầu nhận một LinkedList kiểu Client
    và một đối tượng PacketMessage.

    Điều kiện sau: Phương thức gửi packet PacketMessage được cung cấp tới
    tất cả các đối tượng Client có trong tham số LinkedList clients.
    Packet sẽ được gửi tới từng Client bằng cách gọi phương thức sendPackets.

    Phương thức không trả về giá trị nào.
*/
    public void sendPackets(List<AuctionClient> clients, PacketMessage packet) {
        //Loop through all clients
        for (AuctionClient client : clients) {
            try {
                //Send client the provided packet parameter
                sendPacket(client, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /*
    Điều kiện trước: Không có.

    Điều kiện sau:
    - Phương thức trả về một LinkedList chứa các đối tượng AuctionListItem.
    - Mỗi AuctionListItem được tạo dựa trên thông tin của từng phiên đấu giá hiện có
      trong biến auctions.
*/
    public LinkedList<AuctionListItem> getAllAuctions() {

        LinkedList<AuctionListItem> auctionsItemsPayload = new LinkedList<>();

        // Duyệt qua tất cả các phiên đấu giá hiện đang hoạt động
        for (Auction auction : auctions.values()) {

            Item auctionItem = auction.getItem();

            // Tìm mức giá đấu cao nhất hiện tại của phiên đấu giá (nếu có)
            double highestBidPrice = auction.findHighestItemPrice();

            auctionsItemsPayload.add(
                    new AuctionListItem(
                            auction.getId(),
                            auctionItem.getStartingPrice(),
                            auctionItem.getName(),
                            auctionItem.getDescription(),
                            auction.getSellerId(),
                            highestBidPrice
                    )
            );
        }

        return auctionsItemsPayload;
    }
    /*
    Điều kiện trước:
    - Phương thức nhận vào một tham số kiểu int dùng làm mã định danh cho
      một phiên đấu giá đang tồn tại trong cấu trúc dữ liệu auctions.
    - Đồng thời nhận vào một đối tượng Client đã đăng ký tham gia phiên đấu giá đó.

    Điều kiện sau:
    - Client được xóa khỏi phiên đấu giá tương ứng với auctionID được cung cấp.
    - Phương thức không trả về giá trị nào.

    Lưu ý:
    - ServerNoAuctionException được ném ra nếu không tìm thấy phiên đấu giá
      tương ứng với auctionID được cung cấp.
    - AuctionHighBidException được ném ra nếu Client đang giữ mức giá đấu cao nhất
      trong phiên đấu giá được yêu cầu.
    - AuctionNotRegisteredException được ném ra nếu Client chưa đăng ký
      trong phiên đấu giá được chỉ định.
*/
    public void leaveAuction(String auctionID, AuctionClient client)
            throws ServerNoAuctionException,
            AuctionHighBidException,
            AuctionNotRegisteredException {

        // Kiểm tra xem phiên đấu giá có tồn tại hay không
        if (auctions.containsKey(auctionID)) {

            // Hủy đăng ký client khỏi phiên đấu giá
            auctions.get(auctionID).removeClient(client);

        } else {

            throw new ServerNoAuctionException(
                    "Phiên đấu giá không tồn tại. Hành động không được phép."
            );
        }
    }
    /*
    Điều kiện trước:
    - Phương thức nhận vào một đối tượng Client đang tham gia một kết nối hoạt động
      (được quản lý bởi một luồng ClientHandler trong biến clientHandlers).

    Điều kiện sau:
    - Đối tượng Client được xóa khỏi cấu trúc dữ liệu clientHandlers.
    - Luồng ClientHandler tương ứng được dừng thực thi.
    - Phương thức không trả về giá trị nào.

    Lưu ý:
    - Nếu Client đang giữ mức giá đấu cao nhất trong một hoặc nhiều phiên đấu giá,
      client sẽ không bị xóa.
    - ServerHasHighBidException được ném ra nếu Client đang giữ mức giá đấu cao nhất
      trong một phiên đấu giá.
    - IOException được ném ra nếu không thể đóng kết nối socket.
    - ServerClientHandlerDoesNotExistException được ném ra nếu Client được cung cấp
      hiện không có kết nối hoạt động trong cấu trúc dữ liệu clientHandlers.
*/
    public void removeClient(AuctionClient client)
            throws IOException,
            ServerClientHandlerDoesNotExistException,
            ServerHasHighBidException {

        String clientKey =
                client.getSocket().getInetAddress().getHostAddress()
                        + ":" +
                        client.getSocket().getPort();
        // Kiểm tra xem client có tồn tại hay không
        if (clientHandlers.containsKey(clientKey)) {

            // Dừng thực thi luồng ClientHandler
            clientHandlers.get(clientKey).stopRunning();

        } else {

            throw new ServerClientHandlerDoesNotExistException(
                    "ClientHandler không tồn tại"
            );
        }
    }
    /*
    Điều kiện trước:
    - Phương thức nhận vào một giá trị int dùng để định danh
      một phiên đấu giá đang tồn tại trong biến auctions.

    Điều kiện sau:
    - Phương thức trả về một đối tượng Bid là mức giá đấu cao nhất
      hiện tại của phiên đấu giá được chỉ định.

    Lưu ý:
    - ServerNoAuctionException được ném ra nếu phiên đấu giá được chỉ định
      không tồn tại.
*/
    public BidTransaction getHighestBid(String auctionID)
            throws ServerNoAuctionException {

        // Khởi tạo thuộc tính
        Auction auction;

        // Kiểm tra xem phiên đấu giá có tồn tại hay không
        if (auctions.containsKey(auctionID)) {

            // Lấy phiên đấu giá cụ thể
            auction = auctions.get(auctionID);

            // Yêu cầu phiên đấu giá trả về mức giá đấu cao nhất
            return auction.findHighestBid();

        } else {

            throw new ServerNoAuctionException(
                    "Phiên đấu giá không tồn tại. Hành động không được phép."
            );
        }
    }
    /*
    Điều kiện trước:
    - Phương thức nhận vào một đối tượng Auction.

    Điều kiện sau:
    - Đối tượng Auction được thêm vào cấu trúc dữ liệu auctions.
    - Phương thức không trả về giá trị nào.
*/
    public void addAuction(Auction auction) {

        // Thêm phiên đấu giá mới vào HashMap auctions
        auctions.put(auction.getId(), auction);
    }
    /*
    Điều kiện trước:
    - Phương thức nhận vào:
        + Một giá trị int dùng để định danh một phiên đấu giá tồn tại trong biến auctions.
        + Một đối tượng BidTransaction đại diện cho lượt trả giá cần thực hiện.
        + Một đối tượng Client đại diện cho client thực hiện trả giá.

    Điều kiện sau:
    - Phương thức yêu cầu phiên đấu giá được chỉ định thêm lượt trả giá
      bằng đối tượng BidTransaction và Client được cung cấp.
    - Phương thức không trả về giá trị nào.

    Lưu ý:
    - AuctionLowBidException được ném ra nếu BidTransaction có mức giá
      thấp hơn mức giá hợp lệ hiện tại của phiên đấu giá.
    - AuctionNotRegisteredException được ném ra nếu Client chưa đăng ký
      tham gia phiên đấu giá được chỉ định.
    - AuctionClientIsOwnerException được ném ra nếu Client là chủ sở hữu
      của phiên đấu giá.
    - ServerNoAuctionException được ném ra nếu phiên đấu giá không tồn tại.
*/
    public void auctionBid(
            String auctionID,
            BidTransaction bid,
            AuctionClient client
    )
            throws AuctionLowBidException,
            AuctionNotRegisteredException,
            AuctionClientIsOwnerException,
            ServerNoAuctionException {

        // Kiểm tra xem phiên đấu giá có tồn tại hay không
        if (auctions.containsKey(auctionID)) {

            // Yêu cầu auction xử lý bid mới
            //auctions.get(auctionID).addBid(bid, client);
            /* Do phần addBid có phương thức hoạt động tương tự với datGia và Antisniping
            của Kiệt nên sẽ làm thêm sau
             */

        } else {

            throw new ServerNoAuctionException(
                    "Auction doesn't exist. Action not permitted."
            );
        }
    }
}
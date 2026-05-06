package server.network;

import client.message.MessageType;
import server.models.auction.Auction;
import server.models.item.Item;
import server.models.network.*;
import server.auction.*;
import client.message.PacketMessage;
import server.payload.*;
import server.models.auction.*;
import server.models.item.*;
import server.models.user.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static client.message.MessageType.*;

public class AuctionServer {

    // Lập attribute
    private static ExecutorService pool = Executors.newFixedThreadPool(4);
    // Tạo ServerSocket để nhận vào yêu cầu kết nối TCP. ServerSocket là class dùng
    // để mở port và chờ client kết nối: boolean accept()
    private static ServerSocket serverSocket;
    // Đây là HashMap chứa các thread ClientHandler đang hoạt động, các thread được
    // nhận diện bằng IP, dưới dạng string
    private static Map<String, ClientHandler> clientHandlers;
    // Dùng HashMap để lưu nhiều auction, auction sẽ được định danh bằng ID, ID cũng
    // có thể được dùng để làm key
    private static Map<Integer, Auction> auctions;
    // Server singleton, tức chỉ cho phép 1 object server trong RAM được chạy trong
    // server này
    private static AuctionServer server;
    // Tạo boolean có cho phép người dùng tạo auction từ client hay không, có
    // clienthandler sử dụng, sẽ add sau
    private static boolean isAcceptingAuctions;
    // Boolean dùng để bật/tắt vòng lặp lắng nghe kết nối client của server
    private static boolean isListening;

    // Constructors
    private AuctionServer() {
        super();
        try {
            serverSocket = new ServerSocket(9090);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlers = new HashMap<String, ClientHandler>();
        auctions = new HashMap<Integer, Auction>();
        isListening = false;
        isAcceptingAuctions = false;
    }

    private AuctionServer(int port) {
        super();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlers = new HashMap<String, ClientHandler>();
        auctions = new HashMap<Integer, Auction>();
        isListening = false;
        isAcceptingAuctions = false;
    }

    // setter và getter
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
        clientHandlers = clientHandlers;
    }

    public boolean isAcceptingAuctions() {
        return AuctionServer.isAcceptingAuctions;
    }

    public Map<Integer, Auction> getAuctions() {
        return auctions;
    }

    private static AuctionServer getServer() {
        return server;
    }

    private static void setServer(AuctionServer server) {
        server = server;
    }

    public void setAuctions(Map<Integer, Auction> auctions) {
        this.auctions = auctions;
    }

    public void setAcceptingAuctions(boolean acceptingAuctions) {
        this.isAcceptingAuctions = acceptingAuctions;
    }

    /*
     * Ko điều kiện đầu vào
     * Đầu ra: tạo một đối tượng singleton server, nêu chưa tồn tại sẽ tạo đối tượng
     * mới, kết nối với constructor
     */
    public static AuctionServer getInstance() {
        if (server == null) {
            server = new AuctionServer();
        }
        return server;
    }
    /*
     * Đầu vào: nhận một obj int tên port
     * Đầu ra: tạo một đối tượng singleton server, nêu chưa tồn tại sẽ tạo đối tượng
     * mới,
     * kết nối với constructor với int port được cho
     */

    public static AuctionServer getInstance(int port) {
        if (server == null) {
            server = new AuctionServer(port);
        }
        return server;
    }

    /*
     * Ko điều kiện đầu vào
     * Đầu ra: khiến singleton server chạy trong một vòng lặp, nghe thông tin cho
     * phép xây
     * dựng kết nối TCP. Mỗi socket đang hoạt động sẽ được đưa vào một obj Client
     * được tạo ra,
     * sau đó sẽ được đưa vào một thread của ClientHandler. ClientHandler lại được
     * đưa vào biến clientHandlers,
     * sở hữu các key "socket, InetAddress, SocketAddress" (các value đã được tạo)
     * dưới dạng string
     * Method không trả về gì
     */
    public void joinAuction(int auctionID, AuctionClient client) throws ServerNoAuctionException{

        // 1. Tìm phiên đấu giá (auction) dựa trên auctionId
        Auction auction = auctions.get(auctionID);

        // 2. Nếu tìm thấy phiên đấu giá hợp lệ
        if (auction != null) {
            // thêm auctionId vào danh sách đã đăng ký của client đó
            client.getRegisteredAuctions().add(auctionID);

            System.out.println("[Server] Client " + client.getSocketAddress() + " đã tham gia phiên: " + auctionID);
        } else {
            System.out.println("Lỗi: Không tìm thấy phiên đấu giá với ID " + auctionID);
        }
    }
    public void listen() throws IOException {

        isAcceptingAuctions = true;
        isListening = true;

        // Bool isListening cho phép vòng lặp chạy cho tới khi bool bị chuyển thành
        // false
        while (isListening) {
            // Cho phép client mới kết nối
            Socket clientSocket = serverSocket.accept();
            // Tạo biến client mới
            AuctionClient client = new AuctionClient(clientSocket);
            // Tạo thread clienthandler cho biến client vừa được tạo
            ClientHandler clientThread = new ClientHandler(client);
            // Đặt biến clientThread vào hashmap clientHandler và bắt đầu xử lý
            clientHandlers.put(client.getSocket().getInetAddress().getHostAddress(), clientThread);
            pool.execute(clientThread);
        }

    }
    public void leaveAuction(int auctionID, AuctionClient client){}

}
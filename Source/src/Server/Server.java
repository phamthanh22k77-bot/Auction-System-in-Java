package Server;

import Server.AuctionException.*;
import Server.ServerException.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    // Lập attribute
    private static ExecutorService pool = Executors.newFixedThreadPool(4);
    // Tạo ServerSocket để nhận vào yêu cầu kết nối TCP. ServerSocket là class dùng để mở port và chờ client kết nối: boolean accept()
    private static ServerSocket serverSocket;
    // Đây là HashMap chứa các thread ClientHandler đang hoạt động, các thread được nhận diện bằng IP, dưới dạng string
    private static Map<String, ClientHandler> clientHandlers;
    // Dùng HashMap để lưu nhiều auction, auction sẽ được định danh bằng ID, ID cũng có thể được dùng để làm key
    //private static Map<> auctions; trong <> sẽ là <Integer, Auction> nhưng auction chưa được thiết lập
    // Server singleton, tức chỉ cho phép 1 object server trong RAM được chạy trong server này
    private static Server server;
    // Tạo boolean có cho phép người dùng tạo auction từ client hay không, có clienthandler sử dụng, sẽ add sau
    private static boolean isAcceptingAuctions;
    // Boolean dùng để bật/tắt vòng lặp lắng nghe kết nối client của server
    private static boolean isListening;

    // Constructors
    private Server() {
        super();
        try {
            serverSocket = new ServerSocket(9090);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlers = new HashMap<String, ClientHandler>();
        //auctions = new HashMap<>(); trong <> sẽ là <Integer, Auction> nhưng auction chưa được thiết lập
        isListening = false;
        isAcceptingAuctions = false;
    }

    private Server(int port) {
        super();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientHandlers = new HashMap<String, ClientHandler>();
        //auctions = new HashMap<>(); trong <> sẽ là <Integer, Auction> nhưng auction chưa được thiết lập
        isListening = false;
        isAcceptingAuctions = false;
    }

    // setter và getter 
    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        Server.pool = pool;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        Server.serverSocket = serverSocket;
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
        return Server.isAcceptingAuctions;
    }

    /*public Map<Integer//, Auction//> getAuctions() {
        return auctions;
    }

    private static Server getServer() {
        return server;
    }

    private static void setServer(Server server) {
        server = server;
    }

    public void setAuctions(Map<Integer, Auction> auctions) {
        this.auctions = auctions;
    }

    public void setAcceptingAuctions(boolean acceptingAuctions) {
        this.isAcceptingAuctions = acceptingAuctions;
    } chưa cho phép tạo do chưa có phần server và auction*/
}
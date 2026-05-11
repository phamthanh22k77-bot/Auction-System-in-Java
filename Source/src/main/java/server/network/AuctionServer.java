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

    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static ServerSocket serverSocket;
    private static Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private static AuctionServer instance;
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

    public static Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static boolean isAcceptingAuctions() {
        return isAcceptingAuctions;
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
        if (clientHandlers.containsKey(client.getSocket().getInetAddress().getHostAddress())) {
            clientHandlers.get(client.getSocketAddress().getAddress().getHostAddress()).sendPacket(packet);
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
    public void sendPackets(LinkedList<AuctionClient> clients, PacketMessage packet) {
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
}
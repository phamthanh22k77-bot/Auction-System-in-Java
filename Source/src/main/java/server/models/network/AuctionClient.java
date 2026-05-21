package server.models.network;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

public class AuctionClient {
    private InetSocketAddress socketAddress;
    private LinkedList<String> registeredAuctions;
    private int numberOfHighBids;
    private Socket socket;
    private String username;

    // Khởi tạo Client với socket kết nối
    public AuctionClient(Socket socket) {
        this.socketAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        this.socket = socket;
        this.registeredAuctions = new LinkedList<>();
        this.numberOfHighBids = 0;
        this.username = "Guest";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(InetSocketAddress address) {
        this.socketAddress = address;
    }

    public LinkedList<String> getRegisteredAuctions() {
        return registeredAuctions;
    }

    public void setRegisteredAuctions(LinkedList<String> registeredAuctions) {
        this.registeredAuctions = registeredAuctions;
    }

    public int getNumberOfHighBids() {
        return numberOfHighBids;
    }

    public void setNumberOfHighBids(int numberOfHighBids) {
        this.numberOfHighBids = numberOfHighBids;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Lấy địa chỉ IP của Client
    public String getIP() {
        return socketAddress.getAddress().getHostAddress();
    }

    // Tăng số lượng phiên đang giữ giá cao nhất lên 1
    public void madeHighBid() {
        numberOfHighBids++;
    }

    // Giảm số lượng phiên đang giữ giá cao nhất đi 1
    public void lostHighBid() {
        if (numberOfHighBids > 0) {
            numberOfHighBids--;
        }
    }

    @Override
    public String toString() {
        return "AuctionClient{"
                + "username='" + username + '\''
                + ", socketAddress=" + socketAddress
                + ", registeredAuctions=" + registeredAuctions
                + ", numberOfHighBids=" + numberOfHighBids
                + ", socket=" + socket
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        // Kiểm tra xem hai đối tượng có cùng tham chiếu bộ nhớ hay không
        if (this == o) {
            return true;
        }

        // Kiểm tra đối tượng null hoặc khác lớp
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuctionClient that = (AuctionClient) o;

        // So sánh định danh dựa trên địa chỉ Socket và đối tượng Socket
        return Objects.equals(socketAddress, that.socketAddress) && Objects.equals(socket, that.socket);
    }

    @Override
    public int hashCode() {
        // Tạo mã băm an toàn và bất biến dựa trên thông số Socket
        return Objects.hash(socketAddress, socket);
    }
}

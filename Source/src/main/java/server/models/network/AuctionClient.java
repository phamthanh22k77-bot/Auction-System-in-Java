package server.models.network;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

public class AuctionClient {

    // Các variable cần nhận
    private InetSocketAddress socketAddress;
    private LinkedList<String> registeredAuctions;
    private int numberOfHighBids;
    private Socket socket;


    // Constructors
    public AuctionClient(Socket socket) {
        this.socketAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        this.socket = socket;
        this.registeredAuctions = new LinkedList<>();
        numberOfHighBids = 0;
    }

    // Setter và getter
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

    // Methods

    /*
        Điều kiện trước: Không có.

        Điều kiện sau: Phương thức chỉ đơn giản tăng biến numberOfHighBids lên 1.

        Phương thức không trả về giá trị nào.
    */
    public void madeHighBid() {
        numberOfHighBids++;
    }

    /*
        Điều kiện trước: Không có.

        Điều kiện sau: Phương thức chỉ đơn giản giảm biến numberOfHighBids đi 1
        nếu giá trị hiện tại lớn hơn 0.

        Phương thức không trả về giá trị nào.
    */
    public void lostHighBid() {
        if (numberOfHighBids > 0) {
            numberOfHighBids--;
        }
    }

    @Override
    public String toString() {
        return "ServerClient{" +
                "socketAddress=" + socketAddress +
                ", registeredAuctions=" + registeredAuctions +
                ", numberOfHighBids=" + numberOfHighBids +
                ", socket=" + socket +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        // Kiểm tra xem hai đối tượng có cùng tham chiếu hay không
        if (this == o) {
            return true;
        }

        // Kiểm tra đối tượng null hoặc khác class
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuctionClient that = (AuctionClient) o;

        // So sánh các thuộc tính của Client
        return numberOfHighBids == that.numberOfHighBids &&
                Objects.equals(socketAddress, that.socketAddress) &&
                Objects.equals(registeredAuctions, that.registeredAuctions) &&
                Objects.equals(socket, that.socket);
    }

    @Override
    public int hashCode() {

        // Tạo mã băm dựa trên các thuộc tính của Client
        return Objects.hash(socketAddress, registeredAuctions, numberOfHighBids, socket);
    }
}

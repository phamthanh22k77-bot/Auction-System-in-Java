package server.models.network;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

public class AuctionClient {

    // Các variable cần nhận
    private InetSocketAddress socketAddress;
    private LinkedList<Integer> registeredAuctions;
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

    public LinkedList<Integer> getRegisteredAuctions() {
        return registeredAuctions;
    }

    public void setRegisteredAuctions(LinkedList<Integer> registeredAuctions) {
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
}

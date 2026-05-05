package server.network;

import server.models.network.*;
import server.auction.*;
import server.payload.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import client.message.*;
import java.io.IOException;
import static client.message.MessageType.*;

public class ClientHandler extends Thread {

    // các variable cần nhận
    private AuctionClient client;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private boolean isRunning;

    // Constructor
    public ClientHandler(AuctionClient serverClient) {
        this.client = serverClient;
        this.isRunning = true;
    }

    // Setter và getter
    public AuctionClient getClient() {
        return client;
    }

    public void setClient(AuctionClient client) {
        this.client = client;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        // Khởi tạo các biến, lấy instance server
        AuctionServer server = AuctionServer.getInstance();
        String packetMessageJson = null;
        MessageType packetType = null;
        PacketMessage inputPacketMessage = null;
        isRunning = true;

        try {
            objectInputStream = new ObjectInputStream(client.getSocket().getInputStream());
            objectOutputStream = new ObjectOutputStream(client.getSocket().getOutputStream());
            sendPacket((new PacketMessage(WELCOME_MESSAGE, null)));
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    // Đầu vào: phương thức nhận một packet msg, thử gửi một packet
    // Đầu ra: packet được gửi đúng chỗ
    public void sendPacket(PacketMessage packetMessage) throws IOException {
        objectOutputStream.writeObject(packetMessage);
    }

    /*
     * Đầu vào: nhận được yêu cầu của một client để tham gia phiên đấu giá
     * Đầu ra: client tham gia phiên đấu giá
     * ServerUnexpectedPayloadException khi packet nhận được payload sai kiểu
     * AuctionAlreadyRegisteredException khi client đang cố gắng tham gia phiên đấu
     * giá đã tham gia
     * ServerNoAuctionException khi client đang cố gắng tham gia một phiên đấu giá
     * đã kết thúc
     * IOException khi phương thức packet không hoạt động trong auction
     */
    public void joinAuction(PacketMessage packetMessage)
            throws AuctionAlreadyRegisteredException, ServerNoAuctionException, ServerUnexpectedPayloadException,
            IOException {
        // Kiểm tra packet nhận được payload đúng
        if (packetMessage.getPayload() instanceof RegisterClientPayload) {
            // Tạo server instance tạm thời để chứa packet
            AuctionServer server = AuctionServer.getInstance();
            RegisterClientPayload clientRegisterPayload = (RegisterClientPayload) packetMessage.getPayload();
            // Thêm object "client" của clienthandler vào auctionID tương ứng quá server
            server.joinAuction(clientRegisterPayload.getAuctionID(), client);
        } else {
            throw new ServerUnexpectedPayloadException("Packet provided the wrong payload");
        }
    }

}

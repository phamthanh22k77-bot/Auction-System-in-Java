package client.network;

import client.message.MessageType;
import client.message.PacketMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocketManager {

    private static ClientSocketManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocketManager() {
    }

    public static synchronized ClientSocketManager getInstance() {
        if (instance == null) {
            instance = new ClientSocketManager();
        }
        return instance;
    }

    /**
     * Kết nối đến server.
     * - ObjectOutputStream phải khởi tạo TRƯỚC ObjectInputStream
     * (nếu ngược lại cả hai bên sẽ deadlock khi bắt tay header).
     * - Sau khi kết nối, server gửi ngay WELCOME_MESSAGE —
     * đọc và bỏ qua ở đây để các controller không phải lo.
     */
    public void connect(String ip, int port) throws IOException {
<<<<<<< Updated upstream
        if (socket != null && !socket.isClosed())
            return; // đã kết nối

        socket = new Socket(ip, port);

        // Thứ tự quan trọng: out trước, in sau
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // gửi OOS header ngay
        in = new ObjectInputStream(socket.getInputStream());

        // Tiêu thụ WELCOME_MESSAGE server gửi ngay khi accept
        try {
            PacketMessage welcome = (PacketMessage) in.readObject();
            System.out.println("[ClientSocketManager] " + welcome.getType()); // WELCOME_MESSAGE
        } catch (ClassNotFoundException e) {
            System.err.println("[ClientSocketManager] Không đọc được WELCOME_MESSAGE: " + e.getMessage());
=======
        if (socket == null || socket.isClosed()) {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Gửi ngay stream header để tránh deadlock với ObjectInputStream bên server
            in = new ObjectInputStream(socket.getInputStream());
            
            // Xử lý gói tin WELCOME_MESSAGE mà Server gửi ngay khi kết nối thành công
            try {
                PacketMessage welcomeMsg = (PacketMessage) in.readObject();
                if (welcomeMsg != null && welcomeMsg.getType() == client.message.MessageType.WELCOME_MESSAGE) {
                    System.out.println("Kết nối tới Server thành công. Đã nhận: " + welcomeMsg.getType());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
>>>>>>> Stashed changes
        }
    }

    public void sendMessage(PacketMessage message) throws IOException {
        if (out != null) {
            out.writeObject(message);
            out.flush();
            out.reset(); // tránh Java cache object cũ qua các lần gửi liên tiếp
        }
    }

    public PacketMessage receiveMessage() throws IOException, ClassNotFoundException {
        if (in != null) {
            return (PacketMessage) in.readObject();
        }
        return null;
    }

    public void disconnect() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
            out = null;
            in = null;
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }
}
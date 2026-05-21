package client.network;

import client.message.PacketMessage;
import client.message.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// ClientSocketManager (Singleton)
// Quản lý kết nối Socket tới Server, gửi và nhận PacketMessage. Hỗ trợ cơ chế Listener để các Controller đăng ký nhận tin realtime.

public class ClientSocketManager {

    private static volatile ClientSocketManager instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private boolean isRunning;

    // Danh sách các listener đăng ký nhận mọi tin nhắn
    private final List<Consumer<PacketMessage>> listeners = new CopyOnWriteArrayList<>();

    private ClientSocketManager() {
    }

    public static ClientSocketManager getInstance() {
        if (instance == null) {
            synchronized (ClientSocketManager.class) {
                if (instance == null) {
                    instance = new ClientSocketManager();
                }
            }
        }
        return instance;
    }

    // Khởi tạo kết nối tới Server.
    public synchronized boolean connect(String host, int port) {
        if (socket != null && !socket.isClosed())
            return true;

        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            isRunning = true;
            startListener();
            System.out.println("[Client] Đã kết nối tới Server: " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }

    // Bắt đầu luồng lắng nghe tin nhắn từ Server.
    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                while (isRunning) {
                    Object obj = in.readObject();
                    if (obj instanceof PacketMessage) {
                        PacketMessage msg = (PacketMessage) obj;
                        // Thông báo cho tất cả các listener
                        for (Consumer<PacketMessage> listener : listeners) {
                            listener.accept(msg);
                        }
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("[Client] Mất kết nối tới Server: " + e.getMessage());
                    disconnect();
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // Gửi gói tin lên Server.
    public synchronized void sendPacket(PacketMessage packet) {
        if (out == null || socket == null || socket.isClosed()) {
            System.err.println("[Client] Chưa kết nối tới Server.");
            return;
        }
        try {
            out.writeObject(packet);
            out.flush();
            out.reset(); // Xóa cache object cũ để client luôn nhận bản mới nhất, không bị dùng lại bản
                         // cũ
        } catch (IOException e) {
            System.err.println("[Client] Lỗi gửi packet: " + e.getMessage());
        }
    }

    // Đăng ký một listener để nhận tin nhắn.
    public void addMessageListener(Consumer<PacketMessage> listener) {
        listeners.add(listener);
    }

    // Hủy đăng ký listener.
    public void removeMessageListener(Consumer<PacketMessage> listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && isRunning;
    }

    public synchronized void disconnect() {
        isRunning = false;
        try {
            if (socket != null)
                socket.close();
            if (listenerThread != null)
                listenerThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

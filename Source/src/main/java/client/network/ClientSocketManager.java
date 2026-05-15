package client.network;

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

    private ClientSocketManager() {}

    public static synchronized ClientSocketManager getInstance() {
        if (instance == null) {
            instance = new ClientSocketManager();
        }
        return instance;
    }

    public void connect(String ip, int port) throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
    }

    public void sendMessage(PacketMessage message) throws IOException {
        if (out != null) {
            out.writeObject(message);
            out.flush();
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
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

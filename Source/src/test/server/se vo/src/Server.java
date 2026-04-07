import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        InetAddress ip = InetAddress.getLocalHost();
        System.out.println("Server started at:");
        System.out.println("Host name: " + ip.getHostName());
        System.out.println("IP address: " + ip.getHostAddress());
        System.out.println("Port: " + port);
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();

            InetAddress clientIP = clientSocket.getInetAddress();
            System.out.println("Client connected from: " + clientIP.getHostAddress());

            ClientHandler handler = new ClientHandler(clientSocket);
            new Thread(handler).start();
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(1234);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
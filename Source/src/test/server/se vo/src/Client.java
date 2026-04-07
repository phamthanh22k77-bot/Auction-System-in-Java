import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);

        // Lấy thông tin server
        InetAddress serverIP = socket.getInetAddress();
        System.out.println("Connected to server:");
        System.out.println("Host: " + serverIP.getHostName());
        System.out.println("IP: " + serverIP.getHostAddress());

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
        System.out.println("Server reply: " + in.readLine());
    }

    public static void main(String[] args) {
        try {
            Client client = new Client("localhost", 1234);

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String input;

            while ((input = console.readLine()) != null) {
                client.sendMessage(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package server.network;

import server.models.auction.Auction;
import server.models.auction.BidTransaction;
import server.models.item.Item;
import server.models.item.ItemFactory;
import server.models.network.*;
import server.auction.*;
import server.payload.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import client.message.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;

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

        AuctionServer server = AuctionServer.getInstance();

        isRunning = true;

        try {

            objectInputStream = new ObjectInputStream(client.getSocket().getInputStream());

            objectOutputStream = new ObjectOutputStream(client.getSocket().getOutputStream());

            sendPacket(new PacketMessage(WELCOME_MESSAGE, null));

        } catch (IOException e) {

            e.printStackTrace();
            isRunning = false;
        }

        while (isRunning) {
            try {
                PacketMessage packetMessage = (PacketMessage) objectInputStream.readObject();

                switch (packetMessage.getType()) {

                    case REGISTER_IN_AUCTION:
                        // Server received packet indicating the client wishes to register into an
                        // auction
                        try {
                            joinAuction(packetMessage);
                        } catch (AuctionAlreadyRegisteredException e) {
                            e.printStackTrace();
                        } catch (ServerNoAuctionException | ServerUnexpectedPayloadException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case LOGIN_REQUEST:
                        handleLogin(packetMessage);
                        break;

                    case SIGNUP_REQUEST:
                        handleSignup(packetMessage);
                        break;

                    /*
                     * case CANCEL_AUCTION:
                     * try {
                     * cancelAuction(packetMessage);
                     * } catch (ServerNoAuctionException | ServerNotClientOwnerException |
                     * AuctionException | ServerUnexpectedPayloadException e) {
                     * 
                     * sendPacket(new PacketMessage(ERROR, new
                     * ErrorMessagePayload(e.getMessage())));
                     * }
                     * break; chưa có logic
                     */

                    case REQUEST_ACTIVE_AUCTION_LIST:
                        // Server received packet indicating the client wishes to receive a list of
                        // active auctions
                        sendAllAuctions();
                        break;

                    case UNREGISTER_FROM_AUCTION:
                        // Server received a packet indicating the client wishes to unregister from a
                        // specific auction
                        try {
                            leaveAuction(packetMessage);
                        } catch (ServerUnexpectedPayloadException | AuctionHighBidException
                                | AuctionNotRegisteredException | ServerNoAuctionException
                                | AuctionClientIsOwnerException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case DISCONNECT:
                        // Server received a packet indicating the client wishes to disconnect from the
                        // server
                        try {
                            disconnectFromServer();
                        } catch (ServerHasHighBidException | AuctionHighBidException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case REQUEST_HIGHEST_BID:
                        // Server received a packet indicating the client requested the highest bid in
                        // an auction
                        try {
                            requestHighestBid(packetMessage);
                        } catch (ServerNoAuctionException | ServerUnexpectedPayloadException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case CREATE_AUCTION:
                        // Server received a packet indicating the client wishes to create a new auction
                        // Check if the server wishes to accept the creation of new auctions
                        if (AuctionServer.getInstance().isAcceptingAuctions()) {
                            try {
                                createAuction(packetMessage);
                            } catch (ServerUnexpectedPayloadException e) {
                                sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                            }
                        } else {
                            sendPacket(new PacketMessage(ERROR,
                                    new ErrorMessagePayload("Server is not accepting auctions at this time")));
                        }
                        break;

                    case MAKE_BID:
                        // Server received a packet indicating the client wishes to make a bid in an
                        // auction
                        try {
                            makeBid(packetMessage);
                        } catch (ServerUnexpectedPayloadException | AuctionLowBidException
                                | AuctionClientIsOwnerException | AuctionNotRegisteredException
                                | ServerNoAuctionException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    /*
                     * case REQUEST_MY_AUCTIONS:
                     * //Client requested their auctions list
                     * sendMyAuctions(packetMessage);
                     * break; chưa có logic
                     */

                    default:
                        break;
                }

            } catch (IOException e) {
                try {
                    client.getSocket().close();
                    isRunning = false;
                    String clientKey = client.getSocket().getInetAddress().getHostAddress()
                            + ":" +
                            client.getSocket().getPort();

                    server.getClientHandlers().remove(clientKey);
                    for (String auctionID : client.getRegisteredAuctions()) {
                        if (server.getAuctions().containsKey(auctionID)) {
                            try {
                                server.getAuctions().get(auctionID).forcefullyRemoveClient(client);
                            } catch (AuctionNotRegisteredException auctionNotRegisteredException) {
                                auctionNotRegisteredException.printStackTrace();
                            }
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // vào: phương thức nhận một packet msg, thử gửi một packet
    // Đầu ra: packet được gửi đúng chỗ
    public void sendPacket(PacketMessage packetMessage) throws IOException {
        objectOutputStream.writeObject(packetMessage);
        objectOutputStream.flush();
        objectOutputStream.reset();

    }

    private void handleLogin(PacketMessage packetMessage) throws IOException {
        String[] credentials = (String[]) packetMessage.getPayload();
        String username = credentials[0];
        String password = credentials[1];

        try {
            java.util.List<server.models.user.User> users = new server.dao.UserDAO().loadAll();
            server.models.user.User matchedUser = users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password))
                    .findFirst()
                    .orElse(null);

            if (matchedUser != null) {
                sendPacket(new PacketMessage(AUTH_SUCCESS, matchedUser));
            } else {
                sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload("Sai ten dang nhap hoac mat khau.")));
            }
        } catch (Exception e) {
            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload("Loi doc du lieu.")));
        }
    }

    private void handleSignup(PacketMessage packetMessage) throws IOException {
        server.models.user.User newUser = (server.models.user.User) packetMessage.getPayload();
        try {
            new server.dao.UserDAO().them(newUser);
            sendPacket(new PacketMessage(AUTH_SUCCESS, newUser));
        } catch (Exception e) {
            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload("Khong the luu tai khoan.")));
        }
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

    /*
     * Điều kiện trước: Gói tin yêu cầu xem tất cả các phiên đấu giá đang hoạt động
     * đã được nhận từ client.
     * 
     * Điều kiện sau:
     * - Client nhận được danh sách tất cả các phiên đấu giá đang hoạt động từ
     * server.
     * - Nếu xảy ra lỗi trong quá trình gửi dữ liệu, một thông báo lỗi sẽ được gửi
     * đến client.
     */
    public void sendAllAuctions() {
        // Tạo một instance tạm thời của server
        AuctionServer server = AuctionServer.getInstance();

        try {
            // Lấy và gửi tất cả các phiên đấu giá đang hoạt động cho client
            LinkedList<AuctionListItem> auctionListItemAuctionListPayload = server.getAllAuctions();

            server.sendPacket(
                    client,
                    new PacketMessage(
                            SEND_ACTIVE_AUCTION_LIST,
                            new AuctionListPayload(auctionListItemAuctionListPayload)));

        } catch (IOException e) {

            try {
                // Gửi thông báo lỗi nếu không thể gửi danh sách phiên đấu giá
                server.sendPacket(
                        client,
                        new PacketMessage(
                                ERROR,
                                new ErrorMessagePayload("Không thể gửi danh sách các phiên đấu giá đang hoạt động")));

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*
     * Điều kiện trước:
     * - Client cần được xóa khỏi hệ thống.
     * 
     * Điều kiện sau:
     * - Client được xóa thành công khỏi hệ thống nếu không giữ mức giá đấu cao nhất
     * trong bất kỳ phiên đấu giá nào.
     * - Việc lắng nghe gói tin từ client bị dừng.
     * - Socket của client được đóng.
     * - Đối tượng ClientHandler hiện tại bị xóa khỏi danh sách quản lý của server.
     * 
     * Lưu ý:
     * - ServerHasHighBidException được ném ra nếu client đang giữ mức giá đấu cao
     * nhất
     * trong ít nhất một phiên đấu giá.
     * - IOException được ném ra nếu xảy ra lỗi khi đóng socket.
     */
    public void stopRunning() throws ServerHasHighBidException, IOException {

        // Kiểm tra xem client có đang giữ giá đấu cao nhất trong phiên đấu giá nào
        // không
        if (client.getNumberOfHighBids() <= 0) {

            // Dừng việc lắng nghe các gói tin từ client
            isRunning = false;

            // Đóng socket của client
            client.getSocket().close();

            // Lấy instance của server
            AuctionServer server = AuctionServer.getInstance();

            // Xóa chính ClientHandler hiện tại khỏi danh sách quản lý
            String clientKey = client.getSocket().getInetAddress().getHostAddress()
                    + ":" +
                    client.getSocket().getPort();

            server.getClientHandlers().remove(clientKey);

        } else {

            throw new ServerHasHighBidException(
                    "Không thể ngắt kết nối vì client đang giữ mức giá đấu cao nhất trong ít nhất một phiên đấu giá. Hành động không được phép.");
        }
    }

    /*
     * Điều kiện trước:
     * - Gói tin yêu cầu client hủy đăng ký khỏi một phiên đấu giá đã được nhận.
     * 
     * Điều kiện sau:
     * - Client được xóa thành công khỏi phiên đấu giá tương ứng nếu các điều kiện
     * hợp lệ.
     * 
     * Lưu ý:
     * - AuctionHighBidException được ném ra nếu client đang giữ mức giá đấu cao
     * nhất
     * trong phiên đấu giá.
     * - ServerUnexpectedPayloadException được ném ra nếu gói tin nhận được chứa sai
     * loại payload.
     * - AuctionNotRegisteredException được ném ra nếu client chưa đăng ký trong
     * phiên đấu giá đó.
     * - ServerNoAuctionException được ném ra nếu phiên đấu giá mà client muốn rời
     * khỏi không còn tồn tại.
     * - AuctionClientIsOwnerException được ném ra nếu client yêu cầu hủy đăng ký
     * cũng chính là chủ sở hữu của phiên đấu giá.
     */
    public void leaveAuction(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException,
            AuctionHighBidException,
            AuctionNotRegisteredException,
            ServerNoAuctionException,
            AuctionClientIsOwnerException {

        // Kiểm tra xem gói tin nhận được có chứa đúng loại payload hay không
        if (packetMessage.getPayload() instanceof UnregisterClientPayload) {

            // Lưu tạm payload và instance của server
            UnregisterClientPayload unregisterPayload = (UnregisterClientPayload) packetMessage.getPayload();

            AuctionServer server = AuctionServer.getInstance();

            // Gọi phương thức của auction để xử lý phần còn lại của thao tác rời phiên đấu
            // giá
            server.leaveAuction(unregisterPayload.getAuctionID(), client);

        } else {

            throw new ServerUnexpectedPayloadException(
                    "Gói tin nhận được chứa sai loại payload");
        }
    }

    /*
     * Điều kiện trước:
     * - Client cần được ngắt kết nối khỏi server.
     * 
     * Điều kiện sau:
     * - Client được xóa thành công khỏi server nếu không giữ mức giá đấu cao nhất
     * trong bất kỳ phiên đấu giá nào.
     * - Client được hủy đăng ký khỏi tất cả các phiên đấu giá đang tham gia.
     * - Kết nối socket của client được đóng.
     * 
     * Lưu ý:
     * - AuctionHighBidException được ném ra bởi phương thức leaveAuction()
     * nếu client đang giữ mức giá đấu cao nhất trong một phiên đấu giá.
     * - ServerHasHighBidException được ném ra nếu server phát hiện client
     * đang giữ mức giá đấu cao nhất trong ít nhất một phiên đấu giá
     * khi có yêu cầu ngắt kết nối.
     */
    public void disconnectFromServer()
            throws ServerHasHighBidException, AuctionHighBidException {

        // Lấy instance tạm thời của server
        AuctionServer server = AuctionServer.getInstance();

        // Kiểm tra xem client có giữ mức giá đấu cao nhất trong phiên đấu giá nào không
        if (client.getNumberOfHighBids() > 0) {

            String errorMessage;

            if (client.getNumberOfHighBids() > 1) {

                errorMessage = "Client đang giữ mức giá đấu cao nhất trong "
                        + client.getNumberOfHighBids()
                        + " phiên đấu giá đang hoạt động";

            } else {

                errorMessage = "Client đang giữ mức giá đấu cao nhất trong một phiên đấu giá đang hoạt động";
            }

            throw new ServerHasHighBidException(errorMessage);

        } else {

            // Hủy đăng ký client khỏi tất cả các phiên đấu giá đang tham gia
            for (String auction : client.getRegisteredAuctions()) {

                try {

                    server.leaveAuction(auction, client);

                } catch (ServerNoAuctionException | AuctionNotRegisteredException e) {

                    e.printStackTrace();
                }
            }

            // Thử xóa client và đóng kết nối socket
            try {

                server.removeClient(client);
                client.getSocket().close();

            } catch (IOException
                    | ServerClientHandlerDoesNotExistException
                    | ServerHasHighBidException e) {

                e.printStackTrace();
            }
        }
    }

    /*
     * Điều kiện trước:
     * - Gói tin yêu cầu lấy mức giá đấu cao nhất của một phiên đấu giá đã được nhận
     * từ client.
     * 
     * Điều kiện sau:
     * - Mức giá đấu cao nhất của phiên đấu giá được tìm thấy và gửi về cho client.
     * - Thông tin này được sử dụng cho chức năng làm mới (refresh) trong phần
     * “respond to bids” của client.
     * 
     * Lưu ý:
     * - ServerNoAuctionException được ném ra nếu phiên đấu giá cần truy cập
     * để lấy mức giá đấu cao nhất không còn tồn tại.
     * - ServerUnexpectedPayloadException được ném ra nếu gói tin nhận được
     * chứa sai loại payload.
     */
    public void requestHighestBid(PacketMessage packetMessage)
            throws ServerNoAuctionException,
            ServerUnexpectedPayloadException {

        // Kiểm tra xem gói tin nhận được có đúng loại payload hay không
        if (packetMessage.getPayload() instanceof RequestHighestBidPayload) {

            // Lưu tạm server, mức giá đấu cao nhất, auctionID và payload
            AuctionServer server = AuctionServer.getInstance();

            RequestHighestBidPayload sendHighestBidPayload = (RequestHighestBidPayload) packetMessage.getPayload();

            String auctionID = sendHighestBidPayload.getAuctionID();

            BidTransaction highestBid = server.getHighestBid(auctionID);

            // Tạo gói tin phản hồi chứa mức giá đấu cao nhất
            PacketMessage outputPacketMessage = new PacketMessage(
                    SEND_HIGHEST_BID,
                    new SendHighestBidPayload(
                            highestBid.getTimestamp(),
                            highestBid.getBidAmount(),
                            highestBid.getBidderId(),
                            auctionID));

            // Thử gửi gói tin cho client
            try {

                sendPacket(outputPacketMessage);

            } catch (IOException e) {

                try {

                    server.sendPacket(
                            client,
                            new PacketMessage(
                                    ERROR,
                                    new ErrorMessagePayload(
                                            "Không thể thực hiện yêu cầu lấy mức giá đấu cao nhất.")));

                } catch (IOException ioException) {

                    ioException.printStackTrace();
                }
            }

        } else {

            throw new ServerUnexpectedPayloadException(
                    "Gói tin nhận được chứa sai loại payload");
        }
    }

    /*
     * Điều kiện trước:
     * - Gói tin yêu cầu tạo phiên đấu giá đã được nhận từ client.
     * 
     * Điều kiện sau:
     * - Phiên đấu giá của client được tạo thành công với các thông tin
     * được cung cấp trong gói tin.
     * - Một mã định danh (ID) của phiên đấu giá mới được gửi lại cho client.
     * 
     * Lưu ý:
     * - ServerUnexpectedPayloadException được ném ra nếu gói tin nhận được
     * chứa sai loại payload.
     */
    public void createAuction(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException {

        // Kiểm tra xem gói tin nhận được có chứa đúng loại payload hay không
        if (packetMessage.getPayload() instanceof CreateAuctionPayload) {

            // Lưu tạm instance của server, item, ngày hiện tại,
            // phiên đấu giá mới và payload nhận được
            AuctionServer server = AuctionServer.getInstance();

            CreateAuctionPayload createAuctionPayload = (CreateAuctionPayload) packetMessage.getPayload();

            // Tạo Item mới
            Item item;

            switch (createAuctionPayload.getItemCategory()) {

                case ELECTRONICS:

                    item = ItemFactory.createElectronics(
                            createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(),
                            createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getBrand(),
                            createAuctionPayload.getModel(),
                            createAuctionPayload.getWarranty());

                    break;

                case ART:

                    item = ItemFactory.createArt(
                            createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(),
                            createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getArtist(),
                            createAuctionPayload.getMedium(),
                            createAuctionPayload.getYear());

                    break;

                case VEHICLE:

                    item = ItemFactory.createVehicle(
                            createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(),
                            createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getEngineType(),
                            createAuctionPayload.getModelYear(),
                            createAuctionPayload.getMileage(),
                            createAuctionPayload.getLicensePlate());

                    break;

                default:
                    throw new IllegalArgumentException("Invalid item category");
            }

            // Tạo thời gian hiện tại

            LocalDateTime now = LocalDateTime.now();

            LocalDateTime startTime = createAuctionPayload.getStartTime();

            // Không cho phép startTime trước thời điểm tạo auction
            if (startTime.isBefore(now)) {
                throw new IllegalArgumentException(
                        "Auction start time cannot be before current time.");
            }

            LocalDateTime endTime = startTime.plusMinutes(createAuctionPayload.getAuctionDuration());

            // endTime phải sau startTime
            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException(
                        "Auction end time must be after start time.");
            }

            Auction newAuction = new Auction(
                    // itemId
                    item.getId(),

                    // sellerId
                    client.getSocketAddress()
                            .getAddress()
                            .getHostAddress(),

                    // startTime
                    startTime,

                    // endTime
                    endTime,

                    // startingPrice
                    item.getStartingPrice(),

                    // minimumBidIncrement
                    createAuctionPayload.getMinimumBidIncrement());

            // Liên hệ server để xử lý yêu cầu thêm phiên đấu giá
            server.addAuction(newAuction);

            // Thử gửi ID phiên đấu giá mới cho client
            try {

                sendPacket(
                        new PacketMessage(
                                SEND_AUCTION_ID,
                                new SendAuctionIDPayload(newAuction.getId())));

            } catch (IOException e) {

                e.printStackTrace();
            }

        } else {

            throw new ServerUnexpectedPayloadException(
                    "Gói tin nhận được chứa sai loại payload");
        }
    }

    /*
     * Điều kiện trước:
     * - Gói tin yêu cầu đặt giá đấu cho một phiên đấu giá đã được nhận từ client.
     * 
     * Điều kiện sau:
     * - Client tạo một BidTransaction mới cho phiên đấu giá được chỉ định.
     * - BidTransaction được gửi đến server để xử lý và kiểm tra tính hợp lệ.
     * 
     * Lưu ý:
     * - ServerUnexpectedPayloadException được ném ra nếu gói tin nhận được
     * chứa sai loại payload.
     * - AuctionLowBidException được ném ra nếu mức giá đấu thấp hơn
     * mức giá hợp lệ tối thiểu của phiên đấu giá.
     * - AuctionClientIsOwnerException được ném ra nếu chủ sở hữu phiên đấu giá
     * cố gắng đấu giá trong chính phiên đấu giá của mình.
     * - AuctionNotRegisteredException được ném ra nếu client chưa đăng ký
     * tham gia phiên đấu giá được chỉ định.
     * - ServerNoAuctionException được ném ra nếu phiên đấu giá không tồn tại.
     */
    public void makeBid(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException,
            AuctionLowBidException,
            AuctionClientIsOwnerException,
            AuctionNotRegisteredException,
            ServerNoAuctionException {

        // Kiểm tra payload nhận được có đúng kiểu không
        if (packetMessage.getPayload() instanceof MakeBidPayload) {

            // Lưu tạm server và payload
            AuctionServer server = AuctionServer.getInstance();

            MakeBidPayload newBidPayload = (MakeBidPayload) packetMessage.getPayload();

            // Tạo BidTransaction mới
            BidTransaction newBid = new BidTransaction(

                    // auctionId
                    newBidPayload.getAuctionID(),

                    // bidderId
                    client.getSocketAddress()
                            .getAddress()
                            .getHostAddress(),

                    // bidAmount
                    newBidPayload.getHighestBid());

            // Gửi bid cho server xử lý
            server.auctionBid(
                    newBidPayload.getAuctionID(),
                    newBid,
                    client);

        } else {

            throw new ServerUnexpectedPayloadException(
                    "Packet provided the wrong payload");
        }
    }
}

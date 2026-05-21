package server.network;

import server.models.auction.Auction;
import server.models.auction.BidTransaction;
import server.models.item.Item;
import server.models.item.ItemFactory;
import server.models.network.*;
import server.auction.*;
import server.payload.*;
import server.models.user.Bidder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import client.message.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import static client.message.MessageType.*;

public class ClientHandler extends Thread {
    private AuctionClient client;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private boolean isRunning;
    private server.models.user.User loggedInUser;

    public ClientHandler(AuctionClient serverClient) {
        this.client = serverClient;
        this.isRunning = true;
    }

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

            // Gửi thông điệp chào mừng khi kết nối thành công
            sendPacket(new PacketMessage(WELCOME_MESSAGE, null));

        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }

        while (isRunning) {
            try {
                PacketMessage packetMessage = (PacketMessage) objectInputStream.readObject();

                switch (packetMessage.getType()) {

                    case LOGIN:
                        handleLogin((LoginPayload) packetMessage.getPayload());
                        break;

                    case REGISTER:
                        handleRegister((RegisterPayload) packetMessage.getPayload());
                        break;

                    case REGISTER_IN_AUCTION:
                        try {
                            joinAuction(packetMessage);

                            // Gửi xác nhận đăng ký kèm theo trạng thái hiện tại (Lịch sử đặt giá, Mức giá
                            // cao nhất)
                            String auctionID = ((RegisterClientPayload) packetMessage.getPayload()).getAuctionID();
                            Auction auction = AuctionManager.getInstance().timTheoId(auctionID);
                            if (auction != null) {
                                auction.updateStatus();
                                ConfirmAuctionRegistrationPayload confirm = new ConfirmAuctionRegistrationPayload(
                                        auctionID,
                                        auction.getCurrentHighestBid(), auction.getHighestBidderId(),
                                        new java.util.ArrayList<>(auction.getBidHistory()));
                                sendPacket(new PacketMessage(MessageType.CONFIRM_AUCTION_REGISTRATION, confirm));
                                System.out.println("[Server] Đã gửi xác nhận tham gia phiên " + auctionID + " tới "
                                        + client.getUsername());
                            }
                        } catch (Exception e) {
                            System.err.println("[ClientHandler] Lỗi tham gia phiên: " + e.getMessage());
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case CANCEL_AUCTION:
                        try {
                            cancelAuction(packetMessage);
                        } catch (Exception e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case REQUEST_ACTIVE_AUCTION_LIST:
                        // Server nhận được gói tin yêu cầu gửi danh sách các phiên đấu giá đang hoạt
                        // động
                        sendAllAuctions();
                        break;

                    case REQUEST_MY_AUCTIONS:
                        // Client yêu cầu danh sách các phiên đấu giá của riêng họ
                        sendMyAuctions();
                        break;

                    case BALANCE_UPDATE:
                        handleBalanceUpdate((server.models.user.Bidder) packetMessage.getPayload());
                        break;

                    case REQUEST_ALL_USERS:
                        sendAllUsers();
                        break;

                    case REQUEST_ALL_BIDS:
                        sendAllBids();
                        break;

                    case UNREGISTER_FROM_AUCTION:
                        // Server nhận được gói tin yêu cầu hủy đăng ký khỏi một phiên đấu giá cụ thể
                        try {
                            leaveAuction(packetMessage);
                        } catch (ServerUnexpectedPayloadException | AuctionHighBidException
                                | AuctionNotRegisteredException
                                | ServerNoAuctionException | AuctionClientIsOwnerException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case DISCONNECT:
                        // Server nhận được gói tin yêu cầu ngắt kết nối khỏi server
                        try {
                            disconnectFromServer();
                        } catch (ServerHasHighBidException | AuctionHighBidException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case REQUEST_HIGHEST_BID:
                        // Server nhận được gói tin yêu cầu lấy mức giá đặt cao nhất của một phiên đấu
                        // giá
                        try {
                            requestHighestBid(packetMessage);
                        } catch (ServerNoAuctionException | ServerUnexpectedPayloadException e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case CREATE_AUCTION:
                        // Server nhận được gói tin yêu cầu tạo một phiên đấu giá mới
                        // Kiểm tra xem Server hiện tại có cho phép tạo phiên đấu giá mới hay không
                        if (AuctionServer.getInstance().isAcceptingAuctions()) {
                            try {
                                createAuction(packetMessage);
                            } catch (Exception e) {
                                sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                            }
                        } else {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(
                                     "Hệ thống hiện không chấp nhận tạo phiên đấu giá mới vào lúc này.")));
                        }
                        break;

                    case MAKE_BID:
                        // Server nhận được gói tin yêu cầu thực hiện lượt đặt giá (bid) trong phiên đấu
                        // giá
                        try {
                            makeBid(packetMessage);
                        } catch (Exception e) {
                            sendPacket(new PacketMessage(ERROR, new ErrorMessagePayload(e.getMessage())));
                        }
                        break;

                    case TOGGLE_USER_LOCK:
                        handleToggleUserLock((String) packetMessage.getPayload());
                        break;

                    case REQUEST_BID_HISTORY:
                        handleRequestBidHistory((String) packetMessage.getPayload());
                        break;

                    default:
                        break;
                }

            } catch (IOException e) {
                try {
                    client.getSocket().close();
                    isRunning = false;
                    String clientKey = client.getSocket().getInetAddress().getHostAddress() + ":"
                            + client.getSocket().getPort();

                    server.getClientHandlers().remove(clientKey);
                    for (String auctionID : client.getRegisteredAuctions()) {
                        Auction auction = AuctionManager.getInstance().timTheoId(auctionID);
                        if (auction != null) {
                            try {
                                auction.forcefullyRemoveClient(client);
                            } catch (AuctionNotRegisteredException auctionNotRegisteredException) {
                                // Bỏ qua nếu client chưa đăng ký
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

    private void handleBalanceUpdate(server.models.user.Bidder updatedUser) {
        if (updatedUser == null)
            return;
        try {
            server.dao.UserDAO userDAO = new server.dao.UserDAO();
            List<server.models.user.User> users = userDAO.loadAll();
            for (server.models.user.User u : users) {
                if (u.getUsername().equals(updatedUser.getUsername())) {
                    if (u instanceof server.models.user.Bidder) {
                        Bidder b = (server.models.user.Bidder) u;
                        b.setBalance(updatedUser.getBalance());

                        // Cập nhật lại đối tượng đang giữ trong Handler để makeBid dùng đúng số dư mới
                        if (loggedInUser != null && loggedInUser.getUsername().equals(b.getUsername())) {
                            this.loggedInUser = b;
                        }
                    }
                    break;
                }
            }
            userDAO.saveAll(users);
            System.out.println("[Server] Đã cập nhật số dư cho người dùng: " + updatedUser.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAllUsers() {
        try {
            server.dao.UserDAO userDAO = new server.dao.UserDAO();
            List<server.models.user.User> users = userDAO.loadAll();
            sendPacket(new PacketMessage(SEND_ALL_USERS, new java.util.ArrayList<>(users)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAllBids() {
        List<BidTransaction> allBids = new ArrayList<>();
        for (Auction a : AuctionManager.getInstance().getAuctions()) {
            allBids.addAll(a.getBidHistory());
        }
        try {
            sendPacket(new PacketMessage(SEND_ALL_BIDS, new java.util.ArrayList<>(allBids)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMyAuctions() {
        try {
            String username = (loggedInUser != null) ? loggedInUser.getUsername() : "";
            LinkedList<AuctionListItem> myAuctions = AuctionServer.getInstance().getMyAuctions(username);
            sendPacket(new PacketMessage(MessageType.SEND_MY_AUCTIONS, new AuctionListPayload(myAuctions)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đầu vào: Nhận một đối tượng PacketMessage và thử gửi qua socket
    // Đầu ra: Gói tin được truyền tải thành công tới client
    public void sendPacket(PacketMessage packetMessage) throws IOException {
        objectOutputStream.writeObject(packetMessage);
    }

    // Thực hiện đăng ký cho Client tham gia vào một phiên đấu giá.
    // Kiểm tra đầy đủ tính hợp lệ của gói tin và trạng thái phiên trước khi chấp
    // nhận.
    public void joinAuction(PacketMessage packetMessage) throws AuctionAlreadyRegisteredException,
            AuctionClientIsOwnerException, ServerNoAuctionException, ServerUnexpectedPayloadException, IOException {
        // Kiểm tra packet nhận được payload đúng
        if (packetMessage.getPayload() instanceof RegisterClientPayload) {
            // Tạo server instance tạm thời để chứa packet
            AuctionServer server = AuctionServer.getInstance();
            RegisterClientPayload clientRegisterPayload = (RegisterClientPayload) packetMessage.getPayload();
            // Thêm object "client" của clienthandler vào auctionID tương ứng quá server
            server.joinAuction(clientRegisterPayload.getAuctionID(), client);
        } else {
            throw new ServerUnexpectedPayloadException("Gói tin nhận được chứa sai loại dữ liệu (payload)");
        }
    }

    // Gửi danh sách các phiên đấu giá đang hoạt động tới Client, trả về lỗi nếu kết
    // nối gặp sự cố.
    public void sendAllAuctions() {
        // Tạo một instance tạm thời của server
        AuctionServer server = AuctionServer.getInstance();

        try {
            // Lấy và gửi tất cả các phiên đấu giá đang hoạt động cho client
            LinkedList<AuctionListItem> auctionListItemAuctionListPayload = server.getAllAuctions();

            server.sendPacket(client, new PacketMessage(SEND_ACTIVE_AUCTION_LIST,
                    new AuctionListPayload(auctionListItemAuctionListPayload)));

        } catch (IOException e) {

            try {
                // Gửi thông báo lỗi nếu không thể gửi danh sách phiên đấu giá
                server.sendPacket(client, new PacketMessage(ERROR,
                        new ErrorMessagePayload("Không thể gửi danh sách các phiên đấu giá đang hoạt động")));

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Ngắt kết nối của Client, đóng socket và gỡ bỏ Handler khỏi danh sách quản lý
    // của Server.
    // Chặn hành động nếu Client đang dẫn đầu mức giá của phiên đấu giá nào đó.
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
            String clientKey = client.getSocket().getInetAddress().getHostAddress() + ":"
                    + client.getSocket().getPort();

            server.getClientHandlers().remove(clientKey);

        } else {

            throw new ServerHasHighBidException(
                    "Không thể ngắt kết nối vì client đang giữ mức giá đấu cao nhất trong ít nhất một phiên đấu giá. Hành động không được phép.");
        }
    }

    // Hủy đăng ký của Client khỏi một phiên đấu giá.
    // Kiểm tra đầy đủ điều kiện hợp lệ trước khi cho phép client rời phiên.
    public void leaveAuction(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException, AuctionHighBidException, AuctionNotRegisteredException,
            ServerNoAuctionException, AuctionClientIsOwnerException {

        // Kiểm tra xem gói tin nhận được có chứa đúng loại payload hay không
        if (packetMessage.getPayload() instanceof UnregisterClientPayload) {

            // Lưu tạm payload và instance của server
            UnregisterClientPayload unregisterPayload = (UnregisterClientPayload) packetMessage.getPayload();

            AuctionServer server = AuctionServer.getInstance();

            // Gọi phương thức của auction để xử lý phần còn lại của thao tác rời phiên đấu
            // giá
            server.leaveAuction(unregisterPayload.getAuctionID(), client);

        } else {

            throw new ServerUnexpectedPayloadException("Gói tin nhận được chứa sai loại payload");
        }
    }

    // Rút client khỏi toàn bộ các phiên đang tham gia và ngắt kết nối khỏi Server.
    // Không cho phép thoát nếu Client đang là người giữ giá cao nhất ở bất kỳ phiên
    // nào.
    public void disconnectFromServer() throws ServerHasHighBidException, AuctionHighBidException {

        // Lấy instance tạm thời của server
        AuctionServer server = AuctionServer.getInstance();

        // Kiểm tra xem client có giữ mức giá đấu cao nhất trong phiên đấu giá nào không
        if (client.getNumberOfHighBids() > 0) {

            String errorMessage;

            if (client.getNumberOfHighBids() > 1) {

                errorMessage = "Client đang giữ mức giá đấu cao nhất trong " + client.getNumberOfHighBids()
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

            } catch (IOException | ServerClientHandlerDoesNotExistException | ServerHasHighBidException e) {

                e.printStackTrace();
            }
        }
    }

    // Trả về thông tin lượt đặt giá cao nhất của phiên đấu giá cho Client để làm
    // mới (refresh) giao diện.
    public void requestHighestBid(PacketMessage packetMessage)
            throws ServerNoAuctionException, ServerUnexpectedPayloadException {

        // Kiểm tra xem gói tin nhận được có đúng loại payload hay không
        if (packetMessage.getPayload() instanceof RequestHighestBidPayload) {

            // Lưu tạm server, mức giá đấu cao nhất, auctionID và payload
            AuctionServer server = AuctionServer.getInstance();

            RequestHighestBidPayload sendHighestBidPayload = (RequestHighestBidPayload) packetMessage.getPayload();

            String auctionID = sendHighestBidPayload.getAuctionID();

            BidTransaction highestBid = server.getHighestBid(auctionID);

            // Tạo gói tin phản hồi chứa mức giá đấu cao nhất
            PacketMessage outputPacketMessage = new PacketMessage(SEND_HIGHEST_BID, new SendHighestBidPayload(
                    highestBid.getTimestamp(), highestBid.getBidAmount(), highestBid.getBidderId(), auctionID));

            // Thử gửi gói tin cho client
            try {

                sendPacket(outputPacketMessage);

            } catch (IOException e) {

                try {

                    server.sendPacket(client, new PacketMessage(ERROR,
                            new ErrorMessagePayload("Không thể thực hiện yêu cầu lấy mức giá đấu cao nhất.")));

                } catch (IOException ioException) {

                    ioException.printStackTrace();
                }
            }

        } else {

            throw new ServerUnexpectedPayloadException("Gói tin nhận được chứa sai loại payload");
        }
    }

    // Tạo phiên đấu giá và vật phẩm mới từ yêu cầu của Client, sau đó trả về ID
    // phiên đấu giá vừa tạo.
    public void createAuction(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException, java.io.IOException {

        // Kiểm tra xem gói tin nhận được có chứa đúng loại payload hay không
        if (packetMessage.getPayload() instanceof CreateAuctionPayload) {

            // Lưu tạm instance của server, item, ngày hiện tại, phiên đấu giá mới và
            // payload nhận được
            AuctionServer server = AuctionServer.getInstance();

            CreateAuctionPayload createAuctionPayload = (CreateAuctionPayload) packetMessage.getPayload();

            // Tạo Item mới
            Item item;

            switch (createAuctionPayload.getItemCategory()) {

                case ELECTRONICS:

                    item = ItemFactory.createElectronics(createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(), createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getBrand(), createAuctionPayload.getModel(),
                            createAuctionPayload.getWarranty());

                    break;

                case ART:

                    item = ItemFactory.createArt(createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(), createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getArtist(), createAuctionPayload.getMedium(),
                            createAuctionPayload.getYear());

                    break;

                case VEHICLE:

                    item = ItemFactory.createVehicle(createAuctionPayload.getItemName(),
                            createAuctionPayload.getItemDescription(), createAuctionPayload.getItemStartingPrice(),
                            createAuctionPayload.getEngineType(), createAuctionPayload.getModelYear(),
                            createAuctionPayload.getMileage(), createAuctionPayload.getLicensePlate());

                    break;

                default:
                    throw new IllegalArgumentException("Danh mục vật phẩm không hợp lệ");
            }

            // Lưu vật phẩm vào Manager và DAO để hiển thị đúng Tên thay vì ID
            try {
                ItemManager.getInstance().themItem(item);
            } catch (IOException e) {
                System.err.println("[Server] Lỗi khi lưu vật phẩm: " + e.getMessage());
                sendPacket(new PacketMessage(ERROR,
                        new ErrorMessagePayload("Lỗi server khi lưu vật phẩm: " + e.getMessage())));
                return;
            }

            // Sử dụng Username để đồng bộ với logic getMyAuctions trên Server
            String sellerId = (loggedInUser != null) ? loggedInUser.getUsername() : "Unknown";

            // Tạo thời gian hiện tại
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = createAuctionPayload.getStartTime();

            // Nếu không có startTime, mặc định là ngay bây giờ
            if (startTime == null) {
                startTime = now;
            }

            // Cho phép trễ tối đa 24 giờ để tránh lỗi do lệch múi giờ hoặc điền form lâu
            if (startTime.isBefore(now.minusHours(24))) {
                throw new IllegalArgumentException("Thời gian bắt đầu không thể ở quá khứ quá xa (tối đa 24 giờ).");
            }

            LocalDateTime endTime = startTime.plusMinutes(createAuctionPayload.getAuctionDuration());

            // endTime phải sau startTime
            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("Thời gian kết thúc phiên đấu giá phải sau thời gian bắt đầu.");
            }

            Auction newAuction = new Auction(
                    // itemId
                    item.getId(),

                    // sellerId
                    sellerId,

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

            // Broadcast danh sách mới tới tất cả các client
            broadcastAuctionList();

            // Thử gửi ID phiên đấu giá mới cho client
            try {

                sendPacket(new PacketMessage(SEND_AUCTION_ID, new SendAuctionIDPayload(newAuction.getId())));

            } catch (IOException e) {

                e.printStackTrace();
            }

        } else {

            throw new ServerUnexpectedPayloadException("Gói tin nhận được chứa sai loại payload");
        }
    }

    // Tiến hành xử lý và xác thực giao dịch đặt giá mới từ Client gửi lên.
    public void makeBid(PacketMessage packetMessage)
            throws server.models.network.ServerUnexpectedPayloadException, server.auction.AuctionLowBidException,
            server.auction.AuctionClientIsOwnerException, server.auction.AuctionNotRegisteredException,
            server.models.network.ServerNoAuctionException, java.io.IOException {

        // Kiểm tra payload nhận được có đúng kiểu không
        if (packetMessage.getPayload() instanceof MakeBidPayload) {

            // Lưu tạm server và payload
            AuctionServer server = AuctionServer.getInstance();

            MakeBidPayload newBidPayload = (MakeBidPayload) packetMessage.getPayload();

            // Tạo BidTransaction mới
            BidTransaction newBid = new BidTransaction(newBidPayload.getAuctionID(),
                    loggedInUser != null ? loggedInUser.getUsername() : client.getIP(), newBidPayload.getHighestBid());

            String currentUsername = loggedInUser != null ? loggedInUser.getUsername() : client.getIP();

            // Log lượt bid nhận được
            System.out.println("[Server] Nhận MAKE_BID từ " + currentUsername + ": " + newBidPayload.getHighestBid()
                    + " cho phiên " + newBidPayload.getAuctionID());

            // KIỂM TRA PHIÊN ĐẤU GIÁ TỒN TẠI
            Auction auction = AuctionManager.getInstance().timTheoId(newBidPayload.getAuctionID());

            if (auction == null) {
                System.err.println("[Server] Phiên không tồn tại: " + newBidPayload.getAuctionID());
                sendPacket(
                        new PacketMessage(MessageType.ERROR, new ErrorMessagePayload("Phiên đấu giá không tồn tại!")));
                return;
            }

            newBid.setAuctionId(auction.getId());

            // Khóa phiên đấu giá để ngăn chặn Race Condition
            synchronized (auction) {
                String previousLeaderId = auction.getHighestBidderId();
                double previousLeaderBid = auction.getCurrentHighestBid();

                // Kiểm tra số dư của người hiện tại
                if (loggedInUser instanceof Bidder) {
                    Bidder bidder = (Bidder) loggedInUser;

                    // Nếu đã là người giữ giá cao nhất, số tiền thực tế có = Số dư + Số tiền đang
                    // giam
                    double effectiveBalance = bidder.getBalance();
                    if (currentUsername.equalsIgnoreCase(previousLeaderId)) {
                        effectiveBalance += previousLeaderBid;
                    }

                    if (effectiveBalance < newBid.getBidAmount()) {
                        System.err.println("[Server] " + currentUsername + " không đủ tiền: " + effectiveBalance + " < "
                                + newBid.getBidAmount());
                        sendPacket(new PacketMessage(MessageType.ERROR,
                                new ErrorMessagePayload("Số dư tài khoản không đủ!")));
                        return;
                    }
                }

                // Thực hiện đặt giá tại AuctionManager
                try {
                    server.auctionBid(auction.getId(), newBid, client);
                    System.out.println("[Server] AuctionManager chấp nhận lượt bid của " + currentUsername);
                } catch (Exception e) {
                    System.err.println("[Server] AuctionManager từ chối lượt bid: " + e.getMessage());
                    sendPacket(new PacketMessage(MessageType.ERROR, new ErrorMessagePayload(e.getMessage())));
                    return;
                }

                // Nếu thành công, tiến hành trừ tiền và hoàn tiền
                // Trừ toàn bộ số tiền bid mới của user hiện tại
                updateBalance(currentUsername, -newBid.getBidAmount());

                // Hoàn lại tiền cho người đặt giá trước đó (Bao gồm cả chính mình nếu tự nâng
                // giá)
                if (previousLeaderId != null && !previousLeaderId.isEmpty()) {
                    updateBalance(previousLeaderId, previousLeaderBid);
                }

                // Broadcast cập nhật giá mới
                server.models.item.Item item = ItemManager.getInstance().timTheoId(auction.getItemId());
                String itemDesc = (item != null) ? item.getDescription() : "Không có mô tả";

                AuctionUpdatePayload update = new AuctionUpdatePayload(auction.getId(), LocalDateTime.now(),
                        auction.getCurrentHighestBid(), auction.getItemId(), auction.getHighestBidderId(),
                        itemDesc, auction.getEndTime(), auction.getAntiSnipeCount());

                server.sendPackets(auction.getClientList(), new PacketMessage(MessageType.AUCTION_UPDATE, update));
                System.out.println(
                        "[Server] Đã hoàn tất xử lý và broadcast cho " + auction.getClientList().size() + " clients.");

                // Cập nhật giá trên Dashboard của Seller/Bidder theo thời gian thực khi có
                // người bid mới
                broadcastAuctionList();
            }

        } else {
            throw new ServerUnexpectedPayloadException("Packet provided the wrong payload");
        }
    }

    // Hủy phiên đấu giá và broadcast cho tất cả client.
    private void cancelAuction(PacketMessage packetMessage)
            throws ServerUnexpectedPayloadException, ServerNoAuctionException {
        // Hỗ trợ cả UnregisterClientPayload và String (auctionID)
        String auctionID = null;
        if (packetMessage.getPayload() instanceof UnregisterClientPayload) {
            auctionID = ((UnregisterClientPayload) packetMessage.getPayload()).getAuctionID();
        } else if (packetMessage.getPayload() instanceof String) {
            auctionID = (String) packetMessage.getPayload();
        }

        if (auctionID != null) {
            executeCancel(auctionID);
        } else {
            throw new ServerUnexpectedPayloadException("Payload không hợp lệ cho CANCEL_AUCTION");
        }
    }

    private void executeCancel(String auctionID) throws ServerNoAuctionException {
        try {
            Auction auction = AuctionManager.getInstance().timTheoId(auctionID);
            if (auction == null) {
                throw new ServerNoAuctionException("Không thể hủy phiên: " + auctionID);
            }
            String highestBidder = auction.getHighestBidderId();
            double highestBid = auction.getCurrentHighestBid();

            boolean success = AuctionManager.getInstance().ketThucPhien(auctionID, true);
            if (success) {
                // Hoàn tiền cho người giữ giá cao nhất khi hủy phiên
                if (highestBidder != null && !highestBidder.isEmpty()) {
                    updateBalance(highestBidder, highestBid);
                    System.out.println("[Server] Đã hoàn " + highestBid + " đ cho " + highestBidder + " do phiên "
                            + auctionID + " bị hủy.");
                }
                System.out.println("[Server] Đã hủy phiên: " + auctionID);

                // Gửi thông báo hủy phiên cho các client để cập nhật UI
                AuctionServer.getInstance().broadcast(new PacketMessage(MessageType.AUCTION_CONCLUDED, auctionID));

                broadcastAuctionList();
            } else {
                throw new ServerNoAuctionException("Không thể hủy phiên: " + auctionID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gửi danh sách phiên đấu giá mới nhất tới TẤT CẢ các client đang kết nối.
    private void broadcastAuctionList() {
        AuctionServer server = AuctionServer.getInstance();
        LinkedList<AuctionListItem> list = server.getAllAuctions();
        PacketMessage refreshMsg = new PacketMessage(MessageType.SEND_ACTIVE_AUCTION_LIST,
                new AuctionListPayload(list));
        server.broadcast(refreshMsg);
        System.out.println("[Server] Đã broadcast danh sách đấu giá cập nhật tới tất cả các client.");
    }

    private void handleRequestBidHistory(String auctionId) {
        System.out.println("[Server] Nhận yêu cầu lịch sử đấu giá cho phiên: " + auctionId);
        try {
            Auction auction = AuctionManager.getInstance().timTheoId(auctionId);
            if (auction != null) {
                server.models.item.Item item = server.auction.ItemManager.getInstance().timTheoId(auction.getItemId());
                server.payload.SendBidHistoryPayload payload = new server.payload.SendBidHistoryPayload(auctionId,
                        new java.util.ArrayList<>(auction.getBidHistory()), item);
                sendPacket(new PacketMessage(MessageType.SEND_BID_HISTORY, payload));
            } else {
                System.err.println("[Server] Không tìm thấy phiên đấu giá: " + auctionId);
            }
        } catch (Exception e) {
            System.err.println("[Server] Lỗi handleRequestBidHistory: " + e.getMessage());
        }
    }

    private void handleLogin(LoginPayload payload) throws IOException {
        System.out.println("[Server] Đang xử lý đăng nhập cho: " + payload.getUsername());
        try {
            List<server.models.user.User> users = new server.dao.UserDAO().loadAll();
            server.models.user.User found = users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(payload.getUsername())
                            && u.getPassword().equals(payload.getPassword()))
                    .findFirst().orElse(null);

            if (found != null) {
                if (found.isLocked()) {
                    sendPacket(new PacketMessage(LOGIN_FAILURE,
                            new LoginResponsePayload(false, "Tài khoản của bạn đã bị khóa bởi quản trị viên.", null)));
                    System.out.println("[Server] Từ chối đăng nhập: " + found.getUsername() + " đang bị khóa.");
                    return;
                }
                this.loggedInUser = found;
                this.client.setUsername(found.getUsername()); // [FIX] Đồng bộ username vào client object
                sendPacket(new PacketMessage(LOGIN_SUCCESS,
                        new LoginResponsePayload(true, "Đăng nhập thành công!", found)));
                System.out.println("[Server] Đăng nhập thành công: " + found.getUsername());
            } else {
                sendPacket(new PacketMessage(LOGIN_FAILURE,
                        new LoginResponsePayload(false, "Sai tài khoản hoặc mật khẩu.", null)));
            }
        } catch (Exception e) {
            sendPacket(new PacketMessage(LOGIN_FAILURE,
                    new LoginResponsePayload(false, "Lỗi server: " + e.getMessage(), null)));
        }
    }

    private void handleToggleUserLock(String userId) {
        System.out.println("[Server] Yêu cầu Thay đổi trạng thái khóa cho User ID: " + userId);
        try {
            server.dao.UserDAO dao = new server.dao.UserDAO();
            List<server.models.user.User> users = dao.loadAll();
            boolean found = false;
            for (server.models.user.User u : users) {
                if (u.getId().equals(userId)) {
                    u.setLocked(!u.isLocked());
                    found = true;
                    System.out.println("[Server] User " + u.getUsername() + " hiện tại là: "
                            + (u.isLocked() ? "BỊ KHÓA" : "MỞ KHÓA"));
                    break;
                }
            }
            if (found) {
                dao.saveAll(users);

                // Nếu user bị KHÓA, tìm Handler đang giữ user này và ngắt kết nối ngay lập tức
                for (ClientHandler handler : AuctionServer.getInstance().getClientHandlers().values()) {
                    if (handler.loggedInUser != null && handler.loggedInUser.getId().equals(userId)) {
                        // Cập nhật trạng thái khóa cho đối tượng đang online
                        for (server.models.user.User u : users) {
                            if (u.getId().equals(userId)) {
                                handler.loggedInUser.setLocked(u.isLocked());
                                if (u.isLocked()) {
                                    handler.sendPacket(new PacketMessage(MessageType.ERROR, new ErrorMessagePayload(
                                            "Tài khoản của bạn đã bị khóa bởi quản trị viên. Kết nối sẽ bị ngắt.")));
                                    handler.setRunning(false);
                                    // Cưỡng chế đóng socket để cắt kết nối ngay lập tức
                                    try {
                                        handler.getClient().getSocket().close();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    System.out.println("[Server] Đã cưỡng chế ngắt kết nối user bị khóa: "
                                            + handler.loggedInUser.getUsername());
                                }
                                break;
                            }
                        }
                        break;
                    }
                }

                broadcastToAdmins(new PacketMessage(SEND_ALL_USERS, new java.util.ArrayList<>(users)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cập nhật số dư và thông báo cho Client.
    private void updateBalance(String username, double delta) {
        if (username == null || username.isEmpty() || username.equalsIgnoreCase("unknown"))
            return;

        try {
            server.dao.UserDAO userDAO = new server.dao.UserDAO();
            List<server.models.user.User> allUsers = userDAO.loadAll();

            for (int i = 0; i < allUsers.size(); i++) {
                server.models.user.User u = allUsers.get(i);
                if (u.getUsername().equalsIgnoreCase(username) && u instanceof Bidder) {
                    Bidder bidder = (Bidder) u;
                    if (delta < 0) {
                        bidder.withdraw(-delta);
                    } else {
                        bidder.deposit(delta);
                    }

                    allUsers.set(i, bidder);
                    userDAO.saveAll(allUsers);

                    // Thông báo cho chính ClientHandler đang giữ user này (nếu online)
                    for (ClientHandler handler : AuctionServer.getInstance().getClientHandlers().values()) {
                        if (handler.loggedInUser != null
                                && handler.loggedInUser.getUsername().equalsIgnoreCase(username)) {
                            // Cập nhật lại đối tượng loggedInUser trong handler để đồng bộ
                            handler.loggedInUser = bidder;
                            handler.sendPacket(new PacketMessage(MessageType.BALANCE_UPDATE,
                                    new BalanceUpdatePayload(bidder.getBalance())));
                        }
                    }
                    System.out.println("[Server] Đã cập nhật số dư cho " + username + ": delta=" + delta + ", balance="
                            + bidder.getBalance());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Server] Lỗi updateBalance: " + e.getMessage());
        }
    }

    private void handleRegister(RegisterPayload payload) throws IOException {
        System.out.println("[Server] Đang xử lý đăng ký cho: " + payload.getUsername());
        try {
            server.dao.UserDAO dao = new server.dao.UserDAO();
            List<server.models.user.User> users = dao.loadAll();

            if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(payload.getUsername()))) {
                sendPacket(new PacketMessage(REGISTER_FAILURE,
                        new LoginResponsePayload(false, "Tên đăng nhập đã tồn tại.", null)));
                return;
            }

            server.models.user.User newUser;
            if (payload.getRole().equalsIgnoreCase("Seller")) {
                newUser = new server.models.user.Seller(payload.getUsername(), payload.getEmail(),
                        payload.getPassword(), "");
            } else {
                newUser = new server.models.user.Bidder(payload.getUsername(), payload.getEmail(),
                        payload.getPassword(), 50000000.0); // Cấp 50 triệu VNĐ số dư ban đầu để phục vụ chạy thử nghiệm
                                                            // (test) hệ thống
            }

            dao.them(newUser);
            this.loggedInUser = newUser;
            this.client.setUsername(newUser.getUsername()); // [FIX] Đồng bộ username vào client object
            sendPacket(new PacketMessage(REGISTER_SUCCESS,
                    new LoginResponsePayload(true, "Đăng ký thành công!", newUser)));
            System.out.println("[Server] Đăng ký thành công: " + newUser.getUsername());

            // Broadcast danh sách user mới cập nhật tới tất cả các Admin đang online
            try {
                List<server.models.user.User> updatedUsers = dao.loadAll();
                broadcastToAdmins(new PacketMessage(SEND_ALL_USERS, new java.util.ArrayList<>(updatedUsers)));
            } catch (Exception ex) {
                System.err.println("[Server] Lỗi cập nhật danh sách user cho Admin sau khi đăng ký: " + ex.getMessage());
            }
        } catch (Exception e) {
            sendPacket(new PacketMessage(REGISTER_FAILURE,
                    new LoginResponsePayload(false, "Lỗi server: " + e.getMessage(), null)));
        }
    }

    private void broadcastToAdmins(PacketMessage packet) {
        for (ClientHandler handler : AuctionServer.getInstance().getClientHandlers().values()) {
            if (handler.loggedInUser != null && "ADMIN".equalsIgnoreCase(handler.loggedInUser.getRole())) {
                try {
                    handler.sendPacket(packet);
                } catch (IOException e) {
                    System.err.println("[Server] Lỗi broadcast to Admin: " + e.getMessage());
                }
            }
        }
    }
}

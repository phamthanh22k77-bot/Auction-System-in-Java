package client.controllers;

import client.network.ClientSocketManager;
import client.message.PacketMessage;
import client.message.MessageType;
import server.payload.MakeBidPayload;
import server.payload.AuctionUpdatePayload;
import server.payload.BalanceUpdatePayload;
import server.payload.RegisterClientPayload;
import server.payload.UnregisterClientPayload;
import server.auction.BidObserver;
import server.models.auction.Auction;

import client.controllers.BidPriceLineChart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Điều khiển màn hình phòng đấu giá trực tiếp (BiddingController).
 * Giao diện tương ứng: BiddingScreen.fxml
 * Luồng sử dụng:
 * 1. Màn hình trước (ItemDetail hoặc BidderDashboard) gọi hàm setAuction() truyền dữ liệu sang trước khi hiển thị.
 * 2. initialize() thực hiện khởi tạo biểu đồ diễn biến giá, đếm ngược thời gian và đèn tín hiệu nhấp nháy trực tiếp (Live).
 * 3. Server gọi onBidUpdate() qua cơ chế Socket Client khi có thầu mới -> tự động vẽ lại biểu đồ và cập nhật danh sách thầu.
 * 4. onAuctionClosed() tự động vô hiệu hóa toàn bộ nút đặt giá và hiển thị hộp thoại chúc mừng nếu người dùng chiến thắng.
 */
public class BiddingController implements Initializable, BidObserver {

    // ═════════════════════════════════════════════════════════
    // THANH TRÊN CÙNG (TOP BAR)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblItemName;
    @FXML
    private Label lblLiveIndicator;
    @FXML
    private Label lblBalance;

    // ═════════════════════════════════════════════════════════
    // KHUNG THÔNG TIN BÊN TRÁI (LEFT PANEL)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblCurrentPrice;
    @FXML
    private Label lblHighestBidder;
    @FXML
    private Label lblCountdown;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblSeller;
    @FXML
    private Label lblStartPrice;
    @FXML
    private Label lblEndTime;
    @FXML
    private ListView<String> listLiveBids;

    // ═════════════════════════════════════════════════════════
    // BIỂU ĐỒ DIỄN BIẾN GIÁ ĐẤU (CHART)
    // ═════════════════════════════════════════════════════════
    @FXML
    private LineChart<String, Number> chartBidHistory;
    @FXML
    private CategoryAxis axisTime;
    @FXML
    private NumberAxis axisPrice;

    // ═════════════════════════════════════════════════════════
    // PHẦN NHẬP GIÁ THẦU (BID INPUT)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Button btnQuick1; // +100k
    @FXML
    private Button btnQuick2; // +500k
    @FXML
    private Button btnQuick3; // +1tr
    @FXML
    private Button btnQuick4; // +5tr
    @FXML
    private TextField txtBidAmount;
    @FXML
    private Button btnPlaceBid;
    @FXML
    private Label lblBidFeedback;

    // ═════════════════════════════════════════════════════════
    // CÁC ĐỐI TƯỢNG HỖ TRỢ (HELPER OBJECTS)
    // ═════════════════════════════════════════════════════════
    private BidPriceLineChart bidChart;
    private Timeline countdownTimer;
    private Timeline liveBlinkTimer;

    // ═════════════════════════════════════════════════════════
    // DỮ LIỆU PHIÊN ĐẤU GIÁ HIỆN TẠI (DATA)
    // ═════════════════════════════════════════════════════════
    private String auctionId = "";
    private String itemName = "Phiên đấu giá";
    private double currentHighestBid = 0;
    private double userBalance = 0;
    private long secondsLeft = 0;
    private String itemDescription = "";
    private String rawEndTime = "";
    private int previousAntiSnipeCount = 0; // Theo dõi số lần gia hạn từ Server để kích hoạt cảnh báo

    // Lưu tham chiếu listener để có thể gỡ đăng ký chính xác (tránh rò rỉ do lambda tạo object mới mỗi lần)
    private java.util.function.Consumer<client.message.PacketMessage> socketListener;

    // Các bước thầu nhanh tương ứng với các nút bấm trên giao diện
    private static final double[] QUICK_STEPS = {
            100_000,   // btnQuick1: +100k
            500_000,   // btnQuick2: +500k
            1_000_000, // btnQuick3: +1tr
            5_000_000  // btnQuick4: +5tr
    };

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart();
        setupLiveBlink();
        setupBidInputListener();

        // Đăng ký lắng nghe các gói tin thời gian thực từ Server qua Socket
        // Lưu vào field để có thể gỡ chính xác khi rời màn hình
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);
    }

    private void handleServerMessage(PacketMessage msg) {
        switch (msg.getType()) {
            case AUCTION_UPDATE:
                AuctionUpdatePayload update = (AuctionUpdatePayload) msg.getPayload();

                // Đảm bảo gói tin cập nhật thuộc đúng phiên đấu giá đang xem
                if (update.getAuctionID().equalsIgnoreCase(this.auctionId)) {
                    final boolean[] isExtended = { false };

                    // [ANTI-SNIPING] Tự động đồng bộ thời gian và phát hiện gia hạn
                    if (update.getEndTime() != null) {
                        this.rawEndTime = update.getEndTime().toString();
                        long serverSeconds = java.time.Duration.between(LocalDateTime.now(), update.getEndTime())
                                .getSeconds();

                        // Kích hoạt trạng thái gia hạn dựa trên biến đếm phản hồi từ Server
                        if (update.getAntiSnipeCount() > this.previousAntiSnipeCount) {
                            isExtended[0] = true;
                        }

                        this.secondsLeft = serverSeconds;
                        this.previousAntiSnipeCount = update.getAntiSnipeCount();
                    }

                    // Tự động kiểm tra xem có bị vượt giá hay không để đưa vào lịch sử thông báo
                    String currentUser = client.controllers.SessionManager.getInstance().getUsername();
                    boolean wasHighest = lblHighestBidder.getText().equals("Bạn đang giữ giá!");
                    boolean isMeNew = update.getHighestBidderIP().equalsIgnoreCase(currentUser);
                    if (wasHighest && !isMeNew && update.getHighestBid() > this.currentHighestBid) {
                        String outbidMsg = "⚠️ Bạn đã bị vượt giá ở phiên [" + this.itemName + "]! Mức giá mới: " + fmt(update.getHighestBid()) + " đ.";
                        SessionManager.getInstance().addNotification(outbidMsg);
                    }

                    // Cập nhật giao diện người dùng thời gian thực
                    Platform.runLater(() -> {
                        if (update.getEndTime() != null) {
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
                            lblEndTime.setText("Kết thúc: " + update.getEndTime().format(dtf));
                        }

                        // Cập nhật mô tả của sản phẩm nếu người bán có thay đổi
                        if (update.getItemDescription() != null && !update.getItemDescription().isEmpty()) {
                            this.itemDescription = update.getItemDescription();
                        }

                        onBidPlaced(null, update.getHighestBidderIP(), update.getHighestBid());

                        // Tạo chuỗi thông báo kết quả thầu trực quan
                        String statusMsg = "";

                        if (update.getHighestBidderIP().equalsIgnoreCase(currentUser)) {
                            statusMsg = "✅ Đặt giá thành công: " + fmt(update.getHighestBid()) + " đ";
                        } else {
                            statusMsg = "Người khác vừa đặt giá: " + fmt(update.getHighestBid()) + " đ";
                        }

                        if (isExtended[0]) {
                            statusMsg = "⚠️ GIA HẠN THỜI GIAN (Lần " + update.getAntiSnipeCount() + "/5)! " + statusMsg;
                        } else if (update.getAntiSnipeCount() >= 5) {
                            statusMsg = "🛑 HẾT LƯỢT GIA HẠN! " + statusMsg;
                        }

                        showFeedback(statusMsg, true);
                    });
                }
                break;
            case BALANCE_UPDATE:
                BalanceUpdatePayload balancePayload = (BalanceUpdatePayload) msg.getPayload();
                System.out.println("[Client] Nhận cập nhật số dư mới: " + balancePayload.getNewBalance());
                Platform.runLater(() -> {
                    this.userBalance = balancePayload.getNewBalance();
                    lblBalance.setText(fmt(this.userBalance) + " đ");

                    // Đồng bộ số dư mới vào Session hệ thống để hiển thị thống nhất ở các popup
                    if (client.controllers.SessionManager.getInstance()
                            .getCurrentUser() instanceof server.models.user.Bidder) {
                        ((server.models.user.Bidder) client.controllers.SessionManager.getInstance().getCurrentUser())
                                .setBalance(this.userBalance);
                    }
                });
                break;
            case ERROR:
                server.payload.ErrorMessagePayload err = (server.payload.ErrorMessagePayload) msg.getPayload();
                Platform.runLater(() -> showFeedback(err.getErrorMessage(), false));
                break;
            case NOTIFY_AUCTION_WINNER:
                server.payload.AuctionUpdatePayload winnerPayload = (server.payload.AuctionUpdatePayload) msg.getPayload();
                if (winnerPayload.getAuctionID().equalsIgnoreCase(this.auctionId)) {
                    onAuctionClosed(winnerPayload.getHighestBidderIP(), winnerPayload.getHighestBid(), true);
                }
                break;
            case NOTIFY_NO_AUCTION_WINNER:
                String closedId = (String) msg.getPayload();
                if (closedId.equalsIgnoreCase(this.auctionId)) {
                    onAuctionClosed("", 0, true);
                }
                break;
            case CONFIRM_AUCTION_REGISTRATION:
                server.payload.ConfirmAuctionRegistrationPayload confirm = (server.payload.ConfirmAuctionRegistrationPayload) msg
                        .getPayload();
                if (confirm.getAuctionID().equalsIgnoreCase(this.auctionId)) {
                    System.out.println(
                            "[Client] Nhận xác nhận tham gia. Số lượt thầu lịch sử: " + confirm.getBidHistory().size());
                    Platform.runLater(() -> {
                        this.currentHighestBid = confirm.getCurrentPrice();
                        lblCurrentPrice.setText(fmt(this.currentHighestBid) + " đ");
                        if (confirm.getHighestBidderId() != null && !confirm.getHighestBidderId().isEmpty()) {
                            lblHighestBidder.setText(confirm.getHighestBidderId());
                        }

                        // Làm sạch danh sách thầu giả lập cũ và nạp lại lịch sử thầu thực tế từ Server
                        listLiveBids.getItems().clear();
                        if (confirm.getBidHistory() != null) {
                            for (server.models.auction.BidTransaction bid : confirm.getBidHistory()) {
                                addBidToUI(bid.getTimestamp(), bid.getBidderId(), bid.getBidAmount());
                            }
                        }
                    });
                }
                break;
            case AUCTION_CONCLUDED:
                break;
            default:
                break;
        }
    }

    // ═════════════════════════════════════════════════════════
    // KHỞI TẠO BIỂU ĐỒ DIỄN BIẾN GIÁ (setupChart)
    // ═════════════════════════════════════════════════════════
    private void setupChart() {
        bidChart = new BidPriceLineChart(chartBidHistory, axisTime, axisPrice);
        bidChart.setMaxPoints(20);
    }

    // ═════════════════════════════════════════════════════════
    // HIỆU ỨNG NHẤP NHÁY ĐÈN TÍN HIỆU "TRỰC TIẾP" (setupLiveBlink)
    // ═════════════════════════════════════════════════════════
    private void setupLiveBlink() {
        liveBlinkTimer = new Timeline(
                new KeyFrame(Duration.ZERO,
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(500),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(1000),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")));
        liveBlinkTimer.setCycleCount(Timeline.INDEFINITE);
        liveBlinkTimer.play();
    }

    // ═════════════════════════════════════════════════════════
    // XÓA PHẢN HỒI KHI NGƯỜI DÙNG BẮT ĐẦU NHẬP LẠI (setupBidInputListener)
    // ═════════════════════════════════════════════════════════
    private void setupBidInputListener() {
        txtBidAmount.textProperty().addListener((obs, old, nw) -> {
            lblBidFeedback.setText("");
        });
    }

    // ═════════════════════════════════════════════════════════
    // API: NHẬN DỮ LIỆU TRUYỀN VÀO TỪ MÀN HÌNH TRƯỚC
    // ═════════════════════════════════════════════════════════

    /**
     * Điền toàn bộ thông tin chi tiết phiên đấu giá khi bắt đầu truy cập phòng đấu thầu.
     */
    public void setAuction(String auctionId, String name, String category, String seller, String endTimeStr,
                           double startPrice, double currentHighest, double balance, long totalSeconds, String status,
                           String description) {
        this.auctionId = auctionId;
        this.itemName = name;
        this.currentHighestBid = currentHighest;
        this.userBalance = balance;
        this.itemDescription = description;
        this.rawEndTime = endTimeStr;

        // Tính toán thời gian thực tế secondsLeft dựa trên endTime để đảm bảo đồng bộ
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            this.secondsLeft = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
        } catch (Exception e) {
            this.secondsLeft = totalSeconds;
        }

        // Đổ dữ liệu thô lên các thẻ UI
        lblItemName.setText(name);
        lblCategory.setText("Danh mục: " + category);
        lblSeller.setText("Người bán: " + seller);
        lblStartPrice.setText("Giá khởi: " + fmt(startPrice) + " đ");
        lblEndTime.setText("Kết thúc: " + endTimeStr);
        lblBalance.setText(fmt(balance) + " đ");
        lblCurrentPrice.setText(fmt(currentHighest) + " đ");
        lblHighestBidder.setText("Đang chờ lượt đặt...");

        // Làm sạch dữ liệu cũ / dữ liệu demo trước khi kết nối mạng
        listLiveBids.getItems().clear();
        bidChart.clear();

        // Kiểm tra nhanh trạng thái phiên đấu giá
        if ("CANCELED".equalsIgnoreCase(status) || "FINISHED".equalsIgnoreCase(status)) {
            onAuctionClosed("", currentHighest, false);
            if ("CANCELED".equalsIgnoreCase(status)) {
                showFeedback("Phiên này đã bị hủy bởi người bán.", false);
            }
            return;
        }

        // Khởi động đồng hồ đếm ngược thời gian thực
        startCountdown(totalSeconds);

        // Đăng ký lắng nghe sự kiện của phiên đấu này với Server để nhận cập nhật tự động
        ClientSocketManager.getInstance()
                .sendPacket(new PacketMessage(MessageType.REGISTER_IN_AUCTION, new RegisterClientPayload(auctionId)));
    }

    // ═════════════════════════════════════════════════════════
    // CẬP NHẬT GIAO DIỆN KHI CÓ LƯỢT ĐẶT MỚI (onBidPlaced)
    // ═════════════════════════════════════════════════════════
    @Override
    public void onBidPlaced(server.models.auction.Auction auction, String bidderId, double bidAmount) {
        addBidToUI(java.time.LocalDateTime.now(), bidderId, bidAmount);
    }

    private void addBidToUI(java.time.LocalDateTime timestamp, String bidderId, double bidAmount) {
        Platform.runLater(() -> {
            this.currentHighestBid = bidAmount;

            // Cập nhật nhãn giá cao nhất hiện tại
            lblCurrentPrice.setText(fmt(bidAmount) + " đ");

            String currentUser = SessionManager.getInstance().getCurrentUser() != null
                    ? SessionManager.getInstance().getCurrentUser().getUsername() : "";

            // So sánh thông tin người đặt thầu xem có phải bản thân mình hay không
            boolean isMe = bidderId.equalsIgnoreCase(currentUser);
            String bidderDisplay = isMe ? "Bạn" : "Người thầu: " + bidderId;
            lblHighestBidder.setText("↑ " + bidderDisplay);

            // Thêm điểm thầu mới vào biểu đồ thầu
            bidChart.addBidPoint(bidAmount);

            // Thêm bản ghi thầu vào thanh feed lịch sử trực tiếp bên dưới
            LocalTime displayTime = (timestamp != null) ? timestamp.toLocalTime() : LocalTime.now();
            String entry = displayTime.format(TIME_FMT) + "   " + bidderDisplay + "   " + fmt(bidAmount) + " đ";
            listLiveBids.getItems().add(0, entry);

            // Giới hạn lịch sử thầu hiển thị tối đa 50 lượt để tối ưu bộ nhớ RAM
            if (listLiveBids.getItems().size() > 50) {
                listLiveBids.getItems().remove(50);
            }

            // Kích hoạt hiệu ứng nhấp nháy đèn tín hiệu trực quan
            triggerLivePulse();
        });
    }

    public void onBidUpdate(String bidderName, double newPrice) {
        onBidPlaced(null, bidderName, newPrice);
    }

    /**
     * Vô hiệu hóa toàn bộ công cụ đặt thầu và hiển thị kết quả chung cuộc khi phiên kết thúc.
     */
    public void onAuctionClosed(String winnerName, double finalPrice, boolean showDialog) {
        Platform.runLater(() -> {
            // Dừng đếm ngược
            if (countdownTimer != null)
                countdownTimer.stop();
            lblCountdown.setText("KẾT THÚC");
            lblCountdown.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 22; -fx-font-weight: bold;");

            // Khóa toàn bộ các nút thầu và ô nhập liệu
            btnPlaceBid.setDisable(true);
            txtBidAmount.setDisable(true);
            btnQuick1.setDisable(true);
            btnQuick2.setDisable(true);
            btnQuick3.setDisable(true);
            btnQuick4.setDisable(true);

            // Xác định kết quả và thông báo phản hồi
            String currentUser = client.controllers.SessionManager.getInstance().getUsername();
            boolean isWinner = winnerName.equalsIgnoreCase(currentUser);

            String result = winnerName.isEmpty() ? "Phiên đã kết thúc — Không có ai đặt giá."
                    : (isWinner ? "CHÚC MỪNG! Bạn đã thắng phiên đấu giá này!" : "Người thắng chung cuộc: " + winnerName) + " — "
                    + fmt(finalPrice) + " đ";
            showFeedback(result, true);

            if (showDialog) {
                if (isWinner) {
                    String winMsg = "🏆 Chúc mừng! Bạn đã thắng phiên [" + this.itemName + "] với mức giá " + fmt(finalPrice) + " đ!";
                    SessionManager.getInstance().addNotification(winMsg);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Chiến thắng!");
                    alert.setHeaderText("Chúc mừng bạn!");
                    alert.setContentText(winMsg);
                    alert.showAndWait();
                } else {
                    String loseMsg = winnerName.isEmpty()
                            ? "🛑 Phiên [" + this.itemName + "] kết thúc mà không có ai đặt giá."
                            : "🛑 Bạn đã không thắng phiên [" + this.itemName + "]. Người thắng: " + winnerName + " với giá " + fmt(finalPrice) + " đ.";
                    SessionManager.getInstance().addNotification(loseMsg);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Kết quả đấu giá");
                    alert.setHeaderText("Phiên đấu giá kết thúc");
                    if (winnerName.isEmpty()) {
                        alert.setContentText("Phiên đấu giá đã kết thúc mà không có người thắng cuộc (không có lượt đặt giá).");
                    } else {
                        alert.setContentText("Tiếc quá! Bạn đã không thắng phiên đấu giá này.\nNgười thắng chung cuộc: " + winnerName + "\nMức giá cuối cùng: " + fmt(finalPrice) + " đ.");
                    }
                    alert.showAndWait();
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // ĐỒNG HỒ ĐẾM NGƯỢC (COUNTDOWN)
    // ═════════════════════════════════════════════════════════
    private void startCountdown(long totalSeconds) {
        if (countdownTimer != null)
            countdownTimer.stop();

        this.secondsLeft = totalSeconds;

        if (this.secondsLeft <= 0) {
            lblCountdown.setText("00 : 00 : 00");
            onAuctionClosed("", 0, false);
            return;
        }

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            this.secondsLeft--;

            if (this.secondsLeft <= 0) {
                lblCountdown.setText("00 : 00 : 00");
                countdownTimer.stop();
                onAuctionClosed("", 0, false);
                return;
            }

            long h = this.secondsLeft / 3600;
            long m = (this.secondsLeft % 3600) / 60;
            long s = this.secondsLeft % 60;
            lblCountdown.setText(String.format("%02d : %02d : %02d", h, m, s));

            // Tự động chuyển đèn đếm ngược sang màu đỏ khi thời gian còn dưới 5 phút
            if (this.secondsLeft <= 300) {
                lblCountdown.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 22; -fx-font-weight: bold;");
            } else {
                lblCountdown.setStyle("-fx-text-fill: #D35400; -fx-font-size: 22; -fx-font-weight: bold;");
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    // ═════════════════════════════════════════════════════════
    // HIỆU ỨNG NHẤP NHÁY THẦU MỚI (LIVE PULSE)
    // ═════════════════════════════════════════════════════════
    private void triggerLivePulse() {
        liveBlinkTimer.stop();
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(100),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(200),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(300),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
                new KeyFrame(Duration.millis(400),
                        e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")));
        pulse.setCycleCount(1);
        pulse.setOnFinished(e -> liveBlinkTimer.play());
        pulse.play();
    }

    // ═════════════════════════════════════════════════════════
    // SỰ KIỆN XỬ LÝ NÚT BẤM GIAO DIỆN (FXML HANDLERS)
    // ═════════════════════════════════════════════════════════

    // Đọc bước nhảy thầu từ mảng QUICK_STEPS để tự động thầu nhanh
    @FXML
    private void handleQuickBid(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();

        double step;
        if (src == btnQuick1)
            step = QUICK_STEPS[0]; // +100k
        else if (src == btnQuick2)
            step = QUICK_STEPS[1]; // +500k
        else if (src == btnQuick3)
            step = QUICK_STEPS[2]; // +1tr
        else
            step = QUICK_STEPS[3]; // +5tr

        // Đề xuất giá thầu mới = mức thầu cao nhất hiện tại + bước nhảy nhanh
        double suggested = currentHighestBid + step;

        // Điền vào ô nhập thầu tự động và gọi mở popup xác nhận ngay
        txtBidAmount.setText(String.format("%.0f", suggested));
        handlePlaceBid();
    }

    // Mở Popup xác nhận trước khi thực hiện đặt giá thầu
    @FXML
    private void handlePlaceBid() {
        // 1. Phân tích số tiền thầu người dùng nhập vào
        double amount = 0;
        String raw = txtBidAmount.getText().replace(",", "").replace(".", "").replace(" ", "").trim();
        if (!raw.isEmpty()) {
            try {
                amount = Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                showFeedback("Số tiền không hợp lệ. Vui lòng kiểm tra lại ký tự số.", false);
                return;
            }
        }

        // 2. Mở cửa sổ popup xác nhận đặt thầu
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/BidConfirmation.fxml"));
            Parent root = loader.load();

            BidConfirmationController ctrl = loader.getController();
            ctrl.setAuctionInfo(auctionId, itemName, currentHighestBid, userBalance, amount);

            Stage popup = new Stage();
            popup.initOwner(btnPlaceBid.getScene().getWindow());
            popup.initStyle(javafx.stage.StageStyle.DECORATED);
            popup.setScene(new Scene(root));
            popup.show();

            txtBidAmount.clear();
            lblBidFeedback.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            showFeedback("Không thể mở màn hình xác nhận: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleBack() {
        // Xác định trạng thái trước khi cleanup() dừng timer và xóa listener
        String currentStatus;
        String countdownText = lblCountdown.getText();
        if (countdownText.equals("KẾT THÚC")) {
            currentStatus = lblBidFeedback.getText().contains("bị hủy") ? "CANCELED" : "FINISHED";
        } else {
            currentStatus = "RUNNING";
        }

        cleanup(); // Dừng timer + gỡ listener sau khi đã lấy xong dữ liệu

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/ItemDetail.fxml"));
            Parent root = loader.load();

            // Điền dữ liệu NGAY SAU load() để currentAuctionId được set trước khi
            // bất kỳ gói tin nào từ Server kịp kích hoạt handleServerMessage
            ItemDetailController ctrl = loader.getController();
            ctrl.setAuctionData(auctionId, itemName, lblCategory.getText().replace("Danh mục: ", ""),
                    lblStartPrice.getText().replace("Giá khởi: ", "").replace(" đ", ""), fmt(currentHighestBid),
                    this.rawEndTime,
                    currentStatus, lblSeller.getText().replace("Người bán: ", ""), itemDescription);

            Stage stage = (Stage) btnPlaceBid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // PHƯƠNG THỨC HỖ TRỢ (HELPERS)
    // ═════════════════════════════════════════════════════════

    /**
     * Định dạng số thực thành chuỗi tiền tệ phân tách dễ nhìn.
     */
    private String fmt(double amount) {
        return String.format("%,.0f", amount);
    }

    /**
     * Hiển thị thông báo phản hồi lỗi hoặc thành công lên giao diện phòng đấu thầu.
     */
    private void showFeedback(String msg, boolean success) {
        lblBidFeedback.setText(msg);

        boolean isWarning = msg.contains("GIA HẠN") || msg.contains("HẾT LƯỢT");

        String color = isWarning ? "#D32F2F" : (success ? "#27AE60" : "#E74C3C");
        double fontSize = isWarning ? 14 : 12;

        lblBidFeedback
                .setStyle("-fx-font-size: " + fontSize + "; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        // Nhấp nháy dòng phản hồi cảnh báo quan trọng 3 lần
        if (isWarning) {
            Timeline blink = new Timeline(new KeyFrame(Duration.seconds(0.2), e -> lblBidFeedback.setVisible(false)),
                    new KeyFrame(Duration.seconds(0.4), e -> lblBidFeedback.setVisible(true)));
            blink.setCycleCount(3);
            blink.play();
        }
    }

    /**
     * Dừng tất cả bộ đếm thời gian và gỡ trình lắng nghe trước khi rời khỏi màn hình.
     */
    private void cleanup() {
        if (countdownTimer != null)
            countdownTimer.stop();
        if (liveBlinkTimer != null)
            liveBlinkTimer.stop();

        // Gỡ lắng nghe gói tin từ Server — dùng field để remove() tìm đúng instance củ (tránh rò rỉ)
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        // Hủy đăng ký nhận cập nhật của phiên này với Server
        ClientSocketManager.getInstance().sendPacket(
                new PacketMessage(MessageType.UNREGISTER_FROM_AUCTION, new UnregisterClientPayload(auctionId)));
    }
}

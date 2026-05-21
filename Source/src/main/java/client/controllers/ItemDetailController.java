package client.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Điều khiển màn hình chi tiết sản phẩm đấu giá (ItemDetailController).
 * Giao diện tương ứng: ItemDetail.fxml
 */
public class ItemDetailController implements Initializable {

    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblItemName;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblStartPrice;
    @FXML
    private Label lblCurrentPrice;
    @FXML
    private Label lblSeller;
    @FXML
    private VBox vboxSpecFields;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblCountdown;
    @FXML
    private Label lblStatus;
    @FXML
    private ListView<String> listBidHistory;
    @FXML
    private Button btnEnterBidding;

    // ═════════════════════════════════════════════════════════
    // TRẠNG THÁI HOẠT ĐỘNG (STATE)
    // ═════════════════════════════════════════════════════════
    private Timeline countdownTimer;
    private String currentAuctionId;
    private String currentCategory;
    private String rawEndTime;
    private int previousAntiSnipeCount = 0;

    // Lưu tham chiếu listener để có thể gỡ đăng ký chính xác (tránh rò rỉ do lambda tạo object mới mỗi lần)
    private java.util.function.Consumer<client.message.PacketMessage> socketListener;

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Đăng ký lắng nghe cập nhật trạng thái thời gian thực từ Server qua Socket
        // Lưu vào field để có thể gỡ chính xác khi chuyển màn hình
        socketListener = this::handleServerMessage;
        client.network.ClientSocketManager.getInstance().addMessageListener(socketListener);
    }

    // ═════════════════════════════════════════════════════════
    // XỬ LÝ GÓI TIN TỪ SERVER (SOCKET MESSAGE HANDLER)
    // ═════════════════════════════════════════════════════════
    private void handleServerMessage(client.message.PacketMessage msg) {
        if (msg.getType() == client.message.MessageType.AUCTION_UPDATE) {
            server.payload.AuctionUpdatePayload update = (server.payload.AuctionUpdatePayload) msg.getPayload();
            if (update.getAuctionID().equalsIgnoreCase(currentAuctionId)) {
                javafx.application.Platform.runLater(() -> {
                    lblCurrentPrice.setText(String.format("%,.0f đ", update.getHighestBid()));

                    // Nếu thời gian thay đổi đáng kể (Anti-sniping), cập nhật lại rawEndTime và thiết lập lại đếm ngược
                    if (update.getEndTime() != null) {
                        this.rawEndTime = update.getEndTime().toString();
                        startCountdown(this.rawEndTime);

                        // [NEW] Hiển thị thông báo gia hạn thời gian nếu số lượt Anti-sniping tăng
                        if (update.getAntiSnipeCount() > this.previousAntiSnipeCount) {
                            String originalStatus = lblStatus.getText();
                            lblStatus.setText("⚠️ GIA HẠN (Lần " + update.getAntiSnipeCount() + "/5)!");
                            lblStatus.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");

                            // Trả lại trạng thái hiển thị bình thường sau 3 giây
                            new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                                updateUIByStatus(originalStatus);
                            })).play();
                        }
                        this.previousAntiSnipeCount = update.getAntiSnipeCount();
                    }

                    // Cập nhật trạng thái tự động thời gian thực (ví dụ từ OPEN -> RUNNING)
                    if (update.getItemName().equals("Status Update")) {
                        updateUIByStatus("RUNNING");
                    }
                });
            }
        } else if (msg.getType() == client.message.MessageType.NOTIFY_AUCTION_WINNER
                || msg.getType() == client.message.MessageType.NOTIFY_NO_AUCTION_WINNER) {
            String targetId = "";
            if (msg.getPayload() instanceof server.payload.AuctionUpdatePayload) {
                targetId = ((server.payload.AuctionUpdatePayload) msg.getPayload()).getAuctionID();
            } else if (msg.getPayload() instanceof String) {
                targetId = (String) msg.getPayload();
            }

            if (targetId.equalsIgnoreCase(currentAuctionId)) {
                javafx.application.Platform.runLater(() -> updateUIByStatus("FINISHED"));
            }
        } else if (msg.getType() == client.message.MessageType.SEND_BID_HISTORY) {
            server.payload.SendBidHistoryPayload payload = (server.payload.SendBidHistoryPayload) msg.getPayload();
            if (payload.getAuctionID().equalsIgnoreCase(currentAuctionId)) {
                javafx.application.Platform.runLater(() -> {
                    updateSpecFieldsUI(payload.getItem());
                    updateBidHistoryUI(payload.getBidHistory());
                });
            }
        }
    }

    // ═════════════════════════════════════════════════════════
    // THIẾT LẬP DỮ LIỆU BAN ĐẦU (API)
    // ═════════════════════════════════════════════════════════
    public void setAuctionData(String auctionId, String itemName, String category, String startPrice, String curPrice,
                               String endTime, String status, String seller, String description) {
        this.currentAuctionId = auctionId;
        this.currentCategory = category;
        this.rawEndTime = endTime;

        lblPageTitle.setText("Chi tiết: " + itemName);
        lblItemName.setText(itemName);
        lblCategory.setText("Danh mục: " + category);
        lblStartPrice.setText(startPrice + " đ");
        lblCurrentPrice.setText(curPrice + " đ");
        lblSeller.setText("Người bán: " + seller);
        lblStatus.setText(status);
        lblDescription.setText(description != null ? description : "Mô tả chi tiết của sản phẩm " + itemName + ".");

        // Gửi yêu cầu lấy lịch sử đặt giá và thông số sản phẩm chi tiết qua Socket mạng
        client.network.ClientSocketManager.getInstance().sendPacket(
                new client.message.PacketMessage(client.message.MessageType.REQUEST_BID_HISTORY, auctionId));

        startCountdown(endTime);

        updateUIByStatus(status);
    }

    // ═════════════════════════════════════════════════════════
    // CẬP NHẬT GIAO DIỆN THEO TRẠNG THÁI PHIÊN (UI RENDER)
    // ═════════════════════════════════════════════════════════
    private void updateUIByStatus(String status) {
        lblStatus.setText(status);
        switch (status) {
            case "RUNNING" -> {
                lblStatus.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 18; -fx-font-weight: bold;");
                btnEnterBidding.setText("Vào phòng đấu giá →");
                btnEnterBidding.setDisable(false);
                btnEnterBidding.setStyle(
                        "-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12;");
            }
            case "OPEN" -> {
                lblStatus.setStyle("-fx-text-fill: #1565C0; -fx-font-size: 18; -fx-font-weight: bold;");
                btnEnterBidding.setText("Chờ phiên bắt đầu →");
                btnEnterBidding.setDisable(false);
                btnEnterBidding.setStyle(
                        "-fx-background-color: #546E7A; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12;");
            }
            case "FINISHED" -> {
                lblStatus.setStyle("-fx-text-fill: #757575; -fx-font-size: 18; -fx-font-weight: bold;");
                btnEnterBidding.setText("Xem kết quả đấu giá →");
                btnEnterBidding.setDisable(false);
                btnEnterBidding.setStyle(
                        "-fx-background-color: #90A4AE; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12;");
                lblCountdown.setText("ĐÃ KẾT THÚC");
                lblCountdown.setStyle("-fx-text-fill: #757575; -fx-font-size: 28; -fx-font-weight: bold;");
                if (countdownTimer != null)
                    countdownTimer.stop();
            }
            case "CANCELED" -> {
                lblStatus.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 18; -fx-font-weight: bold;");
                btnEnterBidding.setText("Phiên đã bị hủy");
                btnEnterBidding.setDisable(true);
                btnEnterBidding.setStyle(
                        "-fx-background-color: #BDBDBD; -fx-text-fill: #F5F5F5; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12;");
                lblCountdown.setText("ĐÃ HỦY");
                lblCountdown.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 28; -fx-font-weight: bold;");
                if (countdownTimer != null)
                    countdownTimer.stop();
            }
        }
    }

    // ═════════════════════════════════════════════════════════
    // HIỂN THỊ THÔNG SỐ ĐA HÌNH SẢN PHẨM (POLYMORPHIC SPECS)
    // ═════════════════════════════════════════════════════════
    private void updateSpecFieldsUI(server.models.item.Item item) {
        vboxSpecFields.getChildren().clear();
        if (item == null)
            return;

        lblDescription.setText(item.getDescription());
        if (item instanceof server.models.item.Electronics e) {
            addSpec("Hãng:", e.getBrand());
            addSpec("Model:", e.getModel());
            addSpec("Bảo hành:", e.getWarranty() + " tháng");
        } else if (item instanceof server.models.item.Art art) {
            addSpec("Họa sĩ:", art.getArtist());
            addSpec("Năm:", String.valueOf(art.getYear()));
            addSpec("Chất liệu:", art.getMedium());
        } else if (item instanceof server.models.item.Vehicle v) {
            addSpec("Động cơ:", v.getEngineType());
            addSpec("Năm SX:", String.valueOf(v.getModelYear()));
            addSpec("Odo:", String.format("%,.0f km", v.getMileage()));
            addSpec("Biển số:", v.getLicensePlate());
        }
    }

    private void addSpec(String key, String value) {
        HBox row = new HBox(12);
        Label lblKey = new Label(key);
        lblKey.setStyle("-fx-text-fill: #757575; -fx-font-size: 13; -fx-min-width: 100;");
        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: #212121; -fx-font-size: 13;");
        row.getChildren().addAll(lblKey, lblVal);
        vboxSpecFields.getChildren().add(row);
    }

    // ═════════════════════════════════════════════════════════
    // LỊCH SỬ ĐẶT GIÁ SẢN PHẨM (BID HISTORY)
    // ═════════════════════════════════════════════════════════
    private void updateBidHistoryUI(List<server.models.auction.BidTransaction> history) {
        if (history == null)
            return;

        javafx.collections.ObservableList<String> entries = javafx.collections.FXCollections.observableArrayList();

        for (int i = history.size() - 1; i >= 0; i--) {
            server.models.auction.BidTransaction tx = history.get(i);
            if (tx.getStatus() == server.models.auction.BidTransaction.BidStatus.REJECTED)
                continue;

            String timeStr = tx.getTimestamp() != null
                    ? tx.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) : "--:--:--";

            String bidderDisplay = "Người thầu #" + tx.getBidderId().substring(0, Math.min(4, tx.getBidderId().length()));
            entries.add(timeStr + "  " + bidderDisplay + "  " + String.format("%,.0f", tx.getBidAmount()) + " đ");
        }
        listBidHistory.setItems(entries);
    }

    // ═════════════════════════════════════════════════════════
    // ĐỒNG HỒ ĐẾM NGƯỢC (COUNTDOWN TIMER)
    // ═════════════════════════════════════════════════════════
    private void startCountdown(String endTimeStr) {
        if (countdownTimer != null)
            countdownTimer.stop();

        long initialSecs = 0;
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            LocalDateTime now = LocalDateTime.now();
            initialSecs = java.time.Duration.between(now, endTime).getSeconds();
        } catch (Exception e) {
            if (endTimeStr != null && endTimeStr.contains(" : ")) {
                initialSecs = parseCountdownToSeconds(endTimeStr);
            } else if (endTimeStr != null && (endTimeStr.equals("KẾT THÚC") || endTimeStr.equals("ĐÃ KẾT THÚC"))) {
                initialSecs = 0;
            } else {
                initialSecs = 30 * 60;
            }
        }

        if (initialSecs <= 0) {
            lblCountdown.setText("ĐÃ KẾT THÚC");
            lblCountdown.setStyle("-fx-text-fill: #757575; -fx-font-size: 28; -fx-font-weight: bold;");
            return;
        }

        final long[] secsLeft = { initialSecs };

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secsLeft[0]--;
            if (secsLeft[0] <= 0) {
                lblCountdown.setText("ĐÃ KẾT THÚC");
                lblCountdown.setStyle("-fx-text-fill: #757575; -fx-font-size: 28; -fx-font-weight: bold;");
                countdownTimer.stop();
                return;
            }
            long h = secsLeft[0] / 3600;
            long m = (secsLeft[0] % 3600) / 60;
            long s = secsLeft[0] % 60;
            lblCountdown.setText(String.format("%02d : %02d : %02d", h, m, s));

            if (secsLeft[0] <= 300) {
                lblCountdown.setStyle("-fx-text-fill: #BF360C; -fx-font-size: 28; -fx-font-weight: bold;");
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    // ═════════════════════════════════════════════════════════
    // SỰ KIỆN NÚT BẤM VÀ ĐIỀU HƯỚNG MÀN HÌNH (FXML HANDLERS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleBack() {
        cleanup(); // Gỡ listener và dừng countdown một lần duy nhất
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/BidderDashboard.fxml"));
            Stage stage = (Stage) lblItemName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnterBidding() {
        if ("OPEN".equalsIgnoreCase(lblStatus.getText())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Phiên đấu giá chưa bắt đầu. Vui lòng quay lại khi đến giờ!");
            alert.showAndWait();
            return;
        }

        if (countdownTimer != null)
            countdownTimer.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/BiddingScreen.fxml"));
            Parent root = loader.load();
            BiddingController ctrl = loader.getController();

            double startPrice = parsePrice(lblStartPrice.getText());
            double curPrice = parsePrice(lblCurrentPrice.getText());
            double balance = SessionManager.getInstance().getBalance();

            // Lấy Controller và truyền lại dữ liệu đầy đủ để phòng đấu giá có dữ liệu hiển thị tức thì
            ctrl.setAuction(currentAuctionId, lblItemName.getText(), currentCategory,
                    lblSeller.getText().replace("Người bán: ", ""), rawEndTime, startPrice, curPrice, balance,
                    parseCountdownToSeconds(lblCountdown.getText()), lblStatus.getText(), lblDescription.getText());

            cleanup();
            Stage stage = (Stage) lblItemName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // PHƯƠNG THỨC TIỆN ÍCH VÀ DỌN DẸP BỘ NHỚ (HELPERS)
    // ═════════════════════════════════════════════════════════
    private double parsePrice(String text) {
        if (text == null)
            return 0;
        try {
            String clean = text.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Double.parseDouble(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseCountdownToSeconds(String text) {
        try {
            String[] parts = text.split(" : ");
            if (parts.length == 3) {
                long h = Long.parseLong(parts[0].trim());
                long m = Long.parseLong(parts[1].trim());
                long s = Long.parseLong(parts[2].trim());
                return h * 3600 + m * 60 + s;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private void cleanup() {
        // Dừng đồng hồ đếm ngược
        if (countdownTimer != null)
            countdownTimer.stop();
        // Gỡ trình lắng nghe tin nhắn Socket — dùng field để remove() tìm đúng instance cũ
        client.network.ClientSocketManager.getInstance().removeMessageListener(socketListener);
    }
}

package client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import client.network.ClientSocketManager;
import client.message.PacketMessage;
import client.message.MessageType;
import server.payload.AuctionListPayload;
import server.payload.AuctionListItem;
import server.payload.ErrorMessagePayload;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

/**
 * Điều khiển bảng điều khiển của người đấu giá (BidderDashboardController).
 * Giao diện tương ứng: BidderDashboard.fxml
 *
 * Quản lý giao diện trang chủ của người đấu giá, tải dữ liệu động thời gian thực từ Server qua Socket.
 */
public class BidderDashboardController implements Initializable {

    // ═════════════════════════════════════════════════════════
    // PHẦN TIÊU ĐỀ (HEADER)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Button profileButton;

    // ═════════════════════════════════════════════════════════
    // BỘ LỌC TÌM KIẾM (FILTER & TABS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Button btnTabAll;
    @FXML
    private Button btnTabElec;
    @FXML
    private Button btnTabArt;
    @FXML
    private Button btnTabVehicles;
    @FXML
    private TextField txtSearch;

    @FXML
    private FlowPane itemGridPane;
    @FXML
    private FlowPane sellersGridPane;
    @FXML
    private FlowPane upcomingGridPane;

    private AuctionListItem selectedAuction; // Lưu trữ đối tượng phiên đấu giá đang được chọn

    private List<AuctionListItem> masterAuctionList = new ArrayList<>();
    private String currentCategoryFilter = "Tat ca";

    // Quản lý đếm ngược thời gian thực cho các thẻ phiên đấu giá
    private final Map<String, Label> cardTimerLabels = new HashMap<>();
    private final Map<String, AuctionListItem> cardAuctionData = new HashMap<>();
    private Timeline dashboardTimeline;

    // Lưu tham chiếu listener để có thể gỡ đăng ký chính xác (tránh rò rỉ do lambda tạo object mới mỗi lần)
    private java.util.function.Consumer<client.message.PacketMessage> socketListener;

    // Thiết lập kiểu dáng (Style CSS) cho các Tab danh mục hoạt động / không hoạt động
    private static final String STYLE_TAB_ACTIVE = "-fx-background-color: #2c3e50; -fx-text-fill: white;"
            + "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 18; -fx-font-size: 13;";
    private static final String STYLE_TAB_INACTIVE = "-fx-background-color: white; -fx-border-color: #dde1e7;"
            + "-fx-border-radius: 20; -fx-background-radius: 20;"
            + "-fx-cursor: hand; -fx-padding: 6 18; -fx-font-size: 13;";

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Áp dụng kiểu dáng mặc định cho các Tab danh mục (Mặc định chọn Tất cả)
        btnTabAll.setStyle(STYLE_TAB_ACTIVE);
        btnTabElec.setStyle(STYLE_TAB_INACTIVE);
        btnTabArt.setStyle(STYLE_TAB_INACTIVE);
        btnTabVehicles.setStyle(STYLE_TAB_INACTIVE);

        // Đăng ký nhận tin từ Server qua cơ chế Listener
        // Lưu vào field để có thể gỡ chính xác khi chuyển màn hình
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);

        // Gửi yêu cầu lấy danh sách phiên đấu giá và khởi động đếm ngược
        requestAuctionList();
        startDashboardTimer();
    }

    private void startDashboardTimer() {
        if (dashboardTimeline != null)
            dashboardTimeline.stop();
        dashboardTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            updateAllCardTimers();
        }));
        dashboardTimeline.setCycleCount(Timeline.INDEFINITE);
        dashboardTimeline.play();
    }

    private void updateAllCardTimers() {
        for (String id : cardTimerLabels.keySet()) {
            Label lbl = cardTimerLabels.get(id);
            AuctionListItem item = cardAuctionData.get(id);
            if (lbl != null && item != null) {
                updateSingleCardTimer(lbl, item);
            }
        }
    }

    private void updateSingleCardTimer(Label lbl, AuctionListItem item) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if ("OPEN".equalsIgnoreCase(item.getStatus())) {
                LocalDateTime startTime = LocalDateTime.parse(item.getStartTime());
                long diff = java.time.Duration.between(now, startTime).getSeconds();
                if (diff <= 0) {
                    lbl.setText("Đang diễn ra!");
                    lbl.setTextFill(Color.web("#27ae60"));
                } else {
                    long days = diff / 86400;
                    long hours = (diff % 86400) / 3600;
                    long minutes = (diff % 3600) / 60;
                    if (days > 0) {
                        lbl.setText(String.format("Bắt đầu sau: %dd %dh", days, hours));
                    } else {
                        lbl.setText(String.format("Bắt đầu sau: %dh %dm", hours, minutes));
                    }
                    lbl.setTextFill(Color.web("#e67e22"));
                }
            } else {
                LocalDateTime endTime = LocalDateTime.parse(item.getEndTime());
                long diff = java.time.Duration.between(now, endTime).getSeconds();

                if (diff <= 0) {
                    lbl.setText("Trạng thái: FINISHED");
                    lbl.setTextFill(Color.web("#757575"));
                } else {
                    long h = diff / 3600;
                    long m = (diff % 3600) / 60;
                    long s = diff % 60;
                    lbl.setText(String.format("%02d:%02d:%02d", h, m, s));
                    lbl.setTextFill(Color.web("#e67e22"));
                }
            }
        } catch (Exception e) {
            lbl.setText(item.getStatus());
        }
    }

    private void requestAuctionList() {
        System.out.println("[Dashboard] Đang yêu cầu Server gửi danh sách phiên đấu giá...");
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_ACTIVE_AUCTION_LIST, null));
    }

    private void handleServerMessage(PacketMessage msg) {
        if (msg.getType() == MessageType.SEND_ACTIVE_AUCTION_LIST) {
            AuctionListPayload payload = (AuctionListPayload) msg.getPayload();
            if (payload != null) {
                Platform.runLater(() -> {
                    this.masterAuctionList = payload.getAuctionList();
                    renderAuctions();
                });
            }
        } else if (msg.getType() == MessageType.NOTIFY_AUCTION_WINNER) {
            server.payload.AuctionUpdatePayload winnerPayload = (server.payload.AuctionUpdatePayload) msg.getPayload();
            String currentUser = SessionManager.getInstance().getUsername();

            if (winnerPayload.getHighestBidderIP().equalsIgnoreCase(currentUser)) {
                String winMsg = "🏆 Chúc mừng! Bạn đã thắng phiên [" + winnerPayload.getItemName()
                        + "] với mức giá " + String.format("%,.0f", winnerPayload.getHighestBid()) + " đ!";
                SessionManager.getInstance().addNotification(winMsg);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Chúc mừng!");
                    alert.setHeaderText("BẠN ĐÃ THẮNG CUỘC!");
                    alert.setContentText(winMsg);
                    alert.showAndWait();
                    requestAuctionList(); // Làm mới danh sách phiên đấu giá
                });
            }
        } else if (msg.getType() == MessageType.AUCTION_CONCLUDED) {
            Platform.runLater(this::requestAuctionList);
        } else if (msg.getType() == MessageType.ERROR) {
            ErrorMessagePayload errorPayload = (ErrorMessagePayload) msg.getPayload();
            if (errorPayload != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Hệ thống thông báo lỗi");
                    alert.setContentText(errorPayload.getErrorMessage());
                    alert.showAndWait();
                });
            }
        }
    }

    private void renderAuctions() {
        if (itemGridPane == null)
            return;
        itemGridPane.getChildren().clear();
        if (upcomingGridPane != null)
            upcomingGridPane.getChildren().clear();
        if (sellersGridPane != null)
            sellersGridPane.getChildren().clear();

        cardTimerLabels.clear();
        cardAuctionData.clear();

        // 1. Render Active Auctions (RUNNING)
        List<AuctionListItem> runningList = masterAuctionList.stream()
                .filter(a -> "RUNNING".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());

        if (!isAllFilter(currentCategoryFilter)) {
            String key = getCategoryKey(currentCategoryFilter);
            runningList = runningList.stream()
                    .filter(a -> a.getCategory() != null && a.getCategory().equalsIgnoreCase(key))
                    .collect(Collectors.toList());
        }

        for (AuctionListItem item : runningList) {
            VBox card = createAuctionCard(item);
            itemGridPane.getChildren().add(card);
        }

        // 2. Render Upcoming Auctions (OPEN)
        if (upcomingGridPane != null) {
            List<AuctionListItem> upcomingList = masterAuctionList.stream()
                    .filter(a -> "OPEN".equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());

            if (!isAllFilter(currentCategoryFilter)) {
                String key = getCategoryKey(currentCategoryFilter);
                upcomingList = upcomingList.stream()
                        .filter(a -> a.getCategory() != null && a.getCategory().equalsIgnoreCase(key))
                        .collect(Collectors.toList());
            }

            for (AuctionListItem item : upcomingList) {
                VBox card = createUpcomingCard(item);
                upcomingGridPane.getChildren().add(card);
            }
        }

        // 3. Render Featured Sellers dynamically
        if (sellersGridPane != null) {
            Map<String, String[]> sellerDetailsMap = new HashMap<>();
            sellerDetailsMap.put("gv_son_uet", new String[]{"AI Lab UET", "5.0", "#d6eaf8"});
            sellerDetailsMap.put("gv_dung_uet", new String[]{"Khoa CNTT - UET", "4.9", "#fdebd0"});
            sellerDetailsMap.put("uet_doan_thanhnien", new String[]{"Đoàn Thanh Niên UET", "5.0", "#d5f5e3"});
            sellerDetailsMap.put("uet_clb_robocon", new String[]{"CLB Robot UET", "4.8", "#ebdef0"});
            sellerDetailsMap.put("uet_vp_khoa_dtvt", new String[]{"VP Khoa ĐTVT", "4.7", "#f9e79f"});

            Map<String, Long> sellerCountMap = masterAuctionList.stream()
                    .filter(a -> !"CANCELED".equalsIgnoreCase(a.getStatus()) && !"FINISHED".equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.groupingBy(AuctionListItem::getAuctionOwnerIP, Collectors.counting()));

            int renderedCount = 0;
            for (Map.Entry<String, String[]> entry : sellerDetailsMap.entrySet()) {
                String username = entry.getKey();
                String[] details = entry.getValue();
                long itemCount = sellerCountMap.getOrDefault(username, 0L);

                VBox sellerCard = createSellerCard(username, details[0], Double.parseDouble(details[1]), details[2], itemCount);
                sellersGridPane.getChildren().add(sellerCard);
                renderedCount++;
                if (renderedCount >= 3) break;
            }
        }
    }

    private VBox createSellerCard(String username, String companyName, double rating, String circleColor, long itemCount) {
        VBox card = new VBox();
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefWidth(180);
        card.setSpacing(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 18 14; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.07),5,0,0,1); -fx-cursor: hand;");

        Circle circle = new Circle(28.0);
        circle.setFill(Color.web(circleColor));

        Label lblName = new Label(companyName);
        lblName.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label lblRating = new Label(String.format("%.1f%% uy tín", rating * 20));
        lblRating.setTextFill(Color.web("#f39c12"));
        lblRating.setFont(Font.font("System", 12));

        Label lblCount = new Label(itemCount + " sản phẩm");
        lblCount.setTextFill(Color.web("#7f8c8d"));
        lblCount.setFont(Font.font("System", 11));

        card.getChildren().addAll(circle, lblName, lblRating, lblCount);

        card.setOnMouseClicked(e -> {
            System.out.println("Seller " + username + " được chọn");
        });

        return card;
    }

    private VBox createUpcomingCard(AuctionListItem item) {
        VBox card = new VBox();
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.08),6,0,0,2); -fx-cursor: hand;");

        StackPane imgArea = new StackPane();
        imgArea.setPrefHeight(110);
        String bgColor = "#eaf2fb";
        String txtColor = "#5dade2";
        if ("ART".equalsIgnoreCase(item.getCategory())) {
            bgColor = "#fef9e7";
            txtColor = "#d4ac0d";
        } else if ("VEHICLE".equalsIgnoreCase(item.getCategory())) {
            bgColor = "#eafaf1";
            txtColor = "#27ae60";
        }
        imgArea.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10 10 0 0;");

        Label lblPlaceholder = new Label("[" + item.getItemName() + "]");
        lblPlaceholder.setTextFill(Color.web(txtColor));
        imgArea.getChildren().add(lblPlaceholder);

        Label badge = new Label("Sắp diễn ra");
        badge.setStyle("-fx-background-color: #e8f8f5; -fx-text-fill: #1e8449; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 10;");
        imgArea.getChildren().add(badge);
        StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_LEFT);
        badge.setTranslateX(8);
        badge.setTranslateY(8);

        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 12 12 14 12;");

        Label lblTitle = new Label(item.getItemName());
        lblTitle.setWrapText(true);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        HBox priceRow = new HBox(6);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lblPriceText = new Label("Giá khởi đầu:");
        lblPriceText.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        Label lblPriceVal = new Label(String.format("%,.0f đ", item.getItemStartingPrice()));
        lblPriceVal.setTextFill(Color.web("#e74c3c"));
        lblPriceVal.setFont(Font.font("System", FontWeight.BOLD, 11));
        priceRow.getChildren().addAll(lblPriceText, lblPriceVal);

        String timeStr = "Chưa rõ";
        try {
            LocalDateTime start = LocalDateTime.parse(item.getStartTime());
            LocalDateTime end = LocalDateTime.parse(item.getEndTime());
            timeStr = String.format("%d/%d/%d  %02d:%02d - %02d:%02d",
                    start.getDayOfMonth(), start.getMonthValue(), start.getYear() % 100,
                    start.getHour(), start.getMinute(),
                    end.getHour(), end.getMinute());
        } catch (Exception e) {
            timeStr = item.getStartTime() + " - " + item.getEndTime();
        }
        Label lblTime = new Label("Thời gian: " + timeStr);
        lblTime.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        Label lblCountdown = new Label();
        lblCountdown.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11; -fx-font-weight: bold;");

        cardTimerLabels.put(item.getAuctionID(), lblCountdown);
        cardAuctionData.put(item.getAuctionID(), item);
        updateSingleCardTimer(lblCountdown, item);

        content.getChildren().addAll(lblTitle, priceRow, lblTime, lblCountdown);
        card.getChildren().addAll(imgArea, content);

        card.setOnMouseClicked(e -> showAuctionDetail(item));
        addHoverEffect(card);

        return card;
    }

    private boolean isAllFilter(String filter) {
        if (filter == null) return true;
        String f = filter.trim().toLowerCase();
        return f.equals("all") || f.equals("tat ca") || f.equals("tất cả") || f.contains("tất") || f.contains("ca") || f.isEmpty();
    }

    private String getCategoryKey(String display) {
        if (display == null)
            return "";
        String d = display.trim().toLowerCase();
        if (d.contains("all") || d.contains("tat ca") || d.contains("tất cả")) {
            return "ALL";
        }
        if (d.contains("elec")) {
            return "ELECTRONICS";
        }
        if (d.contains("art")) {
            return "ART";
        }
        if (d.contains("veh")) {
            return "VEHICLE";
        }
        return display.toUpperCase().trim();
    }

    private VBox createAuctionCard(AuctionListItem item) {
        VBox card = new VBox();
        card.setPrefWidth(210);
        card.setSpacing(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.08),6,0,0,2); -fx-cursor: hand;");

        StackPane imgArea = new StackPane();
        imgArea.setPrefHeight(130);
        String bgColor = "#eaf2fb";
        String txtColor = "#5dade2";
        if ("ART".equalsIgnoreCase(item.getCategory())) {
            bgColor = "#fef9e7";
            txtColor = "#d4ac0d";
        } else if ("VEHICLE".equalsIgnoreCase(item.getCategory())) {
            bgColor = "#eafaf1";
            txtColor = "#27ae60";
        }

        imgArea.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10 10 0 0;");

        Label lblPlaceholder = new Label("[" + item.getItemName() + "]");
        lblPlaceholder.setTextFill(Color.web(txtColor));
        imgArea.getChildren().add(lblPlaceholder);

        Label badge = new Label(item.getCategory());
        badge.setStyle("-fx-background-color: white; -fx-text-fill: " + txtColor + "; "
                + "-fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 10;");
        imgArea.getChildren().add(badge);
        StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_LEFT);
        badge.setTranslateX(8);
        badge.setTranslateY(8);

        VBox content = new VBox(4);
        content.setStyle("-fx-padding: 10 10 12 10;");

        Label lblTitle = new Label(item.getItemName());
        lblTitle.setWrapText(true);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        double displayPrice = item.getHighestBid() > 0 ? item.getHighestBid() : item.getItemStartingPrice();
        Label lblPrice = new Label(String.format("%,.0f đ", displayPrice));
        lblPrice.setTextFill(Color.web("#e74c3c"));
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

        if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
            cardTimerLabels.put(item.getAuctionID(), lblStatus);
            cardAuctionData.put(item.getAuctionID(), item);
            updateSingleCardTimer(lblStatus, item);
        } else {
            lblStatus.setText("Trạng thái: " + item.getStatus());
            lblStatus.setTextFill(Color.web("#757575"));
        }

        content.getChildren().addAll(lblTitle, lblPrice, lblStatus);
        card.getChildren().addAll(imgArea, content);

        card.setOnMouseClicked(e -> showAuctionDetail(item));
        addHoverEffect(card);

        return card;
    }

    private void addHoverEffect(VBox card) {
        String base = card.getStyle();
        card.setOnMouseEntered(
                e -> card.setStyle(base + "-fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle(base));
    }

    private void showAuctionDetail(AuctionListItem item) {
        // [FIX MEMORY LEAK] Stop bộ đếm thời gian và hủy đăng ký listener trước khi chuyển màn hình
        if (dashboardTimeline != null)
            dashboardTimeline.stop();
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/ItemDetail.fxml"));
            Parent root = loader.load();

            ItemDetailController ctrl = loader.getController();
            double displayPrice = item.getHighestBid() > 0 ? item.getHighestBid() : item.getItemStartingPrice();

            ctrl.setAuctionData(item.getAuctionID(), item.getItemName(), item.getCategory(),
                    String.format("%,.0f", item.getItemStartingPrice()), String.format("%,.0f", displayPrice),
                    item.getEndTime(), item.getStatus(), item.getAuctionOwnerIP(), item.getItemDescription());

            Stage stage = (Stage) itemGridPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategory(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        btnTabAll.setStyle(STYLE_TAB_INACTIVE);
        btnTabElec.setStyle(STYLE_TAB_INACTIVE);
        btnTabArt.setStyle(STYLE_TAB_INACTIVE);
        btnTabVehicles.setStyle(STYLE_TAB_INACTIVE);

        src.setStyle(STYLE_TAB_ACTIVE);
        currentCategoryFilter = src.getText();

        renderAuctions();
    }

    @FXML
    private void handleProfileClick() {
        try {
            URL fxmlLocation = getClass().getResource("/client/views/UserProfilePopup.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            UserProfilePopupController ctrl = loader.getController();
            Stage mainStage = (Stage) btnTabAll.getScene().getWindow();
            ctrl.setOwnerStage(mainStage);

            server.models.user.User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Sẽ được cập nhật lại tự động khi nhận dữ liệu ví mới nhất từ Server
                ctrl.setUserInfo(currentUser.getUsername(), currentUser.getEmail(), 0,
                        (long) SessionManager.getInstance().getBalance());
            }

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setScene(new Scene(root));
            popup.initOwner(mainStage);

            double buttonX = profileButton.localToScreen(0, 0).getX();
            double buttonY = profileButton.localToScreen(0, 0).getY();
            popup.setX(buttonX + profileButton.getWidth() - 460);
            popup.setY(buttonY + profileButton.getHeight() + 10);
            popup.setWidth(460);
            popup.setHeight(540);
            popup.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Stop bộ đếm thời gian và hủy đăng ký listener trước khi Logout
        if (dashboardTimeline != null)
            dashboardTimeline.stop();
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        ClientSocketManager.getInstance().sendPacket(new PacketMessage(client.message.MessageType.DISCONNECT, null));
        ClientSocketManager.getInstance().disconnect();
        SessionManager.getInstance().clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnTabAll.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("Auction - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        renderAuctions();
    }

    @FXML
    private void handleSupport() {
        System.out.println("Hỗ trợ được nhấn");
    }

    @FXML
    private void handleCart() {
        System.out.println("Giỏ hàng được nhấn");
    }

    @FXML
    private void handleNotify() {
        java.util.List<String> list = SessionManager.getInstance().getNotifications();
        if (list.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Bạn không có thông báo mới nào.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hộp thư thông báo");
        alert.setHeaderText("Lịch sử thông báo (" + list.size() + ")");

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        listView.getItems().addAll(list);
        listView.setPrefWidth(450);
        listView.setPrefHeight(250);

        alert.getDialogPane().setContent(listView);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        renderAuctions();
    }

    @FXML
    private void handleViewAll() {
        // Stop bộ đếm thời gian và hủy đăng ký listener trước khi chuyển cảnh
        if (dashboardTimeline != null)
            dashboardTimeline.stop();
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/AuctionList.fxml"));
            Stage stage = (Stage) btnTabAll.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        System.out.println("Bộ lọc được nhấn");
    }

    @FXML
    private void handleViewAllSellers() {
        System.out.println("Xem tất cả người bán được nhấn");
    }

    @FXML
    private void handleSeller1Click() {
        System.out.println("Người bán 1 được nhấn");
    }

    @FXML
    private void handleSeller2Click() {
        System.out.println("Người bán 2 được nhấn");
    }

    @FXML
    private void handleSeller3Click() {
        System.out.println("Người bán 3 được nhấn");
    }
}

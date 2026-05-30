package client.controllers;

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import server.payload.AuctionListItem;
import server.payload.AuctionListPayload;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * UserProfilePopupController
 * FXML: UserProfilePopup.fxml
 *
 * Quản lý thông tin tài khoản cá nhân, hạng thành viên, lịch sử đơn hàng và tài chính của người dùng.
 * - Giải phóng bộ nhớ tối ưu, chống rò rỉ Socket Listener khi đóng Popup.
 * - Hỗ trợ nạp tiền ảo và đồng bộ hóa thời gian thực lên máy chủ.
 */
public class UserProfilePopupController implements Initializable {

    // ════════════════════════════════════════════════════════
    // FXML — SIDEBAR HỒ SƠ
    // ════════════════════════════════════════════════════════
    @FXML
    private Circle avatarCircle;
    @FXML
    private Label lblAvatarInitial;
    @FXML
    private Label lblSidebarName;
    @FXML
    private Label lblSidebarRank;

    // ════════════════════════════════════════════════════════
    // FXML — NÚT ĐIỀU HƯỚNG SIDEBAR
    // ════════════════════════════════════════════════════════
    @FXML
    private Button btnNavInfo;
    @FXML
    private Button btnNavOrders;
    @FXML
    private Button btnNavFinance;
    @FXML
    private Button btnNavSettings;

    // ════════════════════════════════════════════════════════
    // FXML — CÁC PHÂN KHU GIAO DIỆN (PANES)
    // ════════════════════════════════════════════════════════
    @FXML
    private VBox paneInfo;
    @FXML
    private VBox paneOrders;
    @FXML
    private VBox paneFinance;
    @FXML
    private VBox paneSettings;

    // ════════════════════════════════════════════════════════
    // FXML — THÔNG TIN CHI TIẾT TÀI KHOẢN (PANE INFO)
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblRankIcon;
    @FXML
    private Label lblRankTitle;
    @FXML
    private Label lblRankDesc;
    @FXML
    private Label lblName;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblOrdersCount;
    @FXML
    private Label lblProgress;
    @FXML
    private ProgressBar progressRank;

    // ════════════════════════════════════════════════════════
    // FXML — LỊCH SỬ ĐƠN HÀNG (PANE ORDERS)
    // ════════════════════════════════════════════════════════
    @FXML
    private Button btnOrdAll;
    @FXML
    private Button btnOrdWon;
    @FXML
    private Button btnOrdLost;
    @FXML
    private ListView<String> listOrders;
    @FXML
    private Label lblOrdersEmpty;

    // ════════════════════════════════════════════════════════
    // FXML — TÀI CHÍNH & GIAO DỊCH (PANE FINANCE)
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblBalance;
    @FXML
    private Label lblFrozenBalance;
    @FXML
    private ListView<String> listTransactions;

    // ════════════════════════════════════════════════════════
    // FXML — THIẾT LẬP BẢO MẬT & THÔNG BÁO (PANE SETTINGS)
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblPassError;
    @FXML
    private CheckBox chkNotifyBid;
    @FXML
    private CheckBox chkNotifyWin;
    @FXML
    private CheckBox chkNotifyEnd;

    // ════════════════════════════════════════════════════════
    // DỮ LIỆU & TRẠNG THÁI NỘI BỘ (STATE & PROPERTIES)
    // ════════════════════════════════════════════════════════
    private String currentName;
    private String currentEmail;
    private int totalOrders;
    private long balance;

    private Stage ownerStage;
    private final ObservableList<String> transactionData = FXCollections.observableArrayList();
    private Button activeNavBtn;
    private List<AuctionListItem> allMyAuctions = new java.util.ArrayList<>();

    // Bộ lắng nghe sự kiện mạng (Được quản lý để giải phóng bộ nhớ triệt để)
    private java.util.function.Consumer<PacketMessage> socketListener;

    // Định dạng giao diện CSS cho các nút Sidebar
    private static final String NAV_ACTIVE = "-fx-background-color: #E8F4FD; -fx-text-fill: #1A5276;"
            + "-fx-background-radius: 8; -fx-cursor: hand;"
            + "-fx-padding: 9 8; -fx-font-size: 11; -fx-font-weight: bold;" + "-fx-alignment: CENTER_LEFT;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #5D6D7E;"
            + "-fx-background-radius: 8; -fx-cursor: hand;" + "-fx-padding: 9 8; -fx-font-size: 11;"
            + "-fx-alignment: CENTER_LEFT;";

    // Định dạng tiền tệ VND chuẩn Việt Nam
    private static final NumberFormat VND = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    // ═════════════════════════════════════════════════════════
    // KHỞI TẠO HỆ THỐNG (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hiển thị Pane thông tin cá nhân mặc định
        showPane(paneInfo, btnNavInfo);

        // Khởi tạo và đăng ký Bộ lắng nghe mạng an toàn chống leak
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);

        // Yêu cầu danh sách phiên đấu giá cá nhân của tôi từ Server
        requestMyAuctions();

        // Tải danh sách giao dịch
        loadTransactionsMock();
    }

    private void requestMyAuctions() {
        System.out.println("[Profile] Đang gửi yêu cầu lấy danh sách đấu giá cá nhân...");
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_MY_AUCTIONS, null));
    }

    private void handleServerMessage(PacketMessage msg) {
        if (msg.getType() == MessageType.SEND_MY_AUCTIONS) {
            AuctionListPayload payload = (AuctionListPayload) msg.getPayload();
            if (payload != null) {
                this.allMyAuctions = payload.getAuctionList();
                Platform.runLater(() -> renderOrders(this.allMyAuctions));
            }
        }
    }

    private void renderOrders(List<AuctionListItem> auctions) {
        ObservableList<String> items = FXCollections.observableArrayList();
        int wonCount = 0;
        String myUser = SessionManager.getInstance().getUsername();

        for (AuctionListItem a : auctions) {
            String status = a.getStatus();
            String statusIcon = "🏃";
            String resultText = "Đang diễn ra";

            boolean isFinished = "FINISHED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status);
            boolean isWinner = a.getHighestBidderId() != null && a.getHighestBidderId().equalsIgnoreCase(myUser);

            if (isFinished) {
                if (isWinner) {
                    statusIcon = "✅";
                    resultText = "Thắng";
                    wonCount++;
                } else {
                    statusIcon = "❌";
                    resultText = "Thua";
                }
            } else if ("CANCELED".equalsIgnoreCase(status)) {
                statusIcon = "🚫";
                resultText = "Đã hủy";
            }

            items.add(String.format("%s %s — %s ₫ (%s)", statusIcon, a.getItemName(), VND.format(a.getHighestBid()),
                    resultText));
        }
        listOrders.setItems(items);
        lblOrdersEmpty.setVisible(items.isEmpty());
        lblOrdersEmpty.setManaged(items.isEmpty());

        // Cập nhật lại tổng số đơn thắng thực tế và số dư đồng bộ từ Session
        this.totalOrders = wonCount;
        this.balance = (long) SessionManager.getInstance().getBalance();
        applyUserInfoToUI();
    }

    // ═════════════════════════════════════════════════════════
    // PUBLIC API — TRUYỀN DỮ LIỆU TỪ DASHBOARD CHÍNH
    // ═════════════════════════════════════════════════════════
    public void setUserInfo(String name, String email, int ordersCount, long balance) {
        this.currentName = name != null ? name : "";
        this.currentEmail = email != null ? email : "";
        this.totalOrders = ordersCount;
        this.balance = balance;

        applyUserInfoToUI();
    }

    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    // ═════════════════════════════════════════════════════════
    // ÁNH XẠ DỮ LIỆU LÊN GIAO DIỆN (UI BINDING)
    // ═════════════════════════════════════════════════════════
    private void applyUserInfoToUI() {
        // ── Sidebar ──
        String initial = currentName.isEmpty() ? "?" : String.valueOf(currentName.charAt(0)).toUpperCase();
        lblAvatarInitial.setText(initial);
        lblSidebarName.setText(currentName);

        // ── Thông tin chi tiết ──
        lblName.setText(currentName.isEmpty() ? "—" : currentName);
        lblEmail.setText(currentEmail.isEmpty() ? "—" : currentEmail);

        // ── Hạng thành viên ──
        lblOrdersCount.setText(totalOrders + " đơn thắng");
        applyRank(totalOrders);

        // ── Số dư tài chính ──
        lblBalance.setText(VND.format(balance) + " ₫");
        lblFrozenBalance.setText("Đang đặt cọc: 0 ₫");
    }

    private void applyRank(int orders) {
        String icon, title, desc, sidebarRank;
        int current, next;

        if (orders < 10) {
            icon = "🥉";
            title = "Hạng Đồng";
            current = orders;
            next = 10;
            desc = "Cần thêm " + (next - orders) + " đơn thắng để lên Hạng Bạc";
            sidebarRank = "Hạng Đồng";
        } else if (orders < 30) {
            icon = "🥈";
            title = "Hạng Bạc";
            current = orders - 10;
            next = 20;
            desc = "Cần thêm " + (30 - orders) + " đơn thắng để lên Hạng Vàng";
            sidebarRank = "Hạng Bạc";
        } else if (orders < 60) {
            icon = "🥇";
            title = "Hạng Vàng";
            current = orders - 30;
            next = 30;
            desc = "Cần thêm " + (60 - orders) + " đơn thắng để lên Kim Cương";
            sidebarRank = "Hạng Vàng";
        } else {
            icon = "💎";
            title = "Kim Cương";
            current = 1;
            next = 1;
            desc = "Bạn đã đạt hạng cao nhất!";
            sidebarRank = "Kim Cương";
        }

        lblRankIcon.setText(icon);
        lblRankTitle.setText(title);
        lblRankDesc.setText(desc);
        lblSidebarRank.setText(sidebarRank);
        lblProgress.setText(current + " / " + next);
        progressRank.setProgress(next == 0 ? 1.0 : (double) current / next);
    }

    // ═════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION HANDLERS
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleNav(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        if (src == btnNavInfo) {
            showPane(paneInfo, btnNavInfo);
        } else if (src == btnNavOrders) {
            showPane(paneOrders, btnNavOrders);
        } else if (src == btnNavFinance) {
            showPane(paneFinance, btnNavFinance);
        } else if (src == btnNavSettings) {
            showPane(paneSettings, btnNavSettings);
        }
    }

    private void showPane(VBox target, Button navBtn) {
        for (VBox pane : List.of(paneInfo, paneOrders, paneFinance, paneSettings)) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        target.setVisible(true);
        target.setManaged(true);

        if (activeNavBtn != null) {
            activeNavBtn.setStyle(NAV_INACTIVE);
        }
        navBtn.setStyle(NAV_ACTIVE);
        activeNavBtn = navBtn;
    }

    // ═════════════════════════════════════════════════════════
    // GIAO DIỆN HỦY/ĐÓNG POPUP (CLEAN UP LIFE CYCLE)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleClose() {
        // Hủy đăng ký listener ngay lập tức trước khi đóng cửa sổ để tránh leak bộ nhớ
        if (socketListener != null) {
            ClientSocketManager.getInstance().removeMessageListener(socketListener);
        }
        Stage popup = (Stage) btnNavInfo.getScene().getWindow();
        popup.close();
    }

    // ═════════════════════════════════════════════════════════
    // THAO TÁC ĐĂNG XUẤT (LOGOUT)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleLogout() {
        Stage popup = (Stage) btnNavInfo.getScene().getWindow();
        popup.close();

        // Gỡ bỏ Listener mạng ngầm bảo toàn bộ nhớ
        if (socketListener != null) {
            ClientSocketManager.getInstance().removeMessageListener(socketListener);
        }

        if (ownerStage == null) {
            return;
        }

        try {
            // Ngắt kết nối socket mạng và xóa Session sạch sẽ
            String username = SessionManager.getInstance().getUsername();
            ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.DISCONNECT, username));
            ClientSocketManager.getInstance().disconnect();
            SessionManager.getInstance().clear();

            // Quay về màn hình Login với kích thước theo cấu hình FXML của Login.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            ownerStage.setScene(new Scene(root));
            ownerStage.sizeToScene();
            ownerStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // BỘ LỌC ĐƠN HÀNG ĐẤU GIÁ (ORDER TABS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleOrderFilter(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        styleOrderTab(src);

        if (allMyAuctions == null || allMyAuctions.isEmpty()) {
            return;
        }

        List<AuctionListItem> filtered;
        String myUser = SessionManager.getInstance().getUsername();

        if (src == btnOrdWon) {
            filtered = allMyAuctions.stream()
                    .filter(a -> ("FINISHED".equalsIgnoreCase(a.getStatus()) || "PAID".equalsIgnoreCase(a.getStatus()))
                            && myUser.equalsIgnoreCase(a.getHighestBidderId()))
                    .toList();
        } else if (src == btnOrdLost) {
            filtered = allMyAuctions.stream()
                    .filter(a -> ("FINISHED".equalsIgnoreCase(a.getStatus()) || "PAID".equalsIgnoreCase(a.getStatus()))
                            && !myUser.equalsIgnoreCase(a.getHighestBidderId()))
                    .toList();
        } else {
            filtered = allMyAuctions;
        }

        renderOrders(filtered);
    }

    private void styleOrderTab(Button active) {
        String ACTIVE_STYLE = "-fx-background-color: #2C3E50; -fx-text-fill: white;"
                + "-fx-background-radius: 20; -fx-cursor: hand;" + "-fx-padding: 4 14; -fx-font-size: 11;";
        String INACTIVE_STYLE = "-fx-background-color: #ECF0F1; -fx-text-fill: #5D6D7E;"
                + "-fx-background-radius: 20; -fx-cursor: hand;" + "-fx-padding: 4 14; -fx-font-size: 11;";
        for (Button btn : List.of(btnOrdAll, btnOrdWon, btnOrdLost)) {
            btn.setStyle(btn == active ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    // ═════════════════════════════════════════════════════════
    // GIAO DỊCH TÀI CHÍNH (FINANCIAL OPERATIONS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleTopUp() {
        TextInputDialog dialog = new TextInputDialog("1000000");
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Hệ thống nạp tiền ảo siêu tốc");
        dialog.setContentText("Nhập số tiền muốn nạp (VNĐ):");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showError("Lỗi giao dịch", "Số tiền nạp phải là số dương lớn hơn 0.");
                    return;
                }

                // 1. Cập nhật số dư vào Session dùng chung
                server.models.user.User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser instanceof server.models.user.Bidder) {
                    server.models.user.Bidder bidder = (server.models.user.Bidder) currentUser;
                    double newBalance = bidder.getBalance() + amount;
                    bidder.setBalance(newBalance);

                    // 2. Cập nhật lại số dư trên cửa sổ Popup hiện tại
                    this.balance = (long) newBalance;

                    // 3. Làm mới UI của Popup
                    applyUserInfoToUI();

                    // 4. Đồng bộ hóa số dư tức thời lên Server qua Socket
                    ClientSocketManager.getInstance()
                            .sendPacket(new PacketMessage(MessageType.BALANCE_UPDATE, currentUser));

                    // 5. Ghi nhận lịch sử giao dịch thành công
                    String dateStr = java.time.LocalDate.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
                    transactionData.add(0, "+ " + VND.format(amount) + " ₫  Nạp tiền (" + dateStr + ")");

                    // Thông báo thành công tới người dùng
                    showInfo("Thành công", "Đã nạp thành công " + VND.format(amount) + " ₫.\nSố dư khả dụng mới: "
                            + VND.format(newBalance) + " ₫");
                }
            } catch (NumberFormatException e) {
                showError("Lỗi định dạng", "Vui lòng nhập số tiền hợp lệ (ví dụ: 1000000).");
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleWithdraw() {
        showInfo("Rút tiền", "Tính năng liên kết ngân hàng rút tiền đang được phát triển.");
    }

    @FXML
    private void handleChangePassword() {
        showInfo("Bảo mật", "Tính năng thay đổi mật khẩu đang được phát triển.");
    }

    // ═════════════════════════════════════════════════════════
    // LỊCH SỬ GIAO DỊCH (MOCK & LIFECYCLE)
    // ═════════════════════════════════════════════════════════
    private void loadTransactionsMock() {
        transactionData.clear();
        listTransactions.setItems(transactionData);
    }

    // ═════════════════════════════════════════════════════════
    // HỘ TRỢ HIỂN THỊ (UI HELPERS)
    // ═════════════════════════════════════════════════════════
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

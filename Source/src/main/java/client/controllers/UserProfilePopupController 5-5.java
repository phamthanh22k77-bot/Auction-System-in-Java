package client.controllers;

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
 * Cach su dung tu BidderDashboardController (hoac SellerDashboardController):
 *
 *   FXMLLoader loader = new FXMLLoader(
 *       getClass().getResource("/client/views/UserProfilePopup.fxml"));
 *   Parent root = loader.load();
 *   UserProfilePopupController ctrl = loader.getController();
 *
 *   // Truyen du lieu thuc te vao:
 *   ctrl.setUserInfo("Nguyen Van A", "a@email.com", 5, 1_500_000L);
 *   ctrl.setOwnerStage(mainStage);   // de logout co the dong popup va quay ve Login
 *
 *   Stage popup = new Stage();
 *   popup.initOwner(mainStage);
 *   popup.initStyle(StageStyle.UNDECORATED);
 *   popup.setScene(new Scene(root));
 *   popup.show();
 */
public class UserProfilePopupController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────────
    @FXML private Circle  avatarCircle;
    @FXML private Label   lblAvatarInitial;
    @FXML private Label   lblSidebarName;
    @FXML private Label   lblSidebarRank;

    // ── Nav buttons ───────────────────────────────────────────
    @FXML private Button btnNavInfo;
    @FXML private Button btnNavOrders;
    @FXML private Button btnNavFinance;
    @FXML private Button btnNavSettings;

    // ── Panes ─────────────────────────────────────────────────
    @FXML private VBox paneInfo;
    @FXML private VBox paneOrders;
    @FXML private VBox paneFinance;
    @FXML private VBox paneSettings;

    // ── Pane Info ─────────────────────────────────────────────
    @FXML private Label       lblRankIcon;
    @FXML private Label       lblRankTitle;
    @FXML private Label       lblRankDesc;
    @FXML private Label       lblName;           // <-- Ho ten hien thi
    @FXML private Label       lblEmail;          // <-- Email hien thi
    @FXML private Label       lblOrdersCount;
    @FXML private Label       lblProgress;
    @FXML private ProgressBar progressRank;

    // ── Pane Orders ───────────────────────────────────────────
    @FXML private Button             btnOrdAll;
    @FXML private Button             btnOrdWon;
    @FXML private Button             btnOrdLost;
    @FXML private ListView<String>   listOrders;
    @FXML private Label              lblOrdersEmpty;

    // ── Pane Finance ──────────────────────────────────────────
    @FXML private Label            lblBalance;
    @FXML private Label            lblFrozenBalance;
    @FXML private ListView<String> listTransactions;

    // ── Pane Settings ─────────────────────────────────────────
    @FXML private PasswordField txtOldPass;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;
    @FXML private Label         lblPassError;
    @FXML private CheckBox      chkNotifyBid;
    @FXML private CheckBox      chkNotifyWin;
    @FXML private CheckBox      chkNotifyEnd;

    // ── State noi bo ──────────────────────────────────────────
    /** Du lieu user duoc truyen vao tu man hinh goi */
    private String  currentName;
    private String  currentEmail;
    private int     totalOrders;
    private long    balance;       // don vi: dong VND

    /** Cua so chinh (BidderDashboard) de logout co the quay ve Login */
    private Stage   ownerStage;

    /** Button nav dang active */
    private Button  activeNavBtn;

    // Style cho nav button
    private static final String NAV_ACTIVE =
        "-fx-background-color: #E8F4FD; -fx-text-fill: #1A5276;" +
        "-fx-background-radius: 8; -fx-cursor: hand;" +
        "-fx-padding: 9 8; -fx-font-size: 11; -fx-font-weight: bold;" +
        "-fx-alignment: CENTER_LEFT;";
    private static final String NAV_INACTIVE =
        "-fx-background-color: transparent; -fx-text-fill: #5D6D7E;" +
        "-fx-background-radius: 8; -fx-cursor: hand;" +
        "-fx-padding: 9 8; -fx-font-size: 11;" +
        "-fx-alignment: CENTER_LEFT;";

    // Format tien VND
    private static final NumberFormat VND =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hien pane Info mac dinh, active nut tuong ung
        showPane(paneInfo, btnNavInfo);

        // Tai du lieu don hang va giao dich mau (thay bang server sau)
        loadOrdersMock();
        loadTransactionsMock();
    }

    // ═════════════════════════════════════════════════════════
    // PUBLIC API — goi tu BidderDashboardController
    // ═════════════════════════════════════════════════════════

    /**
     * Nhan thong tin user tu man hinh goi va dien vao toan bo giao dien.
     *
     * @param name        Ho ten day du cua user
     * @param email       Dia chi email
     * @param ordersCount Tong so don dau gia thanh cong
     * @param balance     So du hien tai (dong VND)
     */
    public void setUserInfo(String name, String email, int ordersCount, long balance) {
        this.currentName  = name  != null ? name  : "";
        this.currentEmail = email != null ? email : "";
        this.totalOrders  = ordersCount;
        this.balance      = balance;

        applyUserInfoToUI();
    }

    /**
     * Truyen stage chinh de handleLogout co the dong popup va quay ve Login.
     * Neu khong set, logout chi dong popup.
     */
    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    // ═════════════════════════════════════════════════════════
    // AP DUNG DU LIEU LEN GIAO DIEN
    // ═════════════════════════════════════════════════════════

    /**
     * Dien tat ca Label/ProgressBar theo currentName, currentEmail, totalOrders, balance.
     * Duoc goi lai moi khi setUserInfo() duoc cap nhat.
     */
    private void applyUserInfoToUI() {

        // ── Sidebar ──────────────────────────────────────────
        // Initial (chu cai dau cua ten)
        String initial = currentName.isEmpty() ? "?" :
                         String.valueOf(currentName.charAt(0)).toUpperCase();
        lblAvatarInitial.setText(initial);
        lblSidebarName  .setText(currentName);

        // ── Pane Info: Ho ten & Email ─────────────────────────
        // Day la 2 truong chinh duoc ket noi data theo yeu cau
        lblName .setText(currentName.isEmpty()  ? "—" : currentName);
        lblEmail.setText(currentEmail.isEmpty() ? "—" : currentEmail);

        // ── Pane Info: So don & hang thanh vien ───────────────
        lblOrdersCount.setText(totalOrders + " đơn");
        applyRank(totalOrders);

        // ── Pane Finance: So du ───────────────────────────────
        lblBalance.setText(VND.format(balance) + " ₫");
        // frozenBalance hien tai hardcode 0; thay bang server sau
        lblFrozenBalance.setText("Đang đặt cọc: 0 ₫");
    }

    /**
     * Tinh hang thanh vien dua tren so don thanh cong:
     *   0–9   : Dong   (can 10 don de len hang)
     *   10–29 : Bac    (can 30 don de len hang)
     *   30–59 : Vang   (can 60 don de len hang)
     *   60+   : Kim Cuong
     *
     * TODO: thay nguong bang gia tri lay tu server/config.
     */
    private void applyRank(int orders) {
        String icon, title, desc, sidebarRank;
        int    current, next;

        if (orders < 10) {
            icon = "🥉";  title = "Hạng Đồng";
            current = orders; next = 10;
            desc = "Cần thêm " + (next - orders) + " đơn để lên Hạng Bạc";
            sidebarRank = "Hạng Đồng";
        } else if (orders < 30) {
            icon = "🥈";  title = "Hạng Bạc";
            current = orders - 10; next = 20;   // 20 don trong hang Bac
            desc = "Cần thêm " + (30 - orders) + " đơn để lên Hạng Vàng";
            sidebarRank = "Hạng Bạc";
        } else if (orders < 60) {
            icon = "🥇";  title = "Hạng Vàng";
            current = orders - 30; next = 30;
            desc = "Cần thêm " + (60 - orders) + " đơn để lên Kim Cương";
            sidebarRank = "Hạng Vàng";
        } else {
            icon = "💎";  title = "Kim Cương";
            current = 1; next = 1;
            desc = "Bạn đã đạt hạng cao nhất!";
            sidebarRank = "Kim Cương";
        }

        lblRankIcon    .setText(icon);
        lblRankTitle   .setText(title);
        lblRankDesc    .setText(desc);
        lblSidebarRank .setText(sidebarRank);
        lblProgress    .setText(current + " / " + next);
        progressRank   .setProgress(next == 0 ? 1.0 : (double) current / next);
    }

    // ═════════════════════════════════════════════════════════
    // NAVIGATION
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleNav(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        if      (src == btnNavInfo)     showPane(paneInfo,     btnNavInfo);
        else if (src == btnNavOrders)   showPane(paneOrders,   btnNavOrders);
        else if (src == btnNavFinance)  showPane(paneFinance,  btnNavFinance);
        else if (src == btnNavSettings) showPane(paneSettings, btnNavSettings);
    }

    private void showPane(VBox target, Button navBtn) {
        // An tat ca pane
        for (VBox pane : List.of(paneInfo, paneOrders, paneFinance, paneSettings)) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        // Hien pane duoc chon
        target.setVisible(true);
        target.setManaged(true);

        // Cap nhat style nav button
        if (activeNavBtn != null) activeNavBtn.setStyle(NAV_INACTIVE);
        navBtn.setStyle(NAV_ACTIVE);
        activeNavBtn = navBtn;
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER: DONG POPUP
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleClose() {
        Stage popup = (Stage) btnNavInfo.getScene().getWindow();
        popup.close();
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER: DANG XUAT
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleLogout() {
        // Dong popup truoc
        Stage popup = (Stage) btnNavInfo.getScene().getWindow();
        popup.close();

        if (ownerStage == null) return;

        // Quay ve man hinh Login
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/Login.fxml"));
            ownerStage.setScene(new Scene(root));
            ownerStage.setWidth(480);
            ownerStage.setHeight(520);
            ownerStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER: DON HANG — loc tab
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleOrderFilter(javafx.event.ActionEvent event) {
        // TODO: thay bang loc server thuc (gui userId + filter len server)
        Button src = (Button) event.getSource();
        styleOrderTab(src);

        ObservableList<String> items;
        if (src == btnOrdWon) {
            items = FXCollections.observableArrayList(
                "✅ iPhone 15 Pro — 17,500,000 ₫ (15/04)",
                "✅ Tranh Sơn Dầu — 8,500,000 ₫ (12/04)"
            );
        } else if (src == btnOrdLost) {
            items = FXCollections.observableArrayList(
                "❌ Samsung TV 65\" — Thua (14/04)"
            );
        } else {
            // Tab "Tat ca"
            items = FXCollections.observableArrayList(
                "✅ iPhone 15 Pro — 17,500,000 ₫ (15/04)",
                "✅ Tranh Sơn Dầu — 8,500,000 ₫ (12/04)",
                "❌ Samsung TV 65\" — Thua (14/04)"
            );
        }

        listOrders.setItems(items);
        boolean isEmpty = items.isEmpty();
        lblOrdersEmpty.setVisible(isEmpty);
        lblOrdersEmpty.setManaged(isEmpty);
    }

    /** Cap nhat style 3 nut tab loc don hang */
    private void styleOrderTab(Button active) {
        String ACTIVE_STYLE =
            "-fx-background-color: #2C3E50; -fx-text-fill: white;" +
            "-fx-background-radius: 20; -fx-cursor: hand;" +
            "-fx-padding: 4 14; -fx-font-size: 11;";
        String INACTIVE_STYLE =
            "-fx-background-color: #ECF0F1; -fx-text-fill: #5D6D7E;" +
            "-fx-background-radius: 20; -fx-cursor: hand;" +
            "-fx-padding: 4 14; -fx-font-size: 11;";
        for (Button btn : List.of(btnOrdAll, btnOrdWon, btnOrdLost)) {
            btn.setStyle(btn == active ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER: TAI CHINH
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleTopUp() {
        // TODO: mo dialog nap tien, goi server.topUp(userId, amount)
        showInfo("Nạp tiền", "Tính năng nạp tiền đang được phát triển.");
    }

    @FXML
    private void handleWithdraw() {
        // TODO: mo dialog rut tien, goi server.withdraw(userId, amount)
        showInfo("Rút tiền", "Tính năng rút tiền đang được phát triển.");
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER: CAI DAT — DOI MAT KHAU
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleChangePassword() {
        lblPassError.setText("");

        String oldPass  = txtOldPass.getText();
        String newPass  = txtNewPass.getText();
        String confirm  = txtConfirmPass.getText();

        // Validate phia client
        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            lblPassError.setText("Vui lòng điền đầy đủ các trường mật khẩu.");
            return;
        }
        if (newPass.length() < 6) {
            lblPassError.setText("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (!newPass.equals(confirm)) {
            lblPassError.setText("Xác nhận mật khẩu không khớp.");
            txtConfirmPass.clear();
            txtConfirmPass.requestFocus();
            return;
        }

        // TODO: goi server.changePassword(userId, oldPass, newPass)
        //   boolean ok = server.changePassword(currentUserId, oldPass, newPass);
        //   if (!ok) { lblPassError.setText("Mật khẩu hiện tại không đúng."); return; }

        // Gia lap thanh cong
        txtOldPass.clear();
        txtNewPass.clear();
        txtConfirmPass.clear();
        showInfo("Thành công", "Mật khẩu đã được cập nhật.");
    }

    // ═════════════════════════════════════════════════════════
    // MOCK DATA — thay bang dao/server thuc
    // ═════════════════════════════════════════════════════════

    /**
     * Dien danh sach don hang mau.
     * TODO: thay bang List<OrderItem> lay tu server.getOrders(userId)
     */
    private void loadOrdersMock() {
        listOrders.setItems(FXCollections.observableArrayList(
            "✅ iPhone 15 Pro — 17,500,000 ₫ (15/04)",
            "✅ Tranh Sơn Dầu — 8,500,000 ₫ (12/04)",
            "❌ Samsung TV 65\" — Thua (14/04)"
        ));
        lblOrdersEmpty.setVisible(false);
        lblOrdersEmpty.setManaged(false);
    }

    /**
     * Dien danh sach giao dich mau.
     * TODO: thay bang List<Transaction> lay tu server.getTransactions(userId)
     */
    private void loadTransactionsMock() {
        listTransactions.setItems(FXCollections.observableArrayList(
            "+ 2,000,000 ₫  Nạp tiền (10/04)",
            "− 500,000 ₫   Đặt cọc phiên AUC001 (11/04)",
            "+ 500,000 ₫   Hoàn cọc AUC002 (13/04)"
        ));
    }

    // ═════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

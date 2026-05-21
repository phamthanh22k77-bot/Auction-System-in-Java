package client.controllers;

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.models.auction.BidTransaction;
import server.models.user.User;
import server.payload.AuctionListItem;
import server.payload.AuctionListPayload;
import server.payload.ErrorMessagePayload;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Quản lý toàn bộ hệ thống Admin Dashboard:
 * - Điều hướng thanh bên: Tổng quan / Người dùng / Phiên đấu giá / Sản phẩm / Giá thầu
 * - Tổng quan: Thẻ thống kê + hoạt động gần đây
 * - Người dùng: Bảng dữ liệu + Tìm kiếm + Lọc theo vai trò + Khóa/Mở khóa tài khoản
 * - Phiên đấu giá: Bảng dữ liệu + Lọc trạng thái + Hủy phiên đấu giá
 * - Sản phẩm: Bảng danh sách sản phẩm
 * - Giá thầu: Bảng lịch sử đặt giá hệ thống
 */
public class AdminDashboardController implements Initializable {

    // ═════════════════════════════════════════════════════════
    // THANH ĐIỀU HƯỚNG BÊN (SIDEBAR)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblAdminName;
    @FXML
    private Label lblServerStatus;
    @FXML
    private Label lblConnections;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnNavOverview;
    @FXML
    private Button btnNavUsers;
    @FXML
    private Button btnNavAuctions;
    @FXML
    private Button btnNavItems;
    @FXML
    private Button btnNavBids;

    // ═════════════════════════════════════════════════════════
    // CÁC KHUNG GIAO DIỆN (PANES)
    // ═════════════════════════════════════════════════════════
    @FXML
    private VBox paneOverview;
    @FXML
    private VBox paneUsers;
    @FXML
    private VBox paneAuctions;
    @FXML
    private VBox paneItems;
    @FXML
    private VBox paneBids;

    // ═════════════════════════════════════════════════════════
    // PHẦN TỔNG QUAN (OVERVIEW)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblStatUsers;
    @FXML
    private Label lblStatAuctions;
    @FXML
    private Label lblStatItems;
    @FXML
    private Label lblStatBids;
    @FXML
    private ListView<String> listRecentActivity;

    // ═════════════════════════════════════════════════════════
    // PHẦN NGƯỜI DÙNG (USERS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private TextField txtSearchUser;
    @FXML
    private ComboBox<String> cmbFilterRole;
    @FXML
    private TableView<UserRow> tblUsers;
    @FXML
    private TableColumn<UserRow, String> colUserId;
    @FXML
    private TableColumn<UserRow, String> colUName;
    @FXML
    private TableColumn<UserRow, String> colRole;
    @FXML
    private TableColumn<UserRow, String> colUserAct;

    // ═════════════════════════════════════════════════════════
    // PHẦN PHIÊN ĐẤU GIÁ (AUCTIONS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private ComboBox<String> cmbFilterStatus;
    @FXML
    private TableView<AucRow> tblAuctions;
    @FXML
    private TableColumn<AucRow, String> colAucId;
    @FXML
    private TableColumn<AucRow, String> colAucItem;
    @FXML
    private TableColumn<AucRow, String> colAucSeller;
    @FXML
    private TableColumn<AucRow, String> colAucCurBid;
    @FXML
    private TableColumn<AucRow, String> colAucEnd;
    @FXML
    private TableColumn<AucRow, String> colAucStatus;
    @FXML
    private TableColumn<AucRow, String> colAucActions;

    // ═════════════════════════════════════════════════════════
    // PHẦN SẢN PHẨM (ITEMS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private TableView<ItemRow> tblItems;
    @FXML
    private TableColumn<ItemRow, String> colItId;
    @FXML
    private TableColumn<ItemRow, String> colItName;
    @FXML
    private TableColumn<ItemRow, String> colItCat;
    @FXML
    private TableColumn<ItemRow, String> colItSeller;
    @FXML
    private TableColumn<ItemRow, String> colItPrice;
    @FXML
    private TableColumn<ItemRow, String> colItActions;

    // ═════════════════════════════════════════════════════════
    // PHẦN GIÁ THẦU (BIDS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private TableView<BidRow> tblBids;
    @FXML
    private TableColumn<BidRow, String> colBidId;
    @FXML
    private TableColumn<BidRow, String> colBidder;
    @FXML
    private TableColumn<BidRow, String> colBidAuction;
    @FXML
    private TableColumn<BidRow, String> colBidAmount;
    @FXML
    private TableColumn<BidRow, String> colBidTime;
    @FXML
    private TableColumn<BidRow, String> colBidValid;

    // ═════════════════════════════════════════════════════════
    // BỘ NỮ LIỆU BẢNG (DATA)
    // ═════════════════════════════════════════════════════════
    private final ObservableList<UserRow> userData = FXCollections.observableArrayList();
    private final ObservableList<AucRow> aucData = FXCollections.observableArrayList();
    private final ObservableList<ItemRow> itemData = FXCollections.observableArrayList();
    private final ObservableList<BidRow> bidData = FXCollections.observableArrayList();

    private FilteredList<UserRow> filteredUsers;
    private FilteredList<AucRow> filteredAucs;

    // Style cho nút điều hướng (Nav Button) hoạt động / không hoạt động
    private static final String NAV_ACTIVE = "-fx-background-color: #2D2D2D; -fx-text-fill: #F5F5F5;"
            + "-fx-alignment: CENTER_LEFT; -fx-background-radius: 6;"
            + "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #BDBDBD;"
            + "-fx-alignment: CENTER_LEFT; -fx-background-radius: 6;"
            + "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";

    private Button currentNavBtn;

    // Lưu tham chiếu listener để có thể gỡ đăng ký chính xác (tránh rò rỉ do lambda tạo object mới mỗi lần)
    private java.util.function.Consumer<PacketMessage> socketListener;

    // ═════════════════════════════════════════════════════════
    // ĐỐI TƯỢNG DỮ LIỆU NỘI BỘ (INNER DTOs)
    // ═════════════════════════════════════════════════════════
    public record UserRow(String id, String username, String role, boolean isLocked) {
    }

    public record AucRow(String id, String item, String seller, String curBid, String endTime, String status) {
    }

    public record ItemRow(String id, String name, String cat, String seller, String price) {
    }

    public record BidRow(String id, String bidder, String auction, String amount, String time, String valid) {
    }

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblAdminName.setText(SessionManager.getInstance().getUsername());
        lblServerStatus.setText("● Server: Trực tuyến");
        lblConnections.setText("Kết nối: Đang hoạt động");

        // [FIX AN TOÀN] Khởi tạo bộ lọc trước khi nhận tin từ Server để chặn 100% lỗi NullPointerException
        filteredUsers = new FilteredList<>(userData, p -> true);
        filteredAucs = new FilteredList<>(aucData, p -> true);

        tblUsers.setItems(filteredUsers);
        tblAuctions.setItems(filteredAucs);
        tblItems.setItems(itemData);
        tblBids.setItems(bidData);

        // Danh sách lựa chọn (ComboBoxes)
        cmbFilterRole.setItems(FXCollections.observableArrayList("Tất cả", "Bidder", "Seller", "Admin"));
        cmbFilterRole.getSelectionModel().selectFirst();

        cmbFilterStatus
                .setItems(FXCollections.observableArrayList("Tất cả", "OPEN", "RUNNING", "FINISHED", "CANCELED"));
        cmbFilterStatus.getSelectionModel().selectFirst();

        bindAllColumns();

        // Đăng ký nhận tin từ Server
        // Lưu vào field để có thể gỡ chính xác khi Logout
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);

        refreshData();
        setupFilters();

        // Hiển thị khung Tổng quan (Overview) mặc định
        showPane(paneOverview, btnNavOverview);
    }

    private void refreshData() {
        System.out.println("[Admin] Đang làm mới dữ liệu từ Server...");
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_ACTIVE_AUCTION_LIST, null));
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_ALL_USERS, null));
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_ALL_BIDS, null));
    }

    private void handleServerMessage(PacketMessage msg) {
        Platform.runLater(() -> {
            if (msg.getType() == MessageType.SEND_ACTIVE_AUCTION_LIST) {
                AuctionListPayload p = (AuctionListPayload) msg.getPayload();
                updateAuctionTable(p.getAuctionList());
            } else if (msg.getType() == MessageType.SEND_ALL_USERS) {
                List<User> users = (List<User>) msg.getPayload();
                updateUserTable(users);
            } else if (msg.getType() == MessageType.SEND_ALL_BIDS) {
                List<BidTransaction> bids = (List<BidTransaction>) msg.getPayload();
                updateBidTable(bids);
            } else if (msg.getType() == MessageType.ERROR) {
                ErrorMessagePayload errorPayload = (ErrorMessagePayload) msg.getPayload();
                if (errorPayload != null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi hệ thống");
                    alert.setHeaderText("Yêu cầu thất bại");
                    alert.setContentText(errorPayload.getErrorMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    private void updateAuctionTable(List<AuctionListItem> list) {
        // [FIX AN TOÀN] Chỉ xóa và nạp lại dữ liệu gốc, bộ lọc hiện tại sẽ giữ nguyên không bị reset
        aucData.clear();
        itemData.clear();
        for (AuctionListItem a : list) {
            aucData.add(new AucRow(a.getAuctionID(), a.getItemName(), a.getAuctionOwnerIP(),
                    String.format("%,.0f", a.getHighestBid()), a.getEndTime(), a.getStatus()));
            itemData.add(new ItemRow(a.getAuctionID(), a.getItemName(), a.getCategory(), a.getAuctionOwnerIP(),
                    String.format("%,.0f", a.getItemStartingPrice())));
        }
        lblStatAuctions.setText(String.valueOf(aucData.size()));
        lblStatItems.setText(String.valueOf(itemData.size()));
    }

    private void updateUserTable(List<User> list) {
        // [FIX AN TOÀN] Chỉ cập nhật danh sách dữ liệu gốc để giữ vững bộ lọc tìm kiếm
        userData.clear();
        for (User u : list) {
            userData.add(new UserRow(u.getId(), u.getUsername(), u.getRole(), u.isLocked()));
        }
        lblStatUsers.setText(String.valueOf(userData.size()));
    }

    private void updateBidTable(List<BidTransaction> list) {
        bidData.clear();
        for (BidTransaction b : list) {
            bidData.add(new BidRow(b.getAuctionId(), b.getBidderId(), b.getAuctionId(),
                    String.format("%,.0f", b.getBidAmount()), "Gần đây", b.getStatus().toString()));
        }
        lblStatBids.setText(String.valueOf(bidData.size()));
    }

    // ═════════════════════════════════════════════════════════
    // RÀNG BUỘC CÁC CỘT BẢNG (BIND COLUMNS)
    // ═════════════════════════════════════════════════════════
    private void bindAllColumns() {
        // Người dùng
        colUserId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colUName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().role()));
        colUserAct.setCellFactory(col -> buildActionCell_Users());

        // Phiên đấu giá
        colAucId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colAucItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().item()));
        colAucSeller.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().seller()));
        colAucCurBid.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curBid()));
        colAucEnd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().endTime()));
        colAucStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colAucActions.setCellFactory(col -> buildActionCell_Auctions());

        // Sản phẩm
        colItId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colItName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colItCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().cat()));
        colItSeller.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().seller()));
        colItPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price()));
        colItActions.setCellFactory(col -> buildActionCell_Items());

        // Giá thầu
        colBidId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colBidder.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().bidder()));
        colBidAuction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().auction()));
        colBidAmount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().amount()));
        colBidTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time()));
        colBidValid.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().valid()));
    }

    // ═════════════════════════════════════════════════════════
    // CÁC Ô HÀNH ĐỘNG BẢNG (ACTION CELLS)
    // ═════════════════════════════════════════════════════════
    private TableCell<UserRow, String> buildActionCell_Users() {
        return new TableCell<>() {
            private final Button btnLock = new Button();
            {
                btnLock.setPrefWidth(90);
                btnLock.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    String action = row.isLocked() ? "Mở khóa" : "Khóa";
                    showConfirm(action + " tài khoản", action + " người dùng: " + row.username() + "?", () -> {
                        ClientSocketManager.getInstance()
                                .sendPacket(new PacketMessage(MessageType.TOGGLE_USER_LOCK, row.id()));
                        logActivity(action + " người dùng: " + row.username());
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserRow row = getTableView().getItems().get(getIndex());
                    if (row.isLocked()) {
                        btnLock.setText("Mở khóa");
                        btnLock.setStyle(
                                "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                    } else {
                        btnLock.setText("Khóa");
                        btnLock.setStyle(
                                "-fx-background-color: #E65100; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                    }
                    setGraphic(btnLock);
                }
            }
        };
    }

    private TableCell<AucRow, String> buildActionCell_Auctions() {
        return new TableCell<>() {
            private final Button btnCancel = new Button("Hủy phiên");
            {
                btnCancel.setStyle("-fx-background-color: #C62828; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnCancel.setOnAction(e -> {
                    AucRow row = getTableView().getItems().get(getIndex());
                    showConfirm("Hủy phiên đấu giá", "Hủy phiên: " + row.item() + "?", () -> {
                        ClientSocketManager.getInstance()
                                .sendPacket(new PacketMessage(MessageType.CANCEL_AUCTION, row.id()));
                        logActivity("Yêu cầu hủy phiên: " + row.item());
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnCancel);
            }
        };
    }

    private TableCell<ItemRow, String> buildActionCell_Items() {
        return new TableCell<>() {
            private final Button btnDel = new Button("Xóa");
            {
                btnDel.setStyle("-fx-background-color: #C62828; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnDel.setOnAction(e -> {
                    ItemRow row = getTableView().getItems().get(getIndex());
                    showConfirm("Xóa sản phẩm", "Xóa: " + row.name() + "?", () -> {
                        itemData.remove(row);
                        logActivity("Xóa sản phẩm: " + row.name());
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDel);
            }
        };
    }

    // ═════════════════════════════════════════════════════════
    // BỘ LỌC TỰ ĐỘNG (FILTERS)
    // ═════════════════════════════════════════════════════════
    private void setupFilters() {
        // Người dùng: Tìm kiếm + Lọc theo vai trò
        Runnable userFilter = () -> {
            String kw = txtSearchUser.getText().trim().toLowerCase();
            String role = cmbFilterRole.getValue();
            filteredUsers.setPredicate(row -> (kw.isEmpty() || row.username().toLowerCase().contains(kw))
                    && (role == null || role.equals("Tất cả") || row.role().equals(role)));
        };
        txtSearchUser.textProperty().addListener((o, old, nw) -> userFilter.run());
        cmbFilterRole.valueProperty().addListener((o, old, nw) -> userFilter.run());

        // Phiên đấu giá: Lọc trạng thái phiên
        cmbFilterStatus.valueProperty().addListener((o, old, nw) -> {
            filteredAucs.setPredicate(row -> nw == null || nw.equals("Tất cả") || row.status().equals(nw));
        });
    }

    // ═════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG GIAO DIỆN (NAVIGATION)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleNav(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        if (src == btnNavOverview)
            showPane(paneOverview, btnNavOverview);
        else if (src == btnNavUsers)
            showPane(paneUsers, btnNavUsers);
        else if (src == btnNavAuctions)
            showPane(paneAuctions, btnNavAuctions);
        else if (src == btnNavItems)
            showPane(paneItems, btnNavItems);
        else if (src == btnNavBids)
            showPane(paneBids, btnNavBids);
    }

    private void showPane(VBox target, Button navBtn) {
        // Ẩn tất cả các khung (Pane) khác
        for (VBox pane : List.of(paneOverview, paneUsers, paneAuctions, paneItems, paneBids)) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        // Hiển thị khung mục tiêu
        target.setVisible(true);
        target.setManaged(true);

        // Cập nhật kiểu dáng của nút điều hướng thanh bên
        if (currentNavBtn != null)
            currentNavBtn.setStyle(NAV_INACTIVE);
        navBtn.setStyle(NAV_ACTIVE);
        currentNavBtn = navBtn;
    }

    // ═════════════════════════════════════════════════════════
    // CÁC SỰ KIỆN XỬ LÝ (HANDLERS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleSearchUser() {
        /* Bộ lọc đã tự động xử lý qua bộ lắng nghe (listener) */
    }

    @FXML
    private void handleFilterRole() {
        /* Bộ lọc đã tự động xử lý qua bộ lắng nghe (listener) */
    }

    @FXML
    private void handleFilterStatus() {
        /* Bộ lọc đã tự động xử lý qua bộ lắng nghe (listener) */
    }

    @FXML
    private void handleRefreshActivity() {
        // Nạp lại toàn bộ dữ liệu sạch thực tế từ Server ngay lập tức
        refreshData();
        logActivity("Admin đã làm mới toàn bộ dữ liệu từ hệ thống");
    }

    private void logActivity(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        listRecentActivity.getItems().add(0, time + "  " + msg);
    }

    private void showConfirm(String title, String content, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK)
                onConfirm.run();
        });
    }

    @FXML
    private void handleLogout() {
        // Hủy đăng ký lắng nghe sự kiện của màn hình cũ khỏi Socket Manager
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        ClientSocketManager.getInstance()
                .sendPacket(new PacketMessage(MessageType.DISCONNECT, null));
        ClientSocketManager.getInstance().disconnect();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

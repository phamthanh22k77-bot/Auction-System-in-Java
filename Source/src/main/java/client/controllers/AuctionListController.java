package client.controllers;

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
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import client.network.ClientSocketManager;
import client.message.PacketMessage;
import client.message.MessageType;
import server.payload.AuctionListPayload;
import server.payload.AuctionListItem;
import javafx.application.Platform;

/**
 * Quản lý danh sách phiên đấu giá (AuctionListController).
 * Giao diện tương ứng: AuctionList.fxml
 *
 * Hiển thị toàn bộ phiên đấu giá, hỗ trợ:
 * - Tìm kiếm theo từ khóa thông minh
 * - Lọc theo danh mục sản phẩm và trạng thái phiên đấu
 * - Nhấp đúp chuột vào dòng để chuyển sang màn hình chi tiết (ItemDetail)
 */
public class AuctionListController implements Initializable {

    // ═════════════════════════════════════════════════════════
    // PHẦN TIÊU ĐỀ (HEADER)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblUsername;
    @FXML
    private Button btnLogout;

    // ═════════════════════════════════════════════════════════
    // THANH BỘ LỌC (FILTER BAR)
    // ═════════════════════════════════════════════════════════
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private ComboBox<String> cmbStatus;

    // ═════════════════════════════════════════════════════════
    // BẢNG DANH SÁCH PHIÊN ĐẤU GIÁ (TABLE)
    // ═════════════════════════════════════════════════════════
    @FXML
    private TableView<AuctionRow> tblAuctions;
    @FXML
    private TableColumn<AuctionRow, String> colAuctionId;
    @FXML
    private TableColumn<AuctionRow, String> colItemName;
    @FXML
    private TableColumn<AuctionRow, String> colCategory;
    @FXML
    private TableColumn<AuctionRow, String> colStartPrice;
    @FXML
    private TableColumn<AuctionRow, String> colCurPrice;
    @FXML
    private TableColumn<AuctionRow, String> colEndTime;
    @FXML
    private TableColumn<AuctionRow, String> colStatus;

    // ═════════════════════════════════════════════════════════
    // THANH TRẠNG THÁI DƯỚI (BOTTOM BAR)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblBalance;
    @FXML
    private Label lblTotalAuctions;
    @FXML
    private Button btnViewDetail;

    // ═════════════════════════════════════════════════════════
    // DỮ LIỆU (DATA)
    // ═════════════════════════════════════════════════════════
    private final ObservableList<AuctionRow> allData = FXCollections.observableArrayList();
    private FilteredList<AuctionRow> filtered;

    // Lưu tham chiếu listener để có thể gỡ đăng ký chính xác (tránh rò rỉ do lambda tạo object mới mỗi lần)
    private java.util.function.Consumer<PacketMessage> socketListener;

    // ═════════════════════════════════════════════════════════
    // ĐỐI TƯỢNG DỮ LIỆU NỘI BỘ (INNER DTO)
    // Đại diện cho một dòng hiển thị trong TableView
    // ═════════════════════════════════════════════════════════
    public static class AuctionRow {
        public final String id, itemName, category, startPrice, curPrice, endTime, status, seller, description;

        public AuctionRow(String id, String itemName, String category, String startPrice, String curPrice,
                          String endTime, String status, String seller, String description) {
            this.id = id;
            this.itemName = itemName;
            this.category = category;
            this.startPrice = startPrice;
            this.curPrice = curPrice;
            this.endTime = endTime;
            this.status = status;
            this.seller = seller;
            this.description = description;
        }
    }

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindColumns();
        setupFilter();
        loadData();
        setupRowDoubleClick();
        setupSelectionListener();

        // Cấu hình các lựa chọn danh mục và trạng thái
        cmbCategory.setItems(FXCollections.observableArrayList("Tất cả", "Electronics", "Art", "Vehicle"));
        cmbCategory.getSelectionModel().selectFirst();

        cmbStatus.setItems(FXCollections.observableArrayList("Tất cả", "RUNNING", "OPEN", "FINISHED"));
        cmbStatus.getSelectionModel().selectFirst();

        lblUsername.setText(SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getUsername() : "Bidder");

        lblBalance.setText("Số dư: " + fmtPrice(SessionManager.getInstance().getBalance()) + " ₫");

        // Đăng ký listener nhận dữ liệu thời gian thực từ Server
        // Lưu vào field để có thể gỡ chính xác khi chuyển màn hình
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);

        // Yêu cầu danh sách phiên đấu giá lần đầu tiên
        handleRefresh();
    }

    private void handleServerMessage(PacketMessage msg) {
        if (msg.getType() == MessageType.SEND_ACTIVE_AUCTION_LIST) {
            AuctionListPayload payload = (AuctionListPayload) msg.getPayload();
            Platform.runLater(() -> {
                allData.clear();
                if (payload.getAuctionList() != null) {
                    for (AuctionListItem item : payload.getAuctionList()) {
                        allData.add(new AuctionRow(item.getAuctionID(), item.getItemName(), item.getCategory(),
                                fmtPrice(item.getItemStartingPrice()), fmtPrice(item.getHighestBid()),
                                item.getEndTime(), item.getStatus(), item.getAuctionOwnerIP(),
                                item.getItemDescription()));
                    }
                }
                lblTotalAuctions.setText("Hiển thị: " + allData.size() + " phiên");
            });
        }
    }

    private String fmtPrice(double price) {
        return String.format("%,.0f", price);
    }

    // Gán các cột của TableView tương ứng vào các thuộc tính của AuctionRow
    private void bindColumns() {
        colAuctionId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colItemName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().itemName));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colStartPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colEndTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().endTime));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // Tự động tô màu cột Trạng thái (Status) trực quan
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                switch (status) {
                    case "RUNNING" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "OPEN" -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    case "FINISHED" -> setStyle("-fx-text-fill: #757575;");
                    default -> setStyle("");
                }
            }
        });
    }

    // Bộ lọc FilteredList: Lọc đồng thời theo cả từ khóa, danh mục và trạng thái
    private void setupFilter() {
        filtered = new FilteredList<>(allData, p -> true);
        tblAuctions.setItems(filtered);

        Runnable applyFilter = () -> {
            String kw = txtSearch.getText().trim().toLowerCase();
            String cat = cmbCategory.getValue();
            String st = cmbStatus.getValue();
            filtered.setPredicate(row -> {
                boolean matchKw = kw.isEmpty() || row.itemName.toLowerCase().contains(kw)
                        || row.id.toLowerCase().contains(kw);
                boolean matchCat = cat == null || cat.equals("Tất cả") || row.category.equalsIgnoreCase(cat);
                boolean matchSt = st == null || st.equals("Tất cả") || row.status.equals(st);
                return matchKw && matchCat && matchSt;
            });
            lblTotalAuctions.setText("Hiển thị: " + filtered.size() + " phiên");
        };

        txtSearch.textProperty().addListener((o, old, nw) -> applyFilter.run());
        cmbCategory.valueProperty().addListener((o, old, nw) -> applyFilter.run());
        cmbStatus.valueProperty().addListener((o, old, nw) -> applyFilter.run());
    }

    private void loadData() {
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.REQUEST_ACTIVE_AUCTION_LIST, null));
    }

    // Nhấp đúp chuột vào dòng của bảng để mở nhanh màn hình chi tiết sản phẩm
    private void setupRowDoubleClick() {
        tblAuctions.setRowFactory(tv -> {
            TableRow<AuctionRow> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    openItemDetail(row.getItem());
                }
            });
            return row;
        });
    }

    // Kích hoạt nút "Xem chi tiết" chỉ khi có dòng đang được chọn trong bảng
    private void setupSelectionListener() {
        btnViewDetail.setDisable(true);
        tblAuctions.getSelectionModel().selectedItemProperty()
                .addListener((o, old, nw) -> btnViewDetail.setDisable(nw == null));
    }

    // ═════════════════════════════════════════════════════════
    // CÁC SỰ KIỆN XỬ LÝ (HANDLERS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleSearch() {
        /* Bộ lọc đã tự động xử lý qua bộ lắng nghe (listener) */
    }

    @FXML
    private void handleFilter() {
        /* Bộ lọc đã tự động xử lý qua bộ lắng nghe (listener) */
    }

    @FXML
    private void handleRefresh() {
        loadData();
        txtSearch.clear();
        cmbCategory.getSelectionModel().selectFirst();
        cmbStatus.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleViewDetail() {
        AuctionRow selected = tblAuctions.getSelectionModel().getSelectedItem();
        if (selected != null)
            openItemDetail(selected);
    }

    // Gọi cả từ sự kiện đúp chuột và nút bấm xem chi tiết
    @FXML
    private void handleRowDoubleClick(javafx.scene.input.MouseEvent e) {
        if (e.getClickCount() == 2)
            handleViewDetail();
    }

    private void openItemDetail(AuctionRow row) {
        // Gỡ bộ lắng nghe sự kiện của màn hình cũ khỏi Socket Manager để tránh xung đột
        ClientSocketManager.getInstance().removeMessageListener(socketListener);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/ItemDetail.fxml"));
            Parent root = loader.load();
            ItemDetailController ctrl = loader.getController();

            // Truyền dữ liệu chi tiết của phiên sang màn hình ItemDetail
            ctrl.setAuctionData(row.id, row.itemName, row.category, row.startPrice, row.curPrice, row.endTime,
                    row.status, row.seller, row.description);
            Stage stage = (Stage) tblAuctions.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        ClientSocketManager.getInstance().removeMessageListener(socketListener);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/BidderDashboard.fxml"));
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        ClientSocketManager.getInstance().removeMessageListener(socketListener);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

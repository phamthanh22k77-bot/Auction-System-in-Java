package client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.network.ClientSocketManager;
import client.message.PacketMessage;
import client.message.MessageType;
import server.payload.CreateAuctionPayload;
import server.payload.AuctionListPayload;
import server.payload.AuctionListItem;
import server.payload.ErrorMessagePayload;
import server.models.item.ItemCategory;
import server.models.user.Seller;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * SellerDashboardController
 * FXML: SellerDashboard.fxml
 *
 * Quản lý toàn bộ giao diện điều khiển của Người Bán (Seller Hub):
 * - Hỗ trợ Sidebar điều hướng động.
 * - Lấy dữ liệu qua Socket Network an toàn thay vì truy xuất DAO cục bộ.
 * - Xử lý tạo sản phẩm chi tiết đa thuộc tính (Electronics, Art, Vehicle).
 * - Dọn dẹp luồng Socket, giải phóng tài nguyên hệ thống khi Đăng xuất.
 */
public class SellerDashboardController implements Initializable {

    // ════════════════════════════════════════════════════════
    // FXML — TOP BAR
    // ════════════════════════════════════════════════════════
    @FXML
    private TextField txtGlobalSearch;
    @FXML
    private Button btnSellerUser;
    @FXML
    private Button btnNotify;

    // ════════════════════════════════════════════════════════
    // FXML — SIDEBAR NAV
    // ════════════════════════════════════════════════════════
    @FXML
    private Button btnNavOverview;
    @FXML
    private Button btnNavOrders;
    @FXML
    private Button btnNavProducts;
    @FXML
    private Button btnNavStats;
    @FXML
    private Button btnNavShop;

    // ════════════════════════════════════════════════════════
    // FXML — 3 PANE CHÍNH
    // ════════════════════════════════════════════════════════
    @FXML
    private VBox paneOverview;
    @FXML
    private VBox paneProducts;
    @FXML
    private VBox paneOrders;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: STAT CARDS
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblStatSales;
    @FXML
    private Label lblStatSalesDelta;
    @FXML
    private Label lblStatOrders;
    @FXML
    private Label lblStatOrdersDelta;
    @FXML
    private Label lblStatRevenue;
    @FXML
    private Label lblStatRevenueDelta;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: ĐƠN HÀNG CẦN XỬ LÝ
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblOrdersPending;
    @FXML
    private Label lblOrdersPickup;
    @FXML
    private Label lblOrdersDone;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: BIỂU ĐỒ DOANH THU
    // ════════════════════════════════════════════════════════
    @FXML
    private LineChart<String, Number> chartRevenue;
    @FXML
    private CategoryAxis axisWeek;
    @FXML
    private NumberAxis axisRevenue;
    @FXML
    private Button btnChartWeek;
    @FXML
    private Button btnChartMonth;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: HỒ SƠ SHOP
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblShopName;
    @FXML
    private Label lblShopCompany;
    @FXML
    private Label lblShopRating;
    @FXML
    private Label lblShopDesc;
    @FXML
    private Label lblShopTotalItems;
    @FXML
    private Label lblShopTotalSold;

    // ════════════════════════════════════════════════════════
    // FXML — PRODUCTS: FORM THÊM/SỬA
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblFormTitle;
    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> cmbFormCategory;
    @FXML
    private ComboBox<String> cmbFilterCategory;

    // Nhóm field động theo loại sản phẩm
    @FXML
    private VBox vboxElectronics;
    @FXML
    private TextField txtBrand;
    @FXML
    private TextField txtWarranty;

    @FXML
    private VBox vboxArt;
    @FXML
    private TextField txtArtist;
    @FXML
    private TextField txtMedium;

    @FXML
    private VBox vboxVehicle;
    @FXML
    private TextField txtEngineType;
    @FXML
    private TextField txtModelYear;

    @FXML
    private TextField txtStartingPrice;
    @FXML
    private DatePicker datePickerStart;
    @FXML
    private TextField txtHourStart;
    @FXML
    private TextField txtMinStart;
    @FXML
    private DatePicker datePickerEnd;
    @FXML
    private TextField txtHourEnd;
    @FXML
    private TextField txtMinEnd;
    @FXML
    private Label lblFormError;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    // ════════════════════════════════════════════════════════
    // FXML — PRODUCTS: TABLE + LABELS
    // ════════════════════════════════════════════════════════
    @FXML
    private TableView<ProductRow> tblProducts;
    @FXML
    private TableColumn<ProductRow, String> colItemId;
    @FXML
    private TableColumn<ProductRow, String> colName;
    @FXML
    private TableColumn<ProductRow, String> colCat;
    @FXML
    private TableColumn<ProductRow, String> colPrice;
    @FXML
    private TableColumn<ProductRow, String> colCurPrice;
    @FXML
    private TableColumn<ProductRow, String> colAucStatus;
    @FXML
    private TableColumn<ProductRow, String> colActions;
    @FXML
    private TextField txtSearchProduct;
    @FXML
    private Label lblTotalProducts;
    @FXML
    private Label lblActiveAuctions;
    @FXML
    private Label lblFinishedAuctions;

    // ════════════════════════════════════════════════════════
    // FXML — ORDERS: TABLE
    // ════════════════════════════════════════════════════════
    @FXML
    private TableView<OrderRow> tblOrders;
    @FXML
    private TableColumn<OrderRow, String> colOrdId;
    @FXML
    private TableColumn<OrderRow, String> colOrdItem;
    @FXML
    private TableColumn<OrderRow, String> colOrdBuyer;
    @FXML
    private TableColumn<OrderRow, String> colOrdPrice;
    @FXML
    private TableColumn<OrderRow, String> colOrdTime;
    @FXML
    private TableColumn<OrderRow, String> colOrdStatus;
    @FXML
    private TableColumn<OrderRow, String> colOrdAct;

    // ════════════════════════════════════════════════════════
    // DATA & STATE
    // ════════════════════════════════════════════════════════
    private static final NumberFormat VND = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
    private List<AuctionListItem> cachedAuctions = new ArrayList<>();
    private final ObservableList<String> notifications = FXCollections.observableArrayList();
    private XYChart.Series<String, Number> revenueSeries;

    private final ObservableList<ProductRow> allProducts = FXCollections.observableArrayList();
    private FilteredList<ProductRow> filteredProducts;
    private final ObservableList<OrderRow> allOrders = FXCollections.observableArrayList();

    private ProductRow editingProduct = null;
    private String currentSellerId = "";
    private String currentSellerName = "";

    // Bộ lắng nghe sự kiện mạng (Được đăng ký / hủy đăng ký tối ưu)
    private java.util.function.Consumer<PacketMessage> socketListener;

    // Định dạng cấu trúc giao diện CSS chuẩn của Sidebar
    private static final String NAV_ACTIVE = "-fx-background-color: #EBF5FB; -fx-text-fill: #1A5276;" +
            "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13; -fx-font-weight: bold;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #5D6D7E;" +
            "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";

    // ════════════════════════════════════════════════════════
    // DTO — Đối tượng biểu diễn hàng dữ liệu trên TableView
    // ════════════════════════════════════════════════════════
    public static class ProductRow {
        public String id, name, category, startPrice, curPrice, status;

        public ProductRow(String id, String name, String category,
                String startPrice, String curPrice, String status) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.startPrice = startPrice;
            this.curPrice = curPrice;
            this.status = status;
        }
    }

    public static class OrderRow {
        public String id, itemName, buyer, price, time, status;

        public OrderRow(String id, String itemName, String buyer,
                String price, String time, String status) {
            this.id = id;
            this.itemName = itemName;
            this.buyer = buyer;
            this.price = price;
            this.time = time;
            this.status = status;
        }
    }

    // ════════════════════════════════════════════════════════
    // INITIALIZE
    // ════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Đọc thông tin Seller từ SessionManager
        SessionManager session = SessionManager.getInstance();
        currentSellerId = session.getCurrentUser() != null ? session.getCurrentUser().getId() : "";
        currentSellerName = session.getUsername();

        setupNavButtons();
        setupProductTable();
        setupOrderTable();
        setupFormCategoryListener();
        setupRevenueChart();
        loadSellerInfo();

        // 🔗 [DỌN DẸP & KHỞI TẠO MẠNG CHUẨN]
        socketListener = this::handleServerMessage;
        ClientSocketManager.getInstance().addMessageListener(socketListener);

        // Gửi yêu cầu lấy danh sách đấu giá thực tế của Seller từ Server
        requestMyAuctions();

        if (datePickerStart != null) {
            datePickerStart.setValue(LocalDate.now());
        }

        // Hiển thị mặc định pane Tổng quan
        showPane(paneOverview, btnNavOverview);
    }

    // ════════════════════════════════════════════════════════
    // SETUP
    // ════════════════════════════════════════════════════════
    private void setupRevenueChart() {
        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu");

        chartRevenue.getData().add(revenueSeries);
        chartRevenue.setAnimated(true);
        chartRevenue.setLegendVisible(false);
        chartRevenue.setCreateSymbols(true);
        chartRevenue.setStyle("-fx-background-color: transparent;");

        axisRevenue.setAutoRanging(true);
        axisRevenue.setForceZeroInRange(true);
        axisRevenue.setLabel("VNĐ");

        axisWeek.setAutoRanging(true);
        axisWeek.setLabel("");

        chartRevenue.getStylesheets().add(
                "data:text/css," +
                        ".chart-series-line { -fx-stroke: #27AE60; -fx-stroke-width: 2.5; }" +
                        ".chart-line-symbol { -fx-background-color: #27AE60, white;" +
                        "                     -fx-background-radius: 4; }" +
                        ".chart-plot-background { -fx-background-color: transparent; }" +
                        ".chart-horizontal-grid-lines { -fx-stroke: #F0F0F0; }" +
                        ".chart-vertical-grid-lines   { -fx-stroke: transparent; }" +
                        ".axis { -fx-tick-label-fill: #7F8C8D; }" +
                        ".axis-label { -fx-text-fill: #7F8C8D; -fx-font-size: 11; }");
    }

    private final List<Button> navButtons() {
        return List.of(btnNavOverview, btnNavOrders, btnNavProducts, btnNavStats, btnNavShop);
    }

    private void setupNavButtons() {
        navButtons().forEach(b -> b.setStyle(NAV_INACTIVE));
        btnNavOverview.setStyle(NAV_ACTIVE);
    }

    private void setupProductTable() {
        colItemId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colAucStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        colAucStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(s);
                setStyle(switch (s) {
                    case "RUNNING" -> "-fx-text-fill: #27AE60; -fx-font-weight: bold;";
                    case "OPEN" -> "-fx-text-fill: #1A5276; -fx-font-weight: bold;";
                    case "FINISHED" -> "-fx-text-fill: #95A5A6;";
                    case "CANCELED" -> "-fx-text-fill: #E74C3C;";
                    default -> "";
                });
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnCancel = new Button("Huỷ phiên");
            private final HBox box = new HBox(6, btnEdit, btnCancel);
            {
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8; -fx-font-size: 11;");
                btnCancel.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8; -fx-font-size: 11;");
                btnEdit.setOnAction(e -> {
                    ProductRow row = getTableRow().getItem();
                    if (row != null) {
                        openEditForm(row);
                    }
                });
                btnCancel.setOnAction(e -> {
                    ProductRow row = getTableRow().getItem();
                    if (row != null) {
                        handleCancelAuction(row);
                    }
                });
            }

            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : box);
            }
        });

        filteredProducts = new FilteredList<>(allProducts, p -> true);
        tblProducts.setItems(filteredProducts);

        tblProducts.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                ProductRow row = tblProducts.getSelectionModel().getSelectedItem();
                if (row != null) {
                    openEditForm(row);
                }
            }
        });

        if (cmbFilterCategory != null) {
            cmbFilterCategory.setItems(FXCollections.observableArrayList(
                    "Tất cả", "Electronics", "Art", "Vehicle"));
            cmbFilterCategory.getSelectionModel().selectFirst();
        }
        if (cmbFormCategory != null) {
            cmbFormCategory.setItems(FXCollections.observableArrayList(
                    "Electronics", "Art", "Vehicle"));
        }
    }

    private void setupOrderTable() {
        colOrdId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colOrdItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().itemName));
        colOrdBuyer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().buyer));
        colOrdPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price));
        colOrdTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time));
        colOrdStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        colOrdAct.setCellFactory(col -> new TableCell<>() {
            private final Button btnConfirm = new Button("Xác nhận");
            {
                btnConfirm.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10; -fx-font-size: 11;");
                btnConfirm.setOnAction(e -> {
                    OrderRow row = getTableRow().getItem();
                    if (row != null) {
                        handleConfirmOrder(row);
                    }
                });
            }

            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btnConfirm);
            }
        });

        tblOrders.setItems(allOrders);
    }

    private void setupFormCategoryListener() {
        if (cmbFormCategory != null) {
            cmbFormCategory.valueProperty().addListener((obs, old, nw) -> {
                if (nw != null) {
                    switchCategoryFields(nw);
                }
            });
        }
    }

    private void switchCategoryFields(String category) {
        vboxElectronics.setVisible(false);
        vboxElectronics.setManaged(false);
        vboxArt.setVisible(false);
        vboxArt.setManaged(false);
        vboxVehicle.setVisible(false);
        vboxVehicle.setManaged(false);

        switch (category) {
            case "Electronics" -> {
                vboxElectronics.setVisible(true);
                vboxElectronics.setManaged(true);
            }
            case "Art" -> {
                vboxArt.setVisible(true);
                vboxArt.setManaged(true);
            }
            case "Vehicle" -> {
                vboxVehicle.setVisible(true);
                vboxVehicle.setManaged(true);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // MẠNG XỬ LÝ SỰ KIỆN (SOCKET HANDLER)
    // ════════════════════════════════════════════════════════
    private void requestMyAuctions() {
        ClientSocketManager.getInstance()
                .sendPacket(new PacketMessage(MessageType.REQUEST_MY_AUCTIONS, currentSellerId));
    }

    private void handleServerMessage(PacketMessage packet) {
        Platform.runLater(() -> {
            if (packet.getType() == MessageType.SEND_MY_AUCTIONS) {
                AuctionListPayload payload = (AuctionListPayload) packet.getPayload();
                if (payload != null) {
                    this.cachedAuctions = payload.getAuctionList();
                    renderSellerData();
                }
            } else if (packet.getType() == MessageType.AUCTION_CONCLUDED
                    || packet.getType() == MessageType.AUCTION_CANCELLED
                    || packet.getType() == MessageType.SEND_ACTIVE_AUCTION_LIST) {
                // Tự động làm mới dữ liệu khi có cập nhật thời gian thực từ Server
                requestMyAuctions();
            } else if (packet.getType() == MessageType.ERROR) {
                ErrorMessagePayload errorPayload = (ErrorMessagePayload) packet.getPayload();
                if (errorPayload != null) {
                    String errorMsg = errorPayload.getErrorMessage();
                    if (paneProducts.isVisible() && lblFormError != null) {
                        showFormError(errorMsg);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText("Không thể thực hiện yêu cầu");
                        alert.setContentText(errorMsg);
                        alert.showAndWait();
                    }
                }
            }
        });
    }

    private void renderSellerData() {
        // 1. Ánh xạ danh sách sản phẩm (Product List)
        List<ProductRow> productRows = cachedAuctions.stream().map(a -> {
            String startPrice = VND.format((long) a.getItemStartingPrice());
            String curPrice = VND.format((long) a.getHighestBid());
            return new ProductRow(a.getAuctionID(), a.getItemName(), a.getCategory(), startPrice, curPrice,
                    a.getStatus());
        }).collect(Collectors.toList());

        allProducts.setAll(productRows);
        updateProductLabels();

        // 2. Ánh xạ danh sách đơn hàng đã kết thúc (Orders)
        allOrders.clear();
        List<OrderRow> orderRows = cachedAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()) || "PAID".equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getHighestBidderId() != null && !a.getHighestBidderId().isEmpty())
                .map(a -> {
                    String price = VND.format((long) a.getHighestBid()) + " đ";
                    String displayStatus = "PAID".equalsIgnoreCase(a.getStatus()) ? "Đã xác nhận" : "Chờ xác nhận";
                    return new OrderRow(a.getAuctionID(), a.getItemName(), a.getHighestBidderId(), price,
                            a.getEndTime(), displayStatus);
                }).collect(Collectors.toList());
        allOrders.addAll(orderRows);

        // 3. Làm mới bảng kê thông báo
        notifications.clear();
        for (AuctionListItem a : cachedAuctions) {
            String status = a.getStatus();
            String itemName = a.getItemName();
            String timeStr = a.getEndTime();

            if ("FINISHED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                if (a.getHighestBidderId() != null && !a.getHighestBidderId().isEmpty()) {
                    notifications.add("✅ " + itemName + "\n" + a.getHighestBidderId() + " thắng với "
                            + VND.format((long) a.getHighestBid()) + " đ (" + timeStr + ")");
                } else {
                    notifications.add("❌ " + itemName + "\nPhiên kết thúc không có người thắng (" + timeStr + ")");
                }
            } else if ("CANCELED".equalsIgnoreCase(status)) {
                notifications.add("🚫 " + itemName + "\nPhiên đã bị huỷ (" + timeStr + ")");
            }
        }
        int count = notifications.size();
        btnNotify.setText(count > 0 ? "🔔 " + count : "🔔");

        // 4. Cập nhật các chỉ số tổng quan (Dashboard stats)
        loadOverviewStats();
        loadWeeklyRevenue(); // Cập nhật biểu đồ doanh thu theo tuần
    }

    // ════════════════════════════════════════════════════════
    // TẢI THÔNG TIN CÁ NHÂN SHOP
    // ════════════════════════════════════════════════════════
    private void loadSellerInfo() {
        Seller seller = SessionManager.getInstance().asSeller();
        btnSellerUser.setText("👤 " + currentSellerName);
        lblShopName.setText(currentSellerName + "'s Shop");

        if (seller != null) {
            lblShopCompany.setText(
                    seller.getCompanyName() != null && !seller.getCompanyName().isEmpty() ? seller.getCompanyName()
                            : "Chưa cập nhật");
            lblShopRating.setText(String.format("⭐ %.1f / 5.0", seller.getRating()));
        } else {
            lblShopCompany.setText("Chưa cập nhật");
            lblShopRating.setText("⭐ -- / 5.0");
        }
        lblShopDesc.setText("Hệ thống đấu giá chuyên nghiệp tích hợp Socket.");
    }

    private void loadOverviewStats() {
        long active = allProducts.stream().filter(p -> "RUNNING".equals(p.status)).count();
        long pending = allOrders.stream().filter(o -> "Chờ xác nhận".equals(o.status)).count();
        long confirmed = allOrders.stream().filter(o -> "Đã xác nhận".equals(o.status)).count();

        long totalRevenue = cachedAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()) || "PAID".equalsIgnoreCase(a.getStatus()))
                .mapToLong(a -> (long) a.getHighestBid())
                .sum();

        lblStatSales.setText(String.valueOf(allProducts.size()));
        lblStatSalesDelta.setText("—");
        lblStatOrders.setText(String.valueOf(allOrders.size()));
        lblStatOrdersDelta.setText("—");
        lblStatRevenue.setText(VND.format(totalRevenue));
        lblStatRevenueDelta.setText("—");

        lblOrdersPending.setText(String.valueOf(pending));
        lblOrdersPickup.setText(String.valueOf(active));
        lblOrdersDone.setText(String.valueOf(confirmed));
    }

    // ════════════════════════════════════════════════════════
    // BIỂU ĐỒ DOANH THU
    // ════════════════════════════════════════════════════════
    private void loadWeeklyRevenue() {
        revenueSeries.getData().clear();
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        String[] labels = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
        Map<String, Long> revenueMap = new TreeMap<>();
        for (String label : labels) {
            revenueMap.put(label, 0L);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuctionListItem a : cachedAuctions) {
            String s = a.getStatus();
            if ((!"FINISHED".equalsIgnoreCase(s) && !"PAID".equalsIgnoreCase(s)) || a.getEndTime() == null) {
                continue;
            }
            try {
                LocalDate endDate = LocalDateTime.parse(a.getEndTime(), fmt).toLocalDate();
                if (endDate.isBefore(monday) || endDate.isAfter(monday.plusDays(6))) {
                    continue;
                }

                String label = switch (endDate.getDayOfWeek()) {
                    case MONDAY -> "T2";
                    case TUESDAY -> "T3";
                    case WEDNESDAY -> "T4";
                    case THURSDAY -> "T5";
                    case FRIDAY -> "T6";
                    case SATURDAY -> "T7";
                    case SUNDAY -> "CN";
                };
                revenueMap.merge(label, (long) a.getHighestBid(), Long::sum);
            } catch (Exception ignored) {
            }
        }

        for (String label : labels) {
            revenueSeries.getData().add(new XYChart.Data<>(label, revenueMap.get(label)));
        }
    }

    private void loadMonthlyRevenue() {
        revenueSeries.getData().clear();
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("'Tháng' M");

        Map<String, Long> revenueMap = new java.util.LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            revenueMap.put(month.format(monthFmt), 0L);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuctionListItem a : cachedAuctions) {
            String s = a.getStatus();
            if ((!"FINISHED".equalsIgnoreCase(s) && !"PAID".equalsIgnoreCase(s)) || a.getEndTime() == null) {
                continue;
            }
            try {
                String label = LocalDateTime.parse(a.getEndTime(), fmt).toLocalDate().format(monthFmt);
                if (!revenueMap.containsKey(label)) {
                    continue;
                }
                revenueMap.merge(label, (long) a.getHighestBid(), Long::sum);
            } catch (Exception ignored) {
            }
        }

        revenueMap.forEach((label, revenue) -> revenueSeries.getData().add(new XYChart.Data<>(label, revenue)));
    }

    public void onRevenueUpdate(String label, double amount) {
        Platform.runLater(() -> revenueSeries.getData().add(new XYChart.Data<>(label, amount)));
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — SIDEBAR NAVIGATION
    // ════════════════════════════════════════════════════════
    @FXML
    private void handleSellerNav(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        if (clicked == btnNavOverview) {
            showPane(paneOverview, btnNavOverview);
        } else if (clicked == btnNavOrders) {
            showPane(paneOrders, btnNavOrders);
        } else if (clicked == btnNavProducts) {
            showPane(paneProducts, btnNavProducts);
            resetForm();
        } else if (clicked == btnNavStats || clicked == btnNavShop) {
            showPane(paneOverview, clicked);
        }
    }

    private void showPane(VBox target, Button activeBtn) {
        for (VBox pane : List.of(paneOverview, paneProducts, paneOrders)) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        target.setVisible(true);
        target.setManaged(true);
        navButtons().forEach(b -> b.setStyle(NAV_INACTIVE));
        activeBtn.setStyle(NAV_ACTIVE);
    }

    @FXML
    private void handleChartPeriod(ActionEvent event) {
        Button src = (Button) event.getSource();
        String activeStyle = "-fx-background-color: #2C3E50; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 12;";
        String inactiveStyle = "-fx-background-color: #F0F3F7; -fx-text-fill: #5D6D7E;" +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 12;";

        if (src == btnChartWeek) {
            btnChartWeek.setStyle(activeStyle);
            btnChartMonth.setStyle(inactiveStyle);
            loadWeeklyRevenue();
        } else {
            btnChartMonth.setStyle(activeStyle);
            btnChartWeek.setStyle(inactiveStyle);
            loadMonthlyRevenue();
        }
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — FORM ĐĂNG BÁN SẢN PHẨM MỚI
    // ════════════════════════════════════════════════════════
    @FXML
    private void handleNew() {
        showPane(paneProducts, btnNavProducts);
        resetForm();
    }

    private void openEditForm(ProductRow row) {
        editingProduct = row;
        lblFormTitle.setText("Chỉnh sửa sản phẩm");
        txtName.setText(row.name);
        txtStartingPrice.setText(row.startPrice.replace(".", "").replace(",", ""));
        if (cmbFormCategory != null) {
            cmbFormCategory.setValue(row.category);
        }
        lblFormError.setText("");
    }

    private void resetForm() {
        editingProduct = null;
        lblFormTitle.setText("Thêm sản phẩm mới");
        txtName.clear();
        txtDescription.clear();
        txtStartingPrice.clear();
        txtBrand.clear();
        txtWarranty.clear();
        txtArtist.clear();
        txtMedium.clear();
        txtEngineType.clear();
        txtModelYear.clear();
        if (datePickerStart != null) {
            datePickerStart.setValue(LocalDate.now());
        }
        if (txtHourStart != null) {
            txtHourStart.setText("00");
        }
        if (txtMinStart != null) {
            txtMinStart.setText("00");
        }
        if (datePickerEnd != null) {
            datePickerEnd.setValue(null);
        }
        if (txtHourEnd != null) {
            txtHourEnd.setText("23");
        }
        if (txtMinEnd != null) {
            txtMinEnd.setText("59");
        }
        lblFormError.setText("");
        if (cmbFormCategory != null) {
            cmbFormCategory.getSelectionModel().clearSelection();
        }
        switchCategoryFields("Tất cả");
    }

    @FXML
    private void handleCategoryChange() {
        if (cmbFilterCategory != null && cmbFilterCategory.getValue() != null) {
            String selected = cmbFilterCategory.getValue();
            if ("Tất cả".equals(selected)) {
                filteredProducts.setPredicate(p -> true);
            } else {
                filteredProducts.setPredicate(p -> selected.equalsIgnoreCase(p.category));
            }
            updateProductLabels();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        String name = txtName.getText().trim();
        String category = cmbFormCategory != null ? cmbFormCategory.getValue() : "Electronics";
        String desc = txtDescription.getText().trim();
        double price;
        try {
            price = Double.parseDouble(txtStartingPrice.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            showFormError("Giá khởi điểm phải là số nguyên dương.");
            return;
        }

        LocalDate startDate = datePickerStart.getValue() != null ? datePickerStart.getValue() : LocalDate.now();
        int startH = 0;
        try { startH = Integer.parseInt(txtHourStart.getText().trim()); } catch (Exception ignored) {}
        int startM = 0;
        try { startM = Integer.parseInt(txtMinStart.getText().trim()); } catch (Exception ignored) {}
        LocalDateTime startDateTime = startDate.atTime(startH, startM);

        LocalDate endDate = datePickerEnd.getValue();
        int endH = 23;
        try { endH = Integer.parseInt(txtHourEnd.getText().trim()); } catch (Exception ignored) {}
        int endM = 59;
        try { endM = Integer.parseInt(txtMinEnd.getText().trim()); } catch (Exception ignored) {}
        LocalDateTime endDateTime = endDate.atTime(endH, endM);

        long diffInMinutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes();
        int auctionDuration = diffInMinutes > 0 ? (int) diffInMinutes : 1440; // Mặc định tối thiểu 1 ngày

        // 🔗 [DỌN SẠCH HARDCODE] — Đọc đầy đủ các tham số cấu hình riêng theo loại sản phẩm
        String brand = "";
        String model = "";
        int warranty = 0;
        String artist = "";
        String medium = "";
        int artYear = LocalDate.now().getYear();
        String engineType = "";
        int modelYear = LocalDate.now().getYear();
        double mileage = 0.0;
        String licensePlate = "N/A";

        ItemCategory itemCategory = ItemCategory.ELECTRONICS;

        if ("Electronics".equalsIgnoreCase(category)) {
            itemCategory = ItemCategory.ELECTRONICS;
            brand = txtBrand.getText().trim();
            model = brand; // Gán model đồng bộ với brand
            try {
                if (!txtWarranty.getText().trim().isEmpty()) {
                    warranty = Integer.parseInt(txtWarranty.getText().trim());
                }
            } catch (NumberFormatException ignored) {
            }
        } else if ("Art".equalsIgnoreCase(category)) {
            itemCategory = ItemCategory.ART;
            artist = txtArtist.getText().trim();
            medium = txtMedium.getText().trim();
        } else if ("Vehicle".equalsIgnoreCase(category)) {
            itemCategory = ItemCategory.VEHICLE;
            engineType = txtEngineType.getText().trim();
            try {
                if (!txtModelYear.getText().trim().isEmpty()) {
                    modelYear = Integer.parseInt(txtModelYear.getText().trim());
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Đóng gói cấu trúc Payload tạo Auction
        CreateAuctionPayload payload = new CreateAuctionPayload(
                "PUBLIC",
                price,
                name,
                desc,
                auctionDuration,
                itemCategory,
                brand,
                model,
                warranty,
                artist,
                medium,
                artYear,
                engineType,
                modelYear,
                mileage,
                licensePlate);

        // Gán thời gian bắt đầu
        payload.setStartTime(startDateTime);

        if (editingProduct == null) {
            // Gửi yêu cầu đăng bán sản phẩm mới qua Socket mạng
            ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.CREATE_AUCTION, payload));
            showFormError("");
            resetForm();
        } else {
            // Sửa đổi vật phẩm
            // Để cập nhật, ta đóng gói các trường chỉnh sửa truyền lên hệ thống qua MessageType.AUCTION_UPDATE
            ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.AUCTION_UPDATE, payload));
            resetForm();
        }

        // Làm mới dữ liệu bảng biểu
        requestMyAuctions();
    }

    @FXML
    private void handleCancel() {
        resetForm();
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showFormError("Vui lòng nhập tên sản phẩm.");
            return false;
        }
        if (cmbFormCategory == null || cmbFormCategory.getValue() == null) {
            showFormError("Vui lòng chọn danh mục.");
            return false;
        }
        if (txtStartingPrice.getText().trim().isEmpty()) {
            showFormError("Vui lòng nhập giá khởi điểm.");
            return false;
        }
        if (datePickerEnd.getValue() == null) {
            showFormError("Vui lòng chọn ngày kết thúc.");
            return false;
        }

        LocalDate startDate = datePickerStart.getValue() != null ? datePickerStart.getValue() : LocalDate.now();
        int startH = 0;
        try { startH = Integer.parseInt(txtHourStart.getText().trim()); } catch (Exception ignored) {}
        int startM = 0;
        try { startM = Integer.parseInt(txtMinStart.getText().trim()); } catch (Exception ignored) {}
        LocalDateTime startDateTime = startDate.atTime(startH, startM);

        LocalDate endDate = datePickerEnd.getValue();
        int endH = 23;
        try { endH = Integer.parseInt(txtHourEnd.getText().trim()); } catch (Exception ignored) {}
        int endM = 59;
        try { endM = Integer.parseInt(txtMinEnd.getText().trim()); } catch (Exception ignored) {}
        LocalDateTime endDateTime = endDate.atTime(endH, endM);

        if (endDateTime.isBefore(startDateTime.plusMinutes(5))) {
            showFormError("Thời gian kết thúc phải sau thời gian bắt đầu ít nhất 5 phút.");
            return false;
        }
        return true;
    }

    private void showFormError(String msg) {
        lblFormError.setText(msg);
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — BẢNG KÊ SẢN PHẨM
    // ════════════════════════════════════════════════════════
    @FXML
    private void handleTableClick(javafx.scene.input.MouseEvent e) {
        // Double click được quản lý qua phương thức setupProductTable()
    }

    @FXML
    private void handleSearchProduct() {
        String kw = txtSearchProduct.getText().trim().toLowerCase();
        filteredProducts.setPredicate(row -> kw.isEmpty()
                || row.name.toLowerCase().contains(kw)
                || row.id.toLowerCase().contains(kw));
        updateProductLabels();
    }

    private void updateProductLabels() {
        long active = allProducts.stream().filter(p -> "RUNNING".equals(p.status)).count();
        long finished = allProducts.stream().filter(p -> "FINISHED".equals(p.status)).count();
        lblTotalProducts.setText("Tổng: " + allProducts.size() + " sản phẩm");
        lblActiveAuctions.setText("Đang đấu giá: " + active);
        lblFinishedAuctions.setText("Đã kết thúc: " + finished);
        lblShopTotalItems.setText(String.valueOf(allProducts.size()));
        lblShopTotalSold.setText(String.valueOf(finished));
    }

    private void handleCancelAuction(ProductRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn chắc chắn muốn huỷ phiên đấu giá \"" + row.name + "\"?\nHành động này không thể hoàn tác.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận huỷ phiên");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // Gửi gói tin yêu cầu hủy phiên thực tế lên Server thông qua Socket mạng
                ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.CANCEL_AUCTION, row.id));
            }
        });
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — BẢNG ĐƠN HÀNG
    // ════════════════════════════════════════════════════════
    private void handleConfirmOrder(OrderRow row) {
        // Đồng bộ hóa trạng thái giao dịch
        ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.AUCTION_UPDATE, row.id));
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — THANH TÌM KIẾM TOÀN CỤC & THÔNG BÁO
    // ════════════════════════════════════════════════════════
    @FXML
    private void handleGlobalSearch() {
        String kw = txtGlobalSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            return;
        }
        showPane(paneProducts, btnNavProducts);
        filteredProducts.setPredicate(row -> row.name.toLowerCase().contains(kw) || row.id.toLowerCase().contains(kw));
        updateProductLabels();
    }

    @FXML
    private void handleNotify() {
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        Label title = new Label("🔔 Thông báo (" + notifications.size() + ")");
        title.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-padding: 10 14 8 14;");

        ListView<String> listView = new ListView<>(notifications);
        listView.setPrefWidth(340);
        listView.setPrefHeight(Math.min(notifications.size() * 52 + 10, 260));
        listView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    setStyle(
                            "-fx-padding: 8 12; -fx-font-size: 12; -fx-border-color: transparent transparent #F0F3F7 transparent;");
                }
            }
        });

        if (notifications.isEmpty()) {
            Label empty = new Label("Không có thông báo mới.");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 12; -fx-padding: 16;");
            listView.setPlaceholder(empty);
            listView.setPrefHeight(60);
        }

        VBox container = new VBox(title, listView);
        container.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: #E8ECF0;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);");

        popup.getContent().add(container);

        double x = btnNotify.localToScreen(0, 0).getX() + btnNotify.getWidth() - 340;
        double y = btnNotify.localToScreen(0, 0).getY() + btnNotify.getHeight() + 6;
        popup.show(btnNotify.getScene().getWindow(), x, y);
    }

    public void addNotification(String message) {
        Platform.runLater(() -> {
            notifications.add(0, message);
            btnNotify.setText("🔔 " + notifications.size());
        });
    }

    @FXML
    private void handleSellerProfile() {
        System.out.println("Mở hồ sơ người bán");
    }

    @FXML
    private void handleShopSettings() {
        System.out.println("[SellerDashboard] Mở cài đặt shop của tôi");
    }

    @FXML
    private void handleLogout() {
        try {
            // 🔗 [DỌN SẠCH LUỒNG KẾT NỐI] — Ngắt kết nối mạng an toàn, giải phóng luồng
            // ClientHandler
            ClientSocketManager.getInstance().sendPacket(new PacketMessage(MessageType.DISCONNECT, currentSellerId));

            // Hủy đăng ký listener để dọn dẹp RAM ở Client
            if (socketListener != null) {
                ClientSocketManager.getInstance().removeMessageListener(socketListener);
            }

            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnNavOverview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════
    // API CHUYỂN DỮ LIỆU
    // ════════════════════════════════════════════════════════
    @Deprecated
    public void setCurrentSeller(String sellerId, String sellerName) {
        // Sử dụng SessionManager tự động
    }

    public void onReceiveMyAuctions(List<ProductRow> products) {
        Platform.runLater(() -> {
            allProducts.setAll(products);
            updateProductLabels();
            loadOverviewStats();
        });
    }

    public void onAuctionConcluded(OrderRow order, double revenue) {
        Platform.runLater(() -> {
            allOrders.add(0, order);
            onRevenueUpdate(order.time, revenue);
            loadOverviewStats();
        });
    }
}

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

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * SellerDashboardController
 * FXML: SellerDashboard.fxml
 *
 * Quan ly toan bo giao dien Seller Hub:
 *   - Sidebar dieu huong 5 muc: Tong quan / Don hang / San pham / Hieu suat / Shop
 *   - paneOverview : stat cards, don hang can xu ly, bieu do doanh thu (LineChart truc tiep), ho so shop
 *   - paneProducts : form them/sua san pham (dong theo loai), table san pham
 *   - paneOrders   : table don hang da ket thuc dau gia
 *
 * Ket noi voi cac thanh vien khac:
 *   - Nguoi 1 (Models)  : ProductRow su dung Item, Electronics, Art, Vehicle
 *                         ItemFactory.createItem() khi gui len server
 *   - Nguoi 2 (DAO)     : DAO.getProductsBySeller(), DAO.getOrdersBySeller()
 *   - Nguoi 3 (Network) : networkClient.send(PacketMessage) tai cac diem TODO
 *                         PacketMessage voi MessageType: CREATE_AUCTION, REQUEST_MY_AUCTIONS,
 *                         SEND_MY_AUCTIONS, CANCEL_AUCTION, AUCTION_CONCLUDED
 */
public class SellerDashboardController implements Initializable {

    // ════════════════════════════════════════════════════════
    // FXML — TOP BAR
    // ════════════════════════════════════════════════════════
    @FXML private TextField txtGlobalSearch;
    @FXML private Button    btnSellerUser;

    // ════════════════════════════════════════════════════════
    // FXML — SIDEBAR NAV
    // ════════════════════════════════════════════════════════
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavOrders;
    @FXML private Button btnNavProducts;
    @FXML private Button btnNavStats;
    @FXML private Button btnNavShop;

    // ════════════════════════════════════════════════════════
    // FXML — 3 PANE CHINH
    // ════════════════════════════════════════════════════════
    @FXML private VBox paneOverview;
    @FXML private VBox paneProducts;
    @FXML private VBox paneOrders;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: STAT CARDS
    // ════════════════════════════════════════════════════════
    @FXML private Label lblStatSales;
    @FXML private Label lblStatSalesDelta;
    @FXML private Label lblStatOrders;
    @FXML private Label lblStatOrdersDelta;
    @FXML private Label lblStatRevenue;
    @FXML private Label lblStatRevenueDelta;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: DON HANG CAN XU LY
    // ════════════════════════════════════════════════════════
    @FXML private Label lblOrdersPending;
    @FXML private Label lblOrdersPickup;
    @FXML private Label lblOrdersDone;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: BIEU DO DOANH THU
    // ════════════════════════════════════════════════════════
    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private CategoryAxis axisWeek;
    @FXML private NumberAxis   axisRevenue;
    @FXML private Button btnChartWeek;
    @FXML private Button btnChartMonth;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: HO SO SHOP
    // ════════════════════════════════════════════════════════
    @FXML private Label lblShopName;
    @FXML private Label lblShopCompany;
    @FXML private Label lblShopRating;
    @FXML private Label lblShopDesc;
    @FXML private Label lblShopTotalItems;
    @FXML private Label lblShopTotalSold;

    // ════════════════════════════════════════════════════════
    // FXML — PRODUCTS: FORM THEM/SUA
    // ════════════════════════════════════════════════════════
    @FXML private Label    lblFormTitle;
    @FXML private TextField txtName;
    @FXML private TextArea  txtDescription;
    @FXML private ComboBox<String> cmbCategory;

    // Nhom field dong theo loai san pham
    @FXML private VBox      vboxElectronics;
    @FXML private TextField txtBrand;
    @FXML private TextField txtWarranty;

    @FXML private VBox      vboxArt;
    @FXML private TextField txtArtist;
    @FXML private TextField txtMedium;

    @FXML private VBox      vboxVehicle;
    @FXML private TextField txtEngineType;
    @FXML private TextField txtModelYear;

    @FXML private TextField txtStartingPrice;
    @FXML private DatePicker datePicker;
    @FXML private Label     lblFormError;
    @FXML private Button    btnSave;
    @FXML private Button    btnCancel;

    // ════════════════════════════════════════════════════════
    // FXML — PRODUCTS: TABLE + LABELS
    // ════════════════════════════════════════════════════════
    @FXML private TableView<ProductRow>           tblProducts;
    @FXML private TableColumn<ProductRow, String> colItemId;
    @FXML private TableColumn<ProductRow, String> colName;
    @FXML private TableColumn<ProductRow, String> colCat;
    @FXML private TableColumn<ProductRow, String> colPrice;
    @FXML private TableColumn<ProductRow, String> colCurPrice;
    @FXML private TableColumn<ProductRow, String> colAucStatus;
    @FXML private TableColumn<ProductRow, String> colActions;
    @FXML private TextField txtSearchProduct;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblFinishedAuctions;

    // ════════════════════════════════════════════════════════
    // FXML — ORDERS: TABLE
    // ════════════════════════════════════════════════════════
    @FXML private TableView<OrderRow>           tblOrders;
    @FXML private TableColumn<OrderRow, String> colOrdId;
    @FXML private TableColumn<OrderRow, String> colOrdItem;
    @FXML private TableColumn<OrderRow, String> colOrdBuyer;
    @FXML private TableColumn<OrderRow, String> colOrdPrice;
    @FXML private TableColumn<OrderRow, String> colOrdTime;
    @FXML private TableColumn<OrderRow, String> colOrdStatus;
    @FXML private TableColumn<OrderRow, String> colOrdAct;

    // ════════════════════════════════════════════════════════
    // DATA & STATE
    // ════════════════════════════════════════════════════════

    /** Bieu do doanh thu — dung LineChart truc tiep, khong lien quan BidPriceLineChart */
    private XYChart.Series<String, Number> revenueSeries;

    private final ObservableList<ProductRow> allProducts = FXCollections.observableArrayList();
    private       FilteredList<ProductRow>   filteredProducts;

    private final ObservableList<OrderRow>   allOrders   = FXCollections.observableArrayList();

    /** Dang sua san pham nao (null = che do them moi) */
    private ProductRow editingProduct = null;

    /** Seller hien tai — se duoc set tu LoginController sau khi dang nhap */
    private String currentSellerId   = "SELLER_001";
    private String currentSellerName = "Nguyen Van A";

    // Style cua nav button khi active / inactive
    private static final String NAV_ACTIVE =
        "-fx-background-color: #EBF5FB; -fx-text-fill: #1A5276;" +
        "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8;" +
        "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13; -fx-font-weight: bold;";
    private static final String NAV_INACTIVE =
        "-fx-background-color: transparent; -fx-text-fill: #5D6D7E;" +
        "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8;" +
        "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";

    // ════════════════════════════════════════════════════════
    // DTO — hang trong TableView (thay bang model thuc te cua Nguoi 1)
    // ════════════════════════════════════════════════════════

    /** Dai dien 1 dong trong bang San pham — map tu Item.java cua Nguoi 1 */
    public static class ProductRow {
        public String id, name, category, startPrice, curPrice, status;
        public ProductRow(String id, String name, String category,
                          String startPrice, String curPrice, String status) {
            this.id = id; this.name = name; this.category = category;
            this.startPrice = startPrice; this.curPrice = curPrice; this.status = status;
        }
    }

    /** Dai dien 1 dong trong bang Don hang — map tu BidTransaction.java cua Nguoi 1 */
    public static class OrderRow {
        public String id, itemName, buyer, price, time, status;
        public OrderRow(String id, String itemName, String buyer,
                        String price, String time, String status) {
            this.id = id; this.itemName = itemName; this.buyer = buyer;
            this.price = price; this.time = time; this.status = status;
        }
    }

    // ════════════════════════════════════════════════════════
    // INITIALIZE
    // ════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNavButtons();
        setupProductTable();
        setupOrderTable();
        setupFormCategoryListener();
        setupRevenueChart();
        loadSellerInfo();
        loadProducts();
        loadOrders();
        loadOverviewStats();

        // Hien thi pane Tong quan mac dinh
        showPane(paneOverview, btnNavOverview);
    }

    // ════════════════════════════════════════════════════════
    // SETUP
    // ════════════════════════════════════════════════════════

    /**
     * Khoi tao LineChart doanh thu truc tiep — khong dung BidPriceLineChart
     * vi do la class chuyen biet cho gia dau gia realtime, khong phu hop o day.
     */
    private void setupRevenueChart() {
        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu");

        chartRevenue.getData().add(revenueSeries);
        chartRevenue.setAnimated(true);
        chartRevenue.setLegendVisible(false);
        chartRevenue.setCreateSymbols(true);
        chartRevenue.setStyle("-fx-background-color: transparent;");

        axisRevenue.setAutoRanging(true);
        axisRevenue.setForceZeroInRange(true);  // Doanh thu bat dau tu 0 — khac bid price
        axisRevenue.setLabel("VNĐ");

        axisWeek.setAutoRanging(true);
        axisWeek.setLabel("");

        // CSS don gian cho duong ke doanh thu (mau xanh la — khac cam cua BidPrice)
        chartRevenue.getStylesheets().add(
            "data:text/css," +
            ".chart-series-line { -fx-stroke: #27AE60; -fx-stroke-width: 2.5; }" +
            ".chart-line-symbol { -fx-background-color: #27AE60, white;" +
            "                     -fx-background-radius: 4; }" +
            ".chart-plot-background { -fx-background-color: transparent; }" +
            ".chart-horizontal-grid-lines { -fx-stroke: #F0F0F0; }" +
            ".chart-vertical-grid-lines   { -fx-stroke: transparent; }" +
            ".axis { -fx-tick-label-fill: #7F8C8D; }" +
            ".axis-label { -fx-text-fill: #7F8C8D; -fx-font-size: 11; }"
        );

        loadWeeklyRevenue();
    }

    /** Nav buttons: giu danh sach tat ca de de-active */
    private final List<Button> navButtons() {
        return List.of(btnNavOverview, btnNavOrders, btnNavProducts, btnNavStats, btnNavShop);
    }

    private void setupNavButtons() {
        // Tat ca bat dau o trang thai inactive
        navButtons().forEach(b -> b.setStyle(NAV_INACTIVE));
        btnNavOverview.setStyle(NAV_ACTIVE);
    }

    /** Khoi tao cot TableView san pham */
    private void setupProductTable() {
        colItemId   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colName     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colCat      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colPrice    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colAucStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // To mau cot trang thai
        colAucStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(switch (s) {
                    case "RUNNING" -> "-fx-text-fill: #27AE60; -fx-font-weight: bold;";
                    case "OPEN"    -> "-fx-text-fill: #1A5276; -fx-font-weight: bold;";
                    case "FINISHED"-> "-fx-text-fill: #95A5A6;";
                    case "CANCELED"-> "-fx-text-fill: #E74C3C;";
                    default        -> "";
                });
            }
        });

        // Cot Hanh dong: nut Sua + Huy phien
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Sửa");
            private final Button btnCancel = new Button("Huỷ phiên");
            private final HBox   box       = new HBox(6, btnEdit, btnCancel);
            {
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;" +
                                 "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8; -fx-font-size: 11;");
                btnCancel.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;" +
                                   "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8; -fx-font-size: 11;");
                btnEdit.setOnAction(e -> {
                    ProductRow row = getTableRow().getItem();
                    if (row != null) openEditForm(row);
                });
                btnCancel.setOnAction(e -> {
                    ProductRow row = getTableRow().getItem();
                    if (row != null) handleCancelAuction(row);
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : box);
            }
        });

        // FilteredList cho o tim kiem san pham
        filteredProducts = new FilteredList<>(allProducts, p -> true);
        tblProducts.setItems(filteredProducts);

        // Click vao dong → load len form sua
        tblProducts.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                ProductRow row = tblProducts.getSelectionModel().getSelectedItem();
                if (row != null) openEditForm(row);
            }
        });

        // Combobox danh muc
        if (cmbCategory != null) {
            cmbCategory.setItems(FXCollections.observableArrayList(
                "Tất cả", "Electronics", "Art", "Vehicle"));
            cmbCategory.getSelectionModel().selectFirst();
        }
    }

    /** Khoi tao cot TableView don hang */
    private void setupOrderTable() {
        colOrdId    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colOrdItem  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().itemName));
        colOrdBuyer .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().buyer));
        colOrdPrice .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price));
        colOrdTime  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time));
        colOrdStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // Cot Hanh dong don hang: nut Xac nhan / Xem
        colOrdAct.setCellFactory(col -> new TableCell<>() {
            private final Button btnConfirm = new Button("Xác nhận");
            {
                btnConfirm.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10; -fx-font-size: 11;");
                btnConfirm.setOnAction(e -> {
                    OrderRow row = getTableRow().getItem();
                    if (row != null) handleConfirmOrder(row);
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btnConfirm);
            }
        });

        tblOrders.setItems(allOrders);
    }

    /**
     * Lang nghe thay doi danh muc trong form →
     * hien/an nhom field tuong ung (Electronics / Art / Vehicle)
     */
    private void setupFormCategoryListener() {
        // ComboBox trong form them/sua (khac cmbCategory trong toolbar)
        // Vi FXML dung cung fx:id="cmbCategory" nen dung chung
        // Thuc te nen tach ra 2 fx:id rieng biet
        if (cmbCategory != null) {
            cmbCategory.valueProperty().addListener((obs, old, nw) -> {
                if (nw == null) return;
                switchCategoryFields(nw);
            });
        }
    }

    /** Hien field phu hop, an 2 nhom con lai */
    private void switchCategoryFields(String category) {
        vboxElectronics.setVisible(false); vboxElectronics.setManaged(false);
        vboxArt        .setVisible(false); vboxArt        .setManaged(false);
        vboxVehicle    .setVisible(false); vboxVehicle    .setManaged(false);
        switch (category) {
            case "Electronics" -> { vboxElectronics.setVisible(true); vboxElectronics.setManaged(true); }
            case "Art"         -> { vboxArt        .setVisible(true); vboxArt        .setManaged(true); }
            case "Vehicle"     -> { vboxVehicle    .setVisible(true); vboxVehicle    .setManaged(true); }
        }
    }

    // ════════════════════════════════════════════════════════
    // LOAD DU LIEU — se thay bang goi server/DAO
    // ════════════════════════════════════════════════════════

    /**
     * Hien thi thong tin Seller len top bar va ho so shop.
     *
     * Ket noi Nguoi 3:
     *   networkClient.send(new PacketMessage(MessageType.REQUEST_USER_PROFILE, currentSellerId));
     *   → Nguoi 2 (DAO): UserDAO.getById(sellerId) → tra ve Seller object cua Nguoi 1
     */
    private void loadSellerInfo() {
        btnSellerUser.setText("👤 " + currentSellerName);
        lblShopName   .setText(currentSellerName + "'s Shop");
        lblShopCompany.setText("Công ty TNHH Đấu Giá");
        lblShopRating .setText("⭐ 4.5 / 5.0 (12 đánh giá)");
        lblShopDesc   .setText("Chuyên cung cấp hàng hóa đấu giá chất lượng cao.");
        lblShopTotalItems.setText("5");
        lblShopTotalSold .setText("8");
    }

    /**
     * Load danh sach san pham cua Seller nay.
     *
     * Ket noi Nguoi 3:
     *   networkClient.send(new PacketMessage(MessageType.REQUEST_MY_AUCTIONS, currentSellerId));
     * Server phan hoi MessageType.SEND_MY_AUCTIONS voi payload List<Auction>
     * → map moi Auction sang ProductRow de hien thi
     *
     * Ket noi Nguoi 2 (DAO):
     *   List<Item> items = ItemDAO.getBySeller(currentSellerId);
     */
    private void loadProducts() {
        // TODO: thay bang goi server thuc te (xem comment tren)
        allProducts.setAll(
            new ProductRow("ITM001", "iPhone 15 Pro",    "Electronics", "15,000,000", "17,500,000", "RUNNING"),
            new ProductRow("ITM002", "Tranh Sơn Dầu",    "Art",          "8,000,000",  "8,500,000", "RUNNING"),
            new ProductRow("ITM003", "Toyota Camry 2020","Vehicle",    "600,000,000","600,000,000", "OPEN"),
            new ProductRow("ITM004", "Samsung TV 65\"",  "Electronics", "20,000,000", "22,000,000", "FINISHED"),
            new ProductRow("ITM005", "Xe Máy Honda Wave","Vehicle",     "25,000,000", "28,000,000", "OPEN")
        );
        updateProductLabels();
    }

    /**
     * Load danh sach don hang da ket thuc dau gia.
     *
     * Ket noi Nguoi 3: nhan MessageType.AUCTION_CONCLUDED
     * payload: BidTransaction object cua Nguoi 1
     * → them vao allOrders va cap nhat bang
     */
    private void loadOrders() {
        // TODO: thay bang goi server thuc te
        allOrders.setAll(
            new OrderRow("ORD001", "Samsung TV 65\"",   "Tran Van B", "22,000,000 d", "14/04 20:00", "Chờ xác nhận"),
            new OrderRow("ORD002", "Xe Máy Honda Wave", "Le Thi C",   "28,000,000 d", "12/04 15:00", "Đã xác nhận")
        );
    }

    /** Cap nhat stat cards tren pane Tong quan */
    private void loadOverviewStats() {
        long active   = allProducts.stream().filter(p -> p.status.equals("RUNNING")).count();
        long finished = allProducts.stream().filter(p -> p.status.equals("FINISHED")).count();

        lblStatSales       .setText(String.valueOf(allProducts.size()));
        lblStatSalesDelta  .setText("↑ 12%");
        lblStatOrders      .setText(String.valueOf(allOrders.size()));
        lblStatOrdersDelta .setText("↑ 8%");
        lblStatRevenue     .setText("55,500,000");
        lblStatRevenueDelta.setText("↑ 5%");

        lblOrdersPending.setText("2");
        lblOrdersPickup .setText("1");
        lblOrdersDone   .setText(String.valueOf(finished));
    }

    // ════════════════════════════════════════════════════════
    // BIEU DO DOANH THU
    // ════════════════════════════════════════════════════════

    /** Doanh thu theo tung ngay trong tuan */
    private void loadWeeklyRevenue() {
        revenueSeries.getData().clear();
        revenueSeries.getData().addAll(
            new XYChart.Data<>("T2", 1_500_000),
            new XYChart.Data<>("T3", 3_200_000),
            new XYChart.Data<>("T4", 2_800_000),
            new XYChart.Data<>("T5", 4_100_000),
            new XYChart.Data<>("T6", 5_500_000),
            new XYChart.Data<>("T7", 6_200_000),
            new XYChart.Data<>("CN", 4_800_000)
        );
    }

    /** Doanh thu theo tung thang trong nam */
    private void loadMonthlyRevenue() {
        revenueSeries.getData().clear();
        revenueSeries.getData().addAll(
            new XYChart.Data<>("Tháng 1",  12_000_000),
            new XYChart.Data<>("Tháng 2",  18_500_000),
            new XYChart.Data<>("Tháng 3",  14_200_000),
            new XYChart.Data<>("Tháng 4",  22_000_000),
            new XYChart.Data<>("Tháng 5",  19_800_000),
            new XYChart.Data<>("Tháng 6",  25_500_000)
        );
    }

    /**
     * Nguoi 3 goi phuong thuc nay khi nhan duoc tin hieu AUCTION_CONCLUDED
     * de cap nhat chart doanh thu realtime.
     *
     * @param label  nhan thoi gian (vi du: "T5", "Thang 4")
     * @param amount doanh thu cua phien vua ket thuc
     */
    public void onRevenueUpdate(String label, double amount) {
        Platform.runLater(() ->
            revenueSeries.getData().add(new XYChart.Data<>(label, amount))
        );
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — DIEU HUONG SIDEBAR
    // ════════════════════════════════════════════════════════

    @FXML
    private void handleSellerNav(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        if      (clicked == btnNavOverview)  showPane(paneOverview,  btnNavOverview);
        else if (clicked == btnNavOrders)    showPane(paneOrders,    btnNavOrders);
        else if (clicked == btnNavProducts)  { showPane(paneProducts, btnNavProducts); resetForm(); }
        else if (clicked == btnNavStats)     showPane(paneOverview,  btnNavStats);   // TODO: pane stats rieng
        else if (clicked == btnNavShop)      showPane(paneOverview,  btnNavShop);    // TODO: pane shop settings
    }

    /** An tat ca pane, hien pane duoc chon, cap nhat style nav button */
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

    // ════════════════════════════════════════════════════════
    // HANDLERS — BIEU DO
    // ════════════════════════════════════════════════════════

    @FXML
    private void handleChartPeriod(ActionEvent event) {
        Button src = (Button) event.getSource();
        String activeStyle   = "-fx-background-color: #2C3E50; -fx-text-fill: white;" +
                               "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 12;";
        String inactiveStyle = "-fx-background-color: #F0F3F7; -fx-text-fill: #5D6D7E;" +
                               "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 12;";

        if (src == btnChartWeek) {
            btnChartWeek .setStyle(activeStyle);
            btnChartMonth.setStyle(inactiveStyle);
            loadWeeklyRevenue();
        } else {
            btnChartMonth.setStyle(activeStyle);
            btnChartWeek .setStyle(inactiveStyle);
            loadMonthlyRevenue();
        }
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — FORM SAN PHAM
    // ════════════════════════════════════════════════════════

    /** Nut "+ Tao danh sach moi" tren sidebar */
    @FXML
    private void handleNew() {
        showPane(paneProducts, btnNavProducts);
        resetForm();
    }

    /** Chuyen form sang che do sua: dien san co */
    private void openEditForm(ProductRow row) {
        editingProduct = row;
        lblFormTitle.setText("Chỉnh sửa sản phẩm");
        txtName.setText(row.name);
        txtStartingPrice.setText(row.startPrice.replace(",", ""));
        if (cmbCategory != null) cmbCategory.setValue(row.category);
        lblFormError.setText("");
    }

    /** Reset form ve trang thai them moi */
    private void resetForm() {
        editingProduct = null;
        lblFormTitle.setText("Thêm sản phẩm mới");
        txtName.clear();
        txtDescription.clear();
        txtStartingPrice.clear();
        txtBrand.clear(); txtWarranty.clear();
        txtArtist.clear(); txtMedium.clear();
        txtEngineType.clear(); txtModelYear.clear();
        datePicker.setValue(null);
        lblFormError.setText("");
        if (cmbCategory != null) cmbCategory.getSelectionModel().clearSelection();
        switchCategoryFields("Tất cả");
    }

    @FXML
    private void handleCategoryChange() {
        if (cmbCategory != null && cmbCategory.getValue() != null) {
            switchCategoryFields(cmbCategory.getValue());
        }
    }

    /**
     * Luu san pham (them moi hoac cap nhat).
     *
     * Ket noi Nguoi 1 (Models):
     *   Item item = ItemFactory.createItem(category, name, desc, price, ...);
     *   // ItemFactory.java — Nguoi 1 viet, Nguoi 4 goi
     *
     * Ket noi Nguoi 3 (Network):
     *   networkClient.send(new PacketMessage(MessageType.CREATE_AUCTION, item));
     *   // Server phan hoi MessageType.SEND_AUCTION_ID voi payload auctionId
     */
    @FXML
    private void handleSave() {
        // 1. Validate
        if (!validateForm()) return;

        String name     = txtName.getText().trim();
        String category = cmbCategory != null ? cmbCategory.getValue() : "Electronics";
        String desc     = txtDescription.getText().trim();
        double price;
        try {
            price = Double.parseDouble(txtStartingPrice.getText().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            showFormError("Giá khởi điểm phải là số nguyên dương.");
            return;
        }
        LocalDate endDate = datePicker.getValue();

        if (editingProduct == null) {
            // ── CHE DO THEM MOI ──────────────────────────────
            // TODO: Nguoi 1 — Item newItem = ItemFactory.createItem(category, name, desc, price, ...);
            // TODO: Nguoi 3 — networkClient.send(new PacketMessage(MessageType.CREATE_AUCTION, newItem));
            // Khi nhan duoc SEND_AUCTION_ID → them vao allProducts va cap nhat bang

            // Gia lap: them truc tiep vao bang
            String mockId = "ITM" + String.format("%03d", allProducts.size() + 1);
            allProducts.add(new ProductRow(mockId, name, category,
                String.format("%,.0f", price), String.format("%,.0f", price), "OPEN"));
            showFormError(""); // xoa loi
            resetForm();
        } else {
            // ── CHE DO SUA ───────────────────────────────────
            // TODO: Nguoi 3 — networkClient.send(new PacketMessage(MessageType.AUCTION_UPDATE, updatedItem));
            editingProduct.name      = name;
            editingProduct.startPrice= String.format("%,.0f", price);
            tblProducts.refresh();
            resetForm();
        }
        updateProductLabels();
    }

    @FXML
    private void handleCancel() {
        resetForm();
    }

    /** Validate form truoc khi luu */
    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showFormError("Vui lòng nhập tên sản phẩm.");
            return false;
        }
        if (cmbCategory == null || cmbCategory.getValue() == null
                || cmbCategory.getValue().equals("Tất cả")) {
            showFormError("Vui lòng chọn danh mục.");
            return false;
        }
        if (txtStartingPrice.getText().trim().isEmpty()) {
            showFormError("Vui lòng nhập giá khởi điểm.");
            return false;
        }
        if (datePicker.getValue() == null) {
            showFormError("Vui lòng chọn ngày kết thúc.");
            return false;
        }
        if (datePicker.getValue().isBefore(LocalDate.now().plusDays(1))) {
            showFormError("Ngày kết thúc phải sau hôm nay ít nhất 1 ngày.");
            return false;
        }
        return true;
    }

    private void showFormError(String msg) {
        lblFormError.setText(msg);
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — BANG SAN PHAM
    // ════════════════════════════════════════════════════════

    @FXML
    private void handleTableClick(javafx.scene.input.MouseEvent e) {
        // Double-click da xu ly trong setupProductTable() qua setOnMouseClicked
    }

    @FXML
    private void handleSearchProduct() {
        String kw = txtSearchProduct.getText().trim().toLowerCase();
        filteredProducts.setPredicate(row ->
            kw.isEmpty()
            || row.name.toLowerCase().contains(kw)
            || row.id.toLowerCase().contains(kw)
        );
        updateProductLabels();
    }

    /** Cap nhat label dem san pham / dang dau gia / da ket thuc */
    private void updateProductLabels() {
        long active   = allProducts.stream().filter(p -> "RUNNING".equals(p.status)).count();
        long finished = allProducts.stream().filter(p -> "FINISHED".equals(p.status)).count();
        lblTotalProducts  .setText("Tổng: " + allProducts.size() + " sản phẩm");
        lblActiveAuctions .setText("Đang đấu giá: " + active);
        lblFinishedAuctions.setText("Đã kết thúc: " + finished);
        lblShopTotalItems .setText(String.valueOf(allProducts.size()));
        lblShopTotalSold  .setText(String.valueOf(finished));
    }

    /**
     * Huy phien dau gia — tu nut "Huy phien" trong cot Hanh dong.
     *
     * Ket noi Nguoi 3:
     *   networkClient.send(new PacketMessage(MessageType.CANCEL_AUCTION, row.id));
     *   Server phan hoi MessageType.AUCTION_CANCELLED → cap nhat status tren bang
     */
    private void handleCancelAuction(ProductRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn chắc chắn muốn huỷ phiên đấu giá \"" + row.name + "\"?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận huỷ phiên");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: Nguoi 3 — networkClient.send(new PacketMessage(MessageType.CANCEL_AUCTION, row.id));
                row.status = "CANCELED";
                tblProducts.refresh();
                updateProductLabels();
            }
        });
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — BANG DON HANG
    // ════════════════════════════════════════════════════════

    /**
     * Xac nhan nhan hang — cap nhat trang thai don hang.
     *
     * Ket noi Nguoi 3:
     *   networkClient.send(new PacketMessage(MessageType.AUCTION_UPDATE, row.id));
     * Ket noi Nguoi 2 (DAO):
     *   BidTransactionDAO.updateStatus(row.id, "CONFIRMED");
     */
    private void handleConfirmOrder(OrderRow row) {
        // TODO: Nguoi 3 + Nguoi 2 — xem comment tren
        row.status = "Đã xác nhận";
        tblOrders.refresh();
        lblOrdersPending.setText(
            String.valueOf(Integer.parseInt(lblOrdersPending.getText()) - 1));
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — TOP BAR
    // ════════════════════════════════════════════════════════

    @FXML
    private void handleGlobalSearch() {
        String kw = txtGlobalSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) return;
        // Tim kiem tren ca 2 bang
        showPane(paneProducts, btnNavProducts);
        filteredProducts.setPredicate(row ->
            row.name.toLowerCase().contains(kw) || row.id.toLowerCase().contains(kw));
        updateProductLabels();
    }

    @FXML
    private void handleNotify() {
        // TODO: hien popup danh sach thong bao (AUCTION_CONCLUDED, NOTIFY_AUCTION_WINNER...)
        System.out.println("[SellerDashboard] Mo panel thong bao");
    }

    @FXML
    private void handleShopSettings() {
        // TODO: mo form chinh sua thong tin shop
        System.out.println("[SellerDashboard] Mo thiet lap shop");
    }

    /**
     * Dang xuat — quay ve Login.fxml.
     * Ket noi Nguoi 3:
     *   networkClient.send(new PacketMessage(MessageType.DISCONNECT, currentSellerId));
     */
    @FXML
    private void handleLogout() {
        // TODO: Nguoi 3 — gui DISCONNECT truoc khi dong ket noi
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnNavOverview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════
    // API — NHAN DU LIEU TU LOGIN / NETWORK
    // ════════════════════════════════════════════════════════

    /**
     * LoginController goi sau loader.load() de truyen thong tin seller.
     *
     * Vi du:
     *   SellerDashboardController ctrl = loader.getController();
     *   ctrl.setCurrentSeller(seller.getId(), seller.getUsername());
     */
    public void setCurrentSeller(String sellerId, String sellerName) {
        this.currentSellerId   = sellerId;
        this.currentSellerName = sellerName;
        loadSellerInfo();
        loadProducts();
        loadOrders();
        loadOverviewStats();
    }

    /**
     * Nguoi 3 goi phuong thuc nay khi nhan duoc MessageType.SEND_MY_AUCTIONS
     * (phan hoi cua REQUEST_MY_AUCTIONS) de cap nhat bang san pham.
     */
    public void onReceiveMyAuctions(List<ProductRow> products) {
        Platform.runLater(() -> {
            allProducts.setAll(products);
            updateProductLabels();
            loadOverviewStats();
        });
    }

    /**
     * Nguoi 3 goi phuong thuc nay khi nhan duoc MessageType.AUCTION_CONCLUDED
     * de them don hang moi vao bang Don hang va cap nhat doanh thu.
     */
    public void onAuctionConcluded(OrderRow order, double revenue) {
        Platform.runLater(() -> {
            allOrders.add(0, order); // them dau danh sach
            onRevenueUpdate(order.time, revenue);
            loadOverviewStats();
        });
    }
}

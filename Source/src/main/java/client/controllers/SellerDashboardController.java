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
import server.dao.AuctionDAO;
import server.dao.ItemDAO;
import server.dao.UserDAO;
import server.models.auction.Auction;
import server.models.auction.BidTransaction;
import server.models.item.Item;
import server.models.user.Seller;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * SellerDashboardController
 * FXML: SellerDashboard.fxml
 *
 * Quan ly toan bo giao dien Seller Hub:
 * - Sidebar dieu huong 5 muc: Tong quan / Don hang / San pham / Hieu suat /
 * Shop
 * - paneOverview : stat cards, don hang can xu ly, bieu do doanh thu (LineChart
 * truc tiep), ho so shop
 * - paneProducts : form them/sua san pham (dong theo loai), table san pham
 * - paneOrders : table don hang da ket thuc dau gia
 *
 * Ket noi voi cac thanh vien khac:
 * - Nguoi 1 (Models) : ProductRow su dung Item, Electronics, Art, Vehicle
 * ItemFactory.createItem() khi gui len server
 * - Nguoi 2 (DAO) : DAO.getProductsBySeller(), DAO.getOrdersBySeller()
 * - Nguoi 3 (Network) : networkClient.send(PacketMessage) tai cac diem TODO
 * PacketMessage voi MessageType: CREATE_AUCTION, REQUEST_MY_AUCTIONS,
 * SEND_MY_AUCTIONS, CANCEL_AUCTION, AUCTION_CONCLUDED
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
    private Button btnNotify; // Nut chuong goc phai

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
    // FXML — 3 PANE CHINH
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
    // FXML — OVERVIEW: DON HANG CAN XU LY
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblOrdersPending;
    @FXML
    private Label lblOrdersPickup;
    @FXML
    private Label lblOrdersDone;

    // ════════════════════════════════════════════════════════
    // FXML — OVERVIEW: BIEU DO DOANH THU
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
    // FXML — OVERVIEW: HO SO SHOP
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
    // FXML — PRODUCTS: FORM THEM/SUA
    // ════════════════════════════════════════════════════════
    @FXML
    private Label lblFormTitle;
    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> cmbCategory;

    // Nhom field dong theo loai san pham
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
    private DatePicker datePicker;
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

    /** Format tien VND: 17500000 → "17,500,000" */
    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /** Cache toan bo Auction sau khi loadProducts() — dung cho chart va stats */
    private List<Auction> cachedAuctions = List.of();

    /**
     * Danh sach thong bao hien tai — duoc build tu cachedAuctions khi load.
     * Moi phan tu la 1 chuoi mo ta su kien (phien ket thuc, co winner, bi huy...).
     *
     * TODO (khi co server): bo sung them thong bao real-time tu
     * MessageType.AUCTION_CONCLUDED,
     * NOTIFY_AUCTION_WINNER bang cach goi addNotification() tu network handler.
     */
    private final ObservableList<String> notifications = FXCollections.observableArrayList();

    /**
     * Bieu do doanh thu — dung LineChart truc tiep, khong lien quan
     * BidPriceLineChart
     */
    private XYChart.Series<String, Number> revenueSeries;

    private final ObservableList<ProductRow> allProducts = FXCollections.observableArrayList();
    private FilteredList<ProductRow> filteredProducts;

    private final ObservableList<OrderRow> allOrders = FXCollections.observableArrayList();

    /** Dang sua san pham nao (null = che do them moi) */
    private ProductRow editingProduct = null;

    /** Seller hien tai — se duoc set tu LoginController sau khi dang nhap */
    private String currentSellerId = "";
    private String currentSellerName = "";

    // Style cua nav button khi active / inactive
    private static final String NAV_ACTIVE = "-fx-background-color: #EBF5FB; -fx-text-fill: #1A5276;" +
            "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13; -fx-font-weight: bold;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #5D6D7E;" +
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
            this.id = id;
            this.name = name;
            this.category = category;
            this.startPrice = startPrice;
            this.curPrice = curPrice;
            this.status = status;
        }
    }

    /**
     * Dai dien 1 dong trong bang Don hang — map tu BidTransaction.java cua Nguoi 1
     */
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
        // Doc thong tin seller tu SessionManager
        SessionManager session = SessionManager.getInstance();
        currentSellerId = session.getCurrentUser() != null ? session.getCurrentUser().getId() : "";
        currentSellerName = session.getUsername();

        setupNavButtons();
        setupProductTable();
        setupOrderTable();
        setupFormCategoryListener();
        setupRevenueChart();
        loadSellerInfo();
        loadProducts(); // phai chay truoc — cachedAuctions duoc set o day
        loadOrders();
        loadNotifications(); // build tu cachedAuctions
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
        axisRevenue.setForceZeroInRange(true); // Doanh thu bat dau tu 0 — khac bid price
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
                        ".axis-label { -fx-text-fill: #7F8C8D; -fx-font-size: 11; }");

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
        colItemId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colAucStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // To mau cot trang thai
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

        // Cot Hanh dong: nut Sua + Huy phien
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
                    if (row != null)
                        openEditForm(row);
                });
                btnCancel.setOnAction(e -> {
                    ProductRow row = getTableRow().getItem();
                    if (row != null)
                        handleCancelAuction(row);
                });
            }

            @Override
            protected void updateItem(String s, boolean empty) {
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
                if (row != null)
                    openEditForm(row);
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
        colOrdId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colOrdItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().itemName));
        colOrdBuyer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().buyer));
        colOrdPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price));
        colOrdTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time));
        colOrdStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // Cot Hanh dong don hang: nut Xac nhan / Xem
        colOrdAct.setCellFactory(col -> new TableCell<>() {
            private final Button btnConfirm = new Button("Xác nhận");
            {
                btnConfirm.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10; -fx-font-size: 11;");
                btnConfirm.setOnAction(e -> {
                    OrderRow row = getTableRow().getItem();
                    if (row != null)
                        handleConfirmOrder(row);
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
                if (nw == null)
                    return;
                switchCategoryFields(nw);
            });
        }
    }

    /** Hien field phu hop, an 2 nhom con lai */
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
    // LOAD DU LIEU — se thay bang goi server/DAO
    // ════════════════════════════════════════════════════════

    /**
     * Hien thi thong tin Seller len top bar va box ho so shop.
     *
     * Doc tu SessionManager → khong can goi DAO.
     * companyName va rating lay tu Seller object (subclass cua User).
     *
     * LUU Y: lblShopDesc khong co trong Seller model hien tai —
     * neu muon them mo ta shop can bo sung field description vao Seller.java (Nguoi
     * 1).
     *
     * TODO (khi co server): thay bang goi MessageType.REQUEST_USER_PROFILE
     * de lay thong tin moi nhat tu server thay vi chi doc local session.
     */
    private void loadSellerInfo() {
        Seller seller = SessionManager.getInstance().asSeller();

        // Top bar
        btnSellerUser.setText("👤 " + currentSellerName);

        // Box ho so shop
        lblShopName.setText(currentSellerName + "'s Shop");

        if (seller != null) {
            // companyName va rating lay thuc tu Seller object
            lblShopCompany.setText(
                    seller.getCompanyName() != null && !seller.getCompanyName().isEmpty()
                            ? seller.getCompanyName()
                            : "Chưa cập nhật");
            lblShopRating.setText(
                    String.format("⭐ %.1f / 5.0", seller.getRating()));
        } else {
            lblShopCompany.setText("Chưa cập nhật");
            lblShopRating.setText("⭐ -- / 5.0");
        }

        // Mo ta: Seller hien tai chua co field description
        // → de trong cho den khi Nguoi 1 bo sung Seller.getDescription()
        lblShopDesc.setText("Chưa có mô tả.");

        // lblShopTotalItems va lblShopTotalSold se duoc cap nhat boi
        // updateProductLabels()
        // sau khi loadProducts() chay xong
    }

    /**
     * Load danh sach phien dau gia cua Seller nay tu AuctionDAO + ItemDAO.
     *
     * Logic:
     * - Lay toan bo Auction tu AuctionDAO.loadAll()
     * - Loc nhung phien co sellerId == currentSellerId
     * - Voi moi phien, lay ten vat pham tu ItemDAO.timTheoId(auction.getItemId())
     * - Map sang ProductRow de hien thi trong TableView
     * - Cache lai vao cachedAuctions de chart va stats dung lai
     *
     * TODO (khi co server): thay bang goi MessageType.REQUEST_MY_AUCTIONS
     * de lay danh sach thuc te tu server thay vi doc file JSON truc tiep.
     */
    private void loadProducts() {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            ItemDAO itemDAO = new ItemDAO();

            // Lay tat ca phien, loc theo sellerId
            List<Auction> myAuctions = auctionDAO.loadAll().stream()
                    .filter(a -> currentSellerId.equals(a.getSellerId()))
                    .toList();

            // Cache lai de chart va stats dung
            cachedAuctions = myAuctions;

            // Map sang ProductRow
            List<ProductRow> rows = myAuctions.stream().map(a -> {
                Item item = null;
                try {
                    item = itemDAO.timTheoId(a.getItemId());
                } catch (IOException ignored) {
                }
                String itemName = (item != null) ? item.getName() : "Vật phẩm #" + a.getItemId();
                String category = (item != null) ? a.getItemId() : "—";
                // Lay category tu Item neu co
                if (item != null) {
                    try {
                        category = item.getCategory().name();
                    } catch (Exception ignored) {
                    }
                }
                String startPrice = VND.format((long) a.getStartingPrice());
                String curPrice = VND.format((long) a.getCurrentHighestBid());
                String status = a.getStatus().name();
                return new ProductRow(a.getId(), itemName, category,
                        startPrice, curPrice, status);
            }).toList();

            allProducts.setAll(rows);

        } catch (IOException e) {
            System.err.println("[SellerDashboard] Loi load products: " + e.getMessage());
            // Neu doc file that bai, de trong — khong hien du lieu gia
            allProducts.clear();
        }

        updateProductLabels();
    }

    /**
     * Load danh sach don hang da ket thuc tu AuctionDAO.
     *
     * Logic:
     * - Dung cachedAuctions (da load trong loadProducts()) — khong query lai DAO
     * - Loc phien co status FINISHED hoac PAID
     * - Moi phien co winner (highestBidderId != null) → tao 1 OrderRow
     * - Ten nguoi mua lay tu UserDAO.loadAll() theo highestBidderId
     * - Gia ban = currentHighestBid cua phien do
     *
     * TODO (khi co server): thay bang lang nghe MessageType.AUCTION_CONCLUDED
     * de cap nhat real-time thay vi chi doc khi khoi dong.
     */
    private void loadOrders() {
        allOrders.clear();
        if (cachedAuctions.isEmpty())
            return;

        try {
            // Load UserDAO 1 lan de tra cuu ten nguoi mua
            List<server.models.user.User> allUsers = new UserDAO().loadAll();
            ItemDAO itemDAO = new ItemDAO();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (Auction a : cachedAuctions) {
                String statusName = a.getStatus().name();
                if (!statusName.equals("FINISHED") && !statusName.equals("PAID"))
                    continue;
                if (a.getHighestBidderId() == null || a.getHighestBidderId().isEmpty())
                    continue;

                // Ten nguoi mua
                String buyerName = allUsers.stream()
                        .filter(u -> u.getId().equals(a.getHighestBidderId()))
                        .map(server.models.user.User::getUsername)
                        .findFirst().orElse("Bidder #" + a.getHighestBidderId());

                // Ten vat pham
                Item item = null;
                try {
                    item = itemDAO.timTheoId(a.getItemId());
                } catch (IOException ignored) {
                }
                String itemName = (item != null) ? item.getName() : "Vật phẩm #" + a.getItemId();

                // Thoi gian ket thuc
                String timeStr = a.getEndTime() != null ? a.getEndTime().format(fmt) : "--";

                // Gia cuoi cung
                String price = VND.format((long) a.getCurrentHighestBid()) + " đ";

                allOrders.add(new OrderRow(
                        a.getId(), itemName, buyerName, price, timeStr, "Chờ xác nhận"));
            }

        } catch (IOException e) {
            System.err.println("[SellerDashboard] Loi load orders: " + e.getMessage());
        }
    }

    /**
     * Cap nhat stat cards tren pane Tong quan tu data thuc.
     *
     * Tinh toan tu allProducts va allOrders vua load xong:
     * - Tong phien dau gia = allProducts.size()
     * - Dang dau gia (RUNNING) = dem tu allProducts
     * - Tong don hang = allOrders.size()
     * - Tong doanh thu = tong currentHighestBid cua tat ca phien FINISHED/PAID
     *
     * LUU Y: lblStatSalesDelta, lblStatOrdersDelta, lblStatRevenueDelta
     * hien tai de "—" vi khong co du lieu lich su de so sanh.
     * Khi co server co the tinh % thay doi so voi tuan/thang truoc.
     */
    private void loadOverviewStats() {
        long active = allProducts.stream().filter(p -> "RUNNING".equals(p.status)).count();
        long finished = allProducts.stream().filter(p -> "FINISHED".equals(p.status)).count();
        long pending = allOrders.stream()
                .filter(o -> "Chờ xác nhận".equals(o.status)).count();
        long confirmed = allOrders.stream()
                .filter(o -> "Đã xác nhận".equals(o.status)).count();

        // Tong doanh thu = tong gia cao nhat cua cac phien da ket thuc
        long totalRevenue = cachedAuctions.stream()
                .filter(a -> {
                    String s = a.getStatus().name();
                    return s.equals("FINISHED") || s.equals("PAID");
                })
                .mapToLong(a -> (long) a.getCurrentHighestBid())
                .sum();

        lblStatSales.setText(String.valueOf(allProducts.size()));
        lblStatSalesDelta.setText("—"); // chua co du lieu so sanh lich su
        lblStatOrders.setText(String.valueOf(allOrders.size()));
        lblStatOrdersDelta.setText("—");
        lblStatRevenue.setText(VND.format(totalRevenue));
        lblStatRevenueDelta.setText("—");

        lblOrdersPending.setText(String.valueOf(pending));
        lblOrdersPickup.setText(String.valueOf(active)); // dang dau gia = can theo doi
        lblOrdersDone.setText(String.valueOf(confirmed));
    }

    // ════════════════════════════════════════════════════════
    // BIEU DO DOANH THU
    // ════════════════════════════════════════════════════════

    /**
     * Ve bieu do doanh thu 7 ngay gan nhat tu cachedAuctions.
     *
     * Logic:
     * - Loc phien FINISHED/PAID co endTime trong 7 ngay qua
     * - Nhom theo ngay trong tuan (T2→CN), cong doanh thu tung ngay
     * - Neu ngay nao khong co phien → hien 0
     *
     * LUU Y: neu cachedAuctions trong (seller chua co phien nao) thi chart se trang
     * —
     * day la dung, khong phai loi.
     */
    private void loadWeeklyRevenue() {
        revenueSeries.getData().clear();

        LocalDate today = LocalDate.now();
        // Xac dinh dau tuan (T2)
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        // Khoi tao map nhan → doanh thu cho 7 ngay
        String[] labels = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
        Map<String, Long> revenueMap = new TreeMap<>();
        for (String label : labels)
            revenueMap.put(label, 0L);

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");

        for (Auction a : cachedAuctions) {
            String s = a.getStatus().name();
            if ((!s.equals("FINISHED") && !s.equals("PAID")) || a.getEndTime() == null)
                continue;
            LocalDate endDate = a.getEndTime().toLocalDate();
            // Kiem tra co trong tuan nay khong
            if (endDate.isBefore(monday) || endDate.isAfter(monday.plusDays(6)))
                continue;

            // Map thu trong tuan sang nhan
            String label = switch (endDate.getDayOfWeek()) {
                case MONDAY -> "T2";
                case TUESDAY -> "T3";
                case WEDNESDAY -> "T4";
                case THURSDAY -> "T5";
                case FRIDAY -> "T6";
                case SATURDAY -> "T7";
                case SUNDAY -> "CN";
            };
            revenueMap.merge(label, (long) a.getCurrentHighestBid(), Long::sum);
        }

        // Them vao chart theo thu tu T2→CN
        for (String label : labels) {
            revenueSeries.getData().add(new XYChart.Data<>(label, revenueMap.get(label)));
        }
    }

    /**
     * Ve bieu do doanh thu 6 thang gan nhat tu cachedAuctions.
     *
     * Logic tuong tu loadWeeklyRevenue nhung nhom theo thang.
     * Hien thi 6 thang gan nhat tinh tu thang hien tai.
     */
    private void loadMonthlyRevenue() {
        revenueSeries.getData().clear();

        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("'Tháng' M");

        // Tao danh sach 6 thang (cu nhat → moi nhat)
        Map<String, Long> revenueMap = new java.util.LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            revenueMap.put(month.format(monthFmt), 0L);
        }

        for (Auction a : cachedAuctions) {
            String s = a.getStatus().name();
            if ((!s.equals("FINISHED") && !s.equals("PAID")) || a.getEndTime() == null)
                continue;
            String label = a.getEndTime().toLocalDate().format(monthFmt);
            if (!revenueMap.containsKey(label))
                continue; // ngoai khoang 6 thang
            revenueMap.merge(label, (long) a.getCurrentHighestBid(), Long::sum);
        }

        revenueMap.forEach((label, revenue) -> revenueSeries.getData().add(new XYChart.Data<>(label, revenue)));
    }

    /**
     * Nguoi 3 goi phuong thuc nay khi nhan duoc tin hieu AUCTION_CONCLUDED
     * de cap nhat chart doanh thu realtime.
     *
     * @param label  nhan thoi gian (vi du: "T5", "Thang 4")
     * @param amount doanh thu cua phien vua ket thuc
     */
    public void onRevenueUpdate(String label, double amount) {
        Platform.runLater(() -> revenueSeries.getData().add(new XYChart.Data<>(label, amount)));
    }

    // ════════════════════════════════════════════════════════
    // HANDLERS — DIEU HUONG SIDEBAR
    // ════════════════════════════════════════════════════════

    @FXML
    private void handleSellerNav(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        if (clicked == btnNavOverview)
            showPane(paneOverview, btnNavOverview);
        else if (clicked == btnNavOrders)
            showPane(paneOrders, btnNavOrders);
        else if (clicked == btnNavProducts) {
            showPane(paneProducts, btnNavProducts);
            resetForm();
        } else if (clicked == btnNavStats)
            showPane(paneOverview, btnNavStats); // TODO: pane stats rieng
        else if (clicked == btnNavShop)
            showPane(paneOverview, btnNavShop); // TODO: pane shop settings
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
        if (cmbCategory != null)
            cmbCategory.setValue(row.category);
        lblFormError.setText("");
    }

    /** Reset form ve trang thai them moi */
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
        datePicker.setValue(null);
        lblFormError.setText("");
        if (cmbCategory != null)
            cmbCategory.getSelectionModel().clearSelection();
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
     * Item item = ItemFactory.createItem(category, name, desc, price, ...);
     * // ItemFactory.java — Nguoi 1 viet, Nguoi 4 goi
     *
     * Ket noi Nguoi 3 (Network):
     * networkClient.send(new PacketMessage(MessageType.CREATE_AUCTION, item));
     * // Server phan hoi MessageType.SEND_AUCTION_ID voi payload auctionId
     */
    @FXML
    private void handleSave() {
        // 1. Validate
        if (!validateForm())
            return;

        String name = txtName.getText().trim();
        String category = cmbCategory != null ? cmbCategory.getValue() : "Electronics";
        String desc = txtDescription.getText().trim();
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
            // TODO: Nguoi 1 — Item newItem = ItemFactory.createItem(category, name, desc,
            // price, ...);
            // TODO: Nguoi 3 — networkClient.send(new
            // PacketMessage(MessageType.CREATE_AUCTION, newItem));
            // Khi nhan duoc SEND_AUCTION_ID → them vao allProducts va cap nhat bang

            // Gia lap: them truc tiep vao bang
            String mockId = "ITM" + String.format("%03d", allProducts.size() + 1);
            allProducts.add(new ProductRow(mockId, name, category,
                    String.format("%,.0f", price), String.format("%,.0f", price), "OPEN"));
            showFormError(""); // xoa loi
            resetForm();
        } else {
            // ── CHE DO SUA ───────────────────────────────────
            // TODO: Nguoi 3 — networkClient.send(new
            // PacketMessage(MessageType.AUCTION_UPDATE, updatedItem));
            editingProduct.name = name;
            editingProduct.startPrice = String.format("%,.0f", price);
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
        filteredProducts.setPredicate(row -> kw.isEmpty()
                || row.name.toLowerCase().contains(kw)
                || row.id.toLowerCase().contains(kw));
        updateProductLabels();
    }

    /** Cap nhat label dem san pham / dang dau gia / da ket thuc */
    private void updateProductLabels() {
        long active = allProducts.stream().filter(p -> "RUNNING".equals(p.status)).count();
        long finished = allProducts.stream().filter(p -> "FINISHED".equals(p.status)).count();
        lblTotalProducts.setText("Tổng: " + allProducts.size() + " sản phẩm");
        lblActiveAuctions.setText("Đang đấu giá: " + active);
        lblFinishedAuctions.setText("Đã kết thúc: " + finished);
        lblShopTotalItems.setText(String.valueOf(allProducts.size()));
        lblShopTotalSold.setText(String.valueOf(finished));
    }

    /**
     * Huy phien dau gia — tu nut "Huy phien" trong cot Hanh dong.
     *
     * Ket noi Nguoi 3:
     * networkClient.send(new PacketMessage(MessageType.CANCEL_AUCTION, row.id));
     * Server phan hoi MessageType.AUCTION_CANCELLED → cap nhat status tren bang
     */
    private void handleCancelAuction(ProductRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn chắc chắn muốn huỷ phiên đấu giá \"" + row.name + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận huỷ phiên");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: Nguoi 3 — networkClient.send(new
                // PacketMessage(MessageType.CANCEL_AUCTION, row.id));
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
     * networkClient.send(new PacketMessage(MessageType.AUCTION_UPDATE, row.id));
     * Ket noi Nguoi 2 (DAO):
     * BidTransactionDAO.updateStatus(row.id, "CONFIRMED");
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
        if (kw.isEmpty())
            return;
        // Tim kiem tren ca 2 bang
        showPane(paneProducts, btnNavProducts);
        filteredProducts.setPredicate(row -> row.name.toLowerCase().contains(kw) || row.id.toLowerCase().contains(kw));
        updateProductLabels();
    }

    @FXML
    private void handleNotify() {
        // Tao popup hien danh sach thong bao phia duoi nut chuong
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true); // tu dong dong khi click ra ngoai

        // ── Tieu de ────────────────────────────────────────
        Label title = new Label("🔔 Thông báo (" + notifications.size() + ")");
        title.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2C3E50;" +
                        "-fx-padding: 10 14 8 14;");

        // ── Danh sach thong bao ────────────────────────────
        ListView<String> listView = new ListView<>(notifications);
        listView.setPrefWidth(340);
        listView.setPrefHeight(Math.min(notifications.size() * 52 + 10, 260));
        listView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Custom cell: wrap text, co padding, co duong ke giua cac dong
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
                            "-fx-padding: 8 12; -fx-font-size: 12;" +
                                    "-fx-border-color: transparent transparent #F0F3F7 transparent;");
                }
            }
        });

        // Placeholder khi chua co thong bao
        if (notifications.isEmpty()) {
            Label empty = new Label("Không có thông báo mới.");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 12; -fx-padding: 16;");
            listView.setPlaceholder(empty);
            listView.setPrefHeight(60);
        }

        // ── Container tong the ─────────────────────────────
        VBox container = new VBox(title, listView);
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #E8ECF0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);");

        popup.getContent().add(container);

        // Dinh vi popup phia duoi nut chuong, can phai
        double x = btnNotify.localToScreen(0, 0).getX()
                + btnNotify.getWidth() - 340;
        double y = btnNotify.localToScreen(0, 0).getY()
                + btnNotify.getHeight() + 6;
        popup.show(btnNotify.getScene().getWindow(), x, y);
    }

    /**
     * Xay dung danh sach thong bao tu cachedAuctions.
     *
     * Cac loai thong bao hien tai:
     * - Phien FINISHED co winner → "✅ [ten sp]: [nguoi mua] thang voi [gia]"
     * - Phien FINISHED khong co winner → "❌ [ten sp]: Phien ket thuc khong co nguoi
     * thang"
     * - Phien CANCELED → "🚫 [ten sp]: Phien da bi huy"
     *
     * TODO (khi co server): goi addNotification() khi nhan
     * MessageType.AUCTION_CONCLUDED
     * hoac NOTIFY_AUCTION_WINNER de them thong bao real-time vao dau danh sach.
     */
    private void loadNotifications() {
        notifications.clear();
        if (cachedAuctions.isEmpty())
            return;

        try {
            ItemDAO itemDAO = new ItemDAO();
            List<server.models.user.User> allUsers = new UserDAO().loadAll();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (Auction a : cachedAuctions) {
                String statusName = a.getStatus().name();

                // Lay ten vat pham
                Item item = null;
                try {
                    item = itemDAO.timTheoId(a.getItemId());
                } catch (IOException ignored) {
                }
                String itemName = (item != null) ? item.getName() : "#" + a.getItemId();
                String timeStr = a.getEndTime() != null ? a.getEndTime().format(fmt) : "--";

                switch (statusName) {
                    case "FINISHED", "PAID" -> {
                        if (a.getHighestBidderId() != null && !a.getHighestBidderId().isEmpty()) {
                            // Co nguoi thang
                            String winner = allUsers.stream()
                                    .filter(u -> u.getId().equals(a.getHighestBidderId()))
                                    .map(server.models.user.User::getUsername)
                                    .findFirst()
                                    .orElse("Bidder #" + a.getHighestBidderId());
                            notifications.add(
                                    "✅ " + itemName + "\n"
                                            + winner + " thắng với "
                                            + VND.format((long) a.getCurrentHighestBid()) + " đ"
                                            + " (" + timeStr + ")");
                        } else {
                            // Ket thuc khong co ai dat gia
                            notifications.add(
                                    "❌ " + itemName + "\n"
                                            + "Phiên kết thúc không có người thắng (" + timeStr + ")");
                        }
                    }
                    case "CANCELED" ->
                        notifications.add("🚫 " + itemName + "\nPhiên đã bị huỷ (" + timeStr + ")");
                    // OPEN va RUNNING khong tao thong bao — dang dien ra binh thuong
                }
            }
        } catch (IOException e) {
            System.err.println("[SellerDashboard] Loi load notifications: " + e.getMessage());
        }

        // Cap nhat badge so thong bao tren nut chuong
        int count = notifications.size();
        btnNotify.setText(count > 0 ? "🔔 " + count : "🔔");
    }

    /**
     * Them 1 thong bao moi vao dau danh sach va cap nhat badge.
     * Nguoi 3 goi ham nay khi nhan MessageType.AUCTION_CONCLUDED /
     * NOTIFY_AUCTION_WINNER.
     *
     * @param message noi dung thong bao, vi du: "✅ iPhone 15: Nguyen A thang
     *                17,500,000 đ"
     */
    public void addNotification(String message) {
        javafx.application.Platform.runLater(() -> {
            notifications.add(0, message); // them vao dau
            btnNotify.setText("🔔 " + notifications.size());
        });
    }

    @FXML
    private void handleShopSettings() {
        // TODO: mo form chinh sua thong tin shop
        System.out.println("[SellerDashboard] Mo thiet lap shop");
    }

    /**
     * Dang xuat — quay ve Login.fxml.
     * Ket noi Nguoi 3:
     * networkClient.send(new PacketMessage(MessageType.DISCONNECT,
     * currentSellerId));
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
     * SellerDashboardController ctrl = loader.getController();
     * ctrl.setCurrentSeller(seller.getId(), seller.getUsername());
     */
    /**
     * @deprecated SessionManager da xu ly viec nay tu initialize().
     *             Giu lai de khong break code cu neu co noi goi den.
     */
    @Deprecated
    public void setCurrentSeller(String sellerId, String sellerName) {
        // Khong can thiet nua — du lieu lay tu SessionManager trong initialize()
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
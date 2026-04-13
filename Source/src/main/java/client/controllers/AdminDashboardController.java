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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AdminDashboardController
 * FXML: AdminDashboard.fxml
 *
 * Quan ly toan he thong:
 *   - Sidebar navigation: Overview / Users / Auctions / Items / Bids
 *   - Overview: stat cards + recent activity
 *   - Users: table + tim kiem + loc theo vai tro + khoa/mo tai khoan
 *   - Auctions: table + loc trang thai + huy phien
 *   - Items: table
 *   - Bids: table lich su dat gia
 */
public class AdminDashboardController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────────
    @FXML private Label  lblAdminName;
    @FXML private Label  lblServerStatus;
    @FXML private Label  lblConnections;
    @FXML private Button btnLogout;
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavAuctions;
    @FXML private Button btnNavItems;
    @FXML private Button btnNavBids;

    // ── Panes ─────────────────────────────────────────────────
    @FXML private VBox paneOverview;
    @FXML private VBox paneUsers;
    @FXML private VBox paneAuctions;
    @FXML private VBox paneItems;
    @FXML private VBox paneBids;

    // ── Overview ──────────────────────────────────────────────
    @FXML private Label lblStatUsers;
    @FXML private Label lblStatAuctions;
    @FXML private Label lblStatItems;
    @FXML private Label lblStatBids;
    @FXML private ListView<String> listRecentActivity;

    // ── Users ─────────────────────────────────────────────────
    @FXML private TextField            txtSearchUser;
    @FXML private ComboBox<String>     cmbFilterRole;
    @FXML private TableView<UserRow>           tblUsers;
    @FXML private TableColumn<UserRow, String> colUserId;
    @FXML private TableColumn<UserRow, String> colUName;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colUserAct;

    // ── Auctions ──────────────────────────────────────────────
    @FXML private ComboBox<String>       cmbFilterStatus;
    @FXML private TableView<AucRow>            tblAuctions;
    @FXML private TableColumn<AucRow, String>  colAucId;
    @FXML private TableColumn<AucRow, String>  colAucItem;
    @FXML private TableColumn<AucRow, String>  colAucSeller;
    @FXML private TableColumn<AucRow, String>  colAucCurBid;
    @FXML private TableColumn<AucRow, String>  colAucEnd;
    @FXML private TableColumn<AucRow, String>  colAucStatus;
    @FXML private TableColumn<AucRow, String>  colAucActions;

    // ── Items ─────────────────────────────────────────────────
    @FXML private TableView<ItemRow>           tblItems;
    @FXML private TableColumn<ItemRow, String> colItId;
    @FXML private TableColumn<ItemRow, String> colItName;
    @FXML private TableColumn<ItemRow, String> colItCat;
    @FXML private TableColumn<ItemRow, String> colItSeller;
    @FXML private TableColumn<ItemRow, String> colItPrice;
    @FXML private TableColumn<ItemRow, String> colItActions;

    // ── Bids ──────────────────────────────────────────────────
    @FXML private TableView<BidRow>            tblBids;
    @FXML private TableColumn<BidRow, String>  colBidId;
    @FXML private TableColumn<BidRow, String>  colBidder;
    @FXML private TableColumn<BidRow, String>  colBidAuction;
    @FXML private TableColumn<BidRow, String>  colBidAmount;
    @FXML private TableColumn<BidRow, String>  colBidTime;
    @FXML private TableColumn<BidRow, String>  colBidValid;

    // ── Data ──────────────────────────────────────────────────
    private ObservableList<UserRow> userData = FXCollections.observableArrayList();
    private ObservableList<AucRow>  aucData  = FXCollections.observableArrayList();
    private ObservableList<ItemRow> itemData = FXCollections.observableArrayList();
    private ObservableList<BidRow>  bidData  = FXCollections.observableArrayList();

    private FilteredList<UserRow> filteredUsers;
    private FilteredList<AucRow>  filteredAucs;

    // Style cho nav button active / inactive
    private static final String NAV_ACTIVE =
        "-fx-background-color: #2D2D2D; -fx-text-fill: #F5F5F5;" +
        "-fx-alignment: CENTER_LEFT; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";
    private static final String NAV_INACTIVE =
        "-fx-background-color: transparent; -fx-text-fill: #BDBDBD;" +
        "-fx-alignment: CENTER_LEFT; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-padding: 10 14; -fx-font-size: 13;";

    private Button currentNavBtn;

    // ═════════════════════════════════════════════════════════
    // INNER DTOs
    // ═════════════════════════════════════════════════════════
    public record UserRow(String id, String username, String email, String role) {}
    public record AucRow (String id, String item,     String seller,
                          String curBid, String endTime, String status) {}
    public record ItemRow(String id, String name,     String cat,
                          String seller, String price) {}
    public record BidRow (String id, String bidder,   String auction,
                          String amount, String time, String valid) {}

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblAdminName.setText("Admin");
        lblServerStatus.setText("● Server: Online");
        lblConnections.setText("Ket noi: 3 clients");

        // Comboboxes
        cmbFilterRole.setItems(FXCollections.observableArrayList(
            "Tat ca", "Bidder", "Seller", "Admin"));
        cmbFilterRole.getSelectionModel().selectFirst();

        cmbFilterStatus.setItems(FXCollections.observableArrayList(
            "Tat ca", "OPEN", "RUNNING", "FINISHED", "CANCELED"));
        cmbFilterStatus.getSelectionModel().selectFirst();

        bindAllColumns();
        loadAllData();
        setupFilters();

        // Hien thi pane Overview mac dinh
        showPane(paneOverview, btnNavOverview);
    }

    // ═════════════════════════════════════════════════════════
    // BIND COLUMNS
    // ═════════════════════════════════════════════════════════
    private void bindAllColumns() {
        // Users
        colUserId .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colUName  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username()));
        colEmail  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().email()));
        colRole   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().role()));
        colUserAct.setCellFactory(col -> buildActionCell_Users());

        // Auctions
        colAucId     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colAucItem   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().item()));
        colAucSeller .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().seller()));
        colAucCurBid .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curBid()));
        colAucEnd    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().endTime()));
        colAucStatus .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colAucActions.setCellFactory(col -> buildActionCell_Auctions());

        // Items
        colItId    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colItName  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colItCat   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().cat()));
        colItSeller.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().seller()));
        colItPrice .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().price()));
        colItActions.setCellFactory(col -> buildActionCell_Items());

        // Bids
        colBidId     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id()));
        colBidder    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().bidder()));
        colBidAuction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().auction()));
        colBidAmount .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().amount()));
        colBidTime   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time()));
        colBidValid  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().valid()));
    }

    // ── Action cells ─────────────────────────────────────────

    private TableCell<UserRow, String> buildActionCell_Users() {
        return new TableCell<>() {
            private final Button btnLock = new Button("Khoa");
            {
                btnLock.setStyle(
                    "-fx-background-color: #E65100; -fx-text-fill: white;" +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnLock.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    showConfirm("Khoa tai khoan", "Khoa user: " + row.username() + "?",
                        () -> { /* TODO: dao.lockUser(row.id()) */ logActivity("Khoa user: " + row.username()); });
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnLock);
            }
        };
    }

    private TableCell<AucRow, String> buildActionCell_Auctions() {
        return new TableCell<>() {
            private final Button btnCancel = new Button("Huy phien");
            {
                btnCancel.setStyle(
                    "-fx-background-color: #C62828; -fx-text-fill: white;" +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnCancel.setOnAction(e -> {
                    AucRow row = getTableView().getItems().get(getIndex());
                    showConfirm("Huy phien dau gia", "Huy phien: " + row.item() + "?",
                        () -> { /* TODO: server.cancelAuction(row.id()) */ logActivity("Huy phien: " + row.item()); });
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnCancel);
            }
        };
    }

    private TableCell<ItemRow, String> buildActionCell_Items() {
        return new TableCell<>() {
            private final Button btnDel = new Button("Xoa");
            {
                btnDel.setStyle(
                    "-fx-background-color: #C62828; -fx-text-fill: white;" +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnDel.setOnAction(e -> {
                    ItemRow row = getTableView().getItems().get(getIndex());
                    showConfirm("Xoa san pham", "Xoa: " + row.name() + "?",
                        () -> { itemData.remove(row); logActivity("Xoa item: " + row.name()); });
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDel);
            }
        };
    }

    // ═════════════════════════════════════════════════════════
    // LOAD DU LIEU (dummy — thay bang dao/server)
    // ═════════════════════════════════════════════════════════
    private void loadAllData() {
        userData.setAll(
            new UserRow("U001","nguyen_van_a","a@email.com","Bidder"),
            new UserRow("U002","tran_thi_b",  "b@email.com","Bidder"),
            new UserRow("U003","techshop_vn", "shop@email.com","Seller"),
            new UserRow("U004","admin_01",    "admin@email.com","Admin")
        );
        aucData.setAll(
            new AucRow("AUC001","iPhone 15 Pro","techshop_vn","17,500,000 d","15/04 18:00","RUNNING"),
            new AucRow("AUC002","Tranh Son Dau","art_gallery", "8,500,000 d","16/04 10:00","RUNNING"),
            new AucRow("AUC003","Toyota Camry", "car_world", "650,000,000 d","17/04 09:00","OPEN"),
            new AucRow("AUC004","Samsung TV 65","techshop_vn","22,000,000 d","14/04 20:00","FINISHED")
        );
        itemData.setAll(
            new ItemRow("ITM001","iPhone 15 Pro","Electronics","techshop_vn","15,000,000 d"),
            new ItemRow("ITM002","Tranh Son Dau","Art",        "art_gallery", "8,000,000 d"),
            new ItemRow("ITM003","Toyota Camry", "Vehicle",   "car_world",  "600,000,000 d")
        );
        bidData.setAll(
            new BidRow("BID001","nguyen_van_a","AUC001","17,500,000 d","17:50:05","Hop le"),
            new BidRow("BID002","tran_thi_b",  "AUC001","17,000,000 d","17:48:00","Hop le"),
            new BidRow("BID003","nguyen_van_a","AUC002", "8,500,000 d","09:30:00","Hop le")
        );

        // Overview stats
        lblStatUsers   .setText(String.valueOf(userData.size()));
        lblStatAuctions.setText(String.valueOf(aucData.size()));
        lblStatItems   .setText(String.valueOf(itemData.size()));
        lblStatBids    .setText(String.valueOf(bidData.size()));

        // Recent activity
        listRecentActivity.getItems().setAll(
            "17:50:05  nguyen_van_a dat gia 17,500,000d cho iPhone 15 Pro",
            "17:48:00  tran_thi_b dat gia 17,000,000d cho iPhone 15 Pro",
            "09:30:00  nguyen_van_a dat gia 8,500,000d cho Tranh Son Dau",
            "08:00:00  techshop_vn tao phien AUC001 (iPhone 15 Pro)"
        );

        tblUsers   .setItems(filteredUsers = new FilteredList<>(userData,  p -> true));
        tblAuctions.setItems(filteredAucs  = new FilteredList<>(aucData,   p -> true));
        tblItems   .setItems(itemData);
        tblBids    .setItems(bidData);
    }

    // ═════════════════════════════════════════════════════════
    // FILTERS
    // ═════════════════════════════════════════════════════════
    private void setupFilters() {
        // Users: tim kiem + loc role
        Runnable userFilter = () -> {
            String kw   = txtSearchUser.getText().trim().toLowerCase();
            String role = cmbFilterRole.getValue();
            filteredUsers.setPredicate(row ->
                (kw.isEmpty() || row.username().toLowerCase().contains(kw)
                              || row.email().toLowerCase().contains(kw)) &&
                (role == null || role.equals("Tat ca") || row.role().equals(role))
            );
        };
        txtSearchUser.textProperty().addListener((o, old, nw) -> userFilter.run());
        cmbFilterRole.valueProperty().addListener((o, old, nw) -> userFilter.run());

        // Auctions: loc trang thai
        cmbFilterStatus.valueProperty().addListener((o, old, nw) -> {
            filteredAucs.setPredicate(row ->
                nw == null || nw.equals("Tat ca") || row.status().equals(nw));
        });
    }

    // ═════════════════════════════════════════════════════════
    // NAVIGATION
    // ═════════════════════════════════════════════════════════

    /** Tat ca nut Nav deu goi handleNav() -> doc source de biet pane nao can hien */
    @FXML
    private void handleNav(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        if      (src == btnNavOverview)  showPane(paneOverview,  btnNavOverview);
        else if (src == btnNavUsers)     showPane(paneUsers,     btnNavUsers);
        else if (src == btnNavAuctions)  showPane(paneAuctions,  btnNavAuctions);
        else if (src == btnNavItems)     showPane(paneItems,     btnNavItems);
        else if (src == btnNavBids)      showPane(paneBids,      btnNavBids);
    }

    private void showPane(VBox target, Button navBtn) {
        // An tat ca pane
        for (VBox pane : List.of(paneOverview, paneUsers, paneAuctions, paneItems, paneBids)) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        // Hien pane can thiet
        target.setVisible(true);
        target.setManaged(true);

        // Cap nhat style nav button
        if (currentNavBtn != null) currentNavBtn.setStyle(NAV_INACTIVE);
        navBtn.setStyle(NAV_ACTIVE);
        currentNavBtn = navBtn;
    }

    // ═════════════════════════════════════════════════════════
    // HANDLERS
    // ═════════════════════════════════════════════════════════

    @FXML private void handleSearchUser()     { /* filter tu dong qua listener */ }
    @FXML private void handleFilterRole()     { /* filter tu dong qua listener */ }
    @FXML private void handleFilterStatus()   { /* filter tu dong qua listener */ }

    @FXML
    private void handleRefreshActivity() {
        // TODO: tai lai hoat dong tu server
        logActivity("Admin lam moi du lieu");
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
            if (btn == ButtonType.OK) onConfirm.run();
        });
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}

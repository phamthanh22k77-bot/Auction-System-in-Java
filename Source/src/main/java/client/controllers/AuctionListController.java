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

/**
 * AuctionListController
 * FXML: AuctionList.fxml
 *
 * Hien thi toan bo phien dau gia, ho tro:
 *   - Tim kiem theo tu khoa
 *   - Loc theo danh muc + trang thai
 *   - Double-click dong -> chuyen sang ItemDetail
 */
public class AuctionListController implements Initializable {

    // ── Header ────────────────────────────────────────────────
    @FXML private Label  lblUsername;
    @FXML private Button btnLogout;

    // ── Filter bar ────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbStatus;

    // ── Table ─────────────────────────────────────────────────
    @FXML private TableView<AuctionRow>           tblAuctions;
    @FXML private TableColumn<AuctionRow, String> colAuctionId;
    @FXML private TableColumn<AuctionRow, String> colItemName;
    @FXML private TableColumn<AuctionRow, String> colCategory;
    @FXML private TableColumn<AuctionRow, String> colStartPrice;
    @FXML private TableColumn<AuctionRow, String> colCurPrice;
    @FXML private TableColumn<AuctionRow, String> colEndTime;
    @FXML private TableColumn<AuctionRow, String> colStatus;

    // ── Bottom bar ────────────────────────────────────────────
    @FXML private Label  lblBalance;
    @FXML private Label  lblTotalAuctions;
    @FXML private Button btnViewDetail;

    // ── Data ──────────────────────────────────────────────────
    private ObservableList<AuctionRow> allData   = FXCollections.observableArrayList();
    private FilteredList<AuctionRow>   filtered;

    // ═════════════════════════════════════════════════════════
    // INNER DTO — dai dien 1 dong trong TableView
    // Thay bang Auction model thuc te
    // ═════════════════════════════════════════════════════════
    public static class AuctionRow {
        public final String id, itemName, category, startPrice, curPrice, endTime, status;
        public AuctionRow(String id, String itemName, String category,
                          String startPrice, String curPrice, String endTime, String status) {
            this.id = id; this.itemName = itemName; this.category = category;
            this.startPrice = startPrice; this.curPrice = curPrice;
            this.endTime = endTime; this.status = status;
        }
    }

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindColumns();
        setupFilter();
        loadData();
        setupRowDoubleClick();
        setupSelectionListener();

        // Combobox options
        cmbCategory.setItems(FXCollections.observableArrayList(
            "Tat ca", "Electronics", "Art", "Vehicle"));
        cmbCategory.getSelectionModel().selectFirst();

        cmbStatus.setItems(FXCollections.observableArrayList(
            "Tat ca", "RUNNING", "OPEN", "FINISHED"));
        cmbStatus.getSelectionModel().selectFirst();

        lblUsername.setText("Bidder");
        lblBalance.setText("So du: 5,000,000 d");
    }

    // Gan cot TableView vao truong cua AuctionRow
    private void bindColumns() {
        colAuctionId .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colItemName  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().itemName));
        colCategory  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colStartPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colEndTime   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().endTime));
        colStatus    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // To mau cot Status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status) {
                    case "RUNNING" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "OPEN"    -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    case "FINISHED"-> setStyle("-fx-text-fill: #757575;");
                    default        -> setStyle("");
                }
            }
        });
    }

    // FilteredList: loc dong thoi theo tu khoa + danh muc + trang thai
    private void setupFilter() {
        filtered = new FilteredList<>(allData, p -> true);
        tblAuctions.setItems(filtered);

        Runnable applyFilter = () -> {
            String kw  = txtSearch.getText().trim().toLowerCase();
            String cat = cmbCategory.getValue();
            String st  = cmbStatus.getValue();
            filtered.setPredicate(row -> {
                boolean matchKw  = kw.isEmpty()
                    || row.itemName.toLowerCase().contains(kw)
                    || row.id.toLowerCase().contains(kw);
                boolean matchCat = cat == null || cat.equals("Tat ca")
                    || row.category.equals(cat);
                boolean matchSt  = st == null || st.equals("Tat ca")
                    || row.status.equals(st);
                return matchKw && matchCat && matchSt;
            });
            lblTotalAuctions.setText("Hien thi: " + filtered.size() + " phien");
        };

        txtSearch.textProperty().addListener((o, old, nw) -> applyFilter.run());
        cmbCategory.valueProperty().addListener((o, old, nw) -> applyFilter.run());
        cmbStatus  .valueProperty().addListener((o, old, nw) -> applyFilter.run());
    }

    private void loadData() {
        // Thay bang: server.getAuctions() hoac DAO.getAllAuctions()
        allData.setAll(
            new AuctionRow("AUC001","iPhone 15 Pro","Electronics","15,000,000","17,500,000","15/04 18:00","RUNNING"),
            new AuctionRow("AUC002","Tranh Son Dau", "Art",        "8,000,000",  "8,500,000","16/04 10:00","RUNNING"),
            new AuctionRow("AUC003","Toyota Camry",  "Vehicle",  "600,000,000","650,000,000","17/04 09:00","OPEN"),
            new AuctionRow("AUC004","Samsung TV 65", "Electronics","20,000,000","22,000,000","14/04 20:00","FINISHED"),
            new AuctionRow("AUC005","Xe May Wave",   "Vehicle",   "25,000,000", "28,000,000","18/04 15:00","OPEN")
        );
        lblTotalAuctions.setText("Hien thi: " + allData.size() + " phien");
    }

    // Double-click dong -> mo ItemDetail
    private void setupRowDoubleClick() {
        tblAuctions.setRowFactory(tv -> {
            TableRow<AuctionRow> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseButton.PRIMARY
                        && !row.isEmpty()) {
                    openItemDetail(row.getItem());
                }
            });
            return row;
        });
    }

    // Nut "Xem chi tiet" enabled khi co dong duoc chon
    private void setupSelectionListener() {
        btnViewDetail.setDisable(true);
        tblAuctions.getSelectionModel().selectedItemProperty()
            .addListener((o, old, nw) -> btnViewDetail.setDisable(nw == null));
    }

    // ═════════════════════════════════════════════════════════
    // HANDLERS
    // ═════════════════════════════════════════════════════════
    @FXML private void handleSearch()  { /* filter tu dong qua listener */ }
    @FXML private void handleFilter()  { /* filter tu dong qua listener */ }

    @FXML
    private void handleRefresh() {
        loadData();
        txtSearch.clear();
        cmbCategory.getSelectionModel().selectFirst();
        cmbStatus  .getSelectionModel().selectFirst();
    }

    @FXML
    private void handleViewDetail() {
        AuctionRow selected = tblAuctions.getSelectionModel().getSelectedItem();
        if (selected != null) openItemDetail(selected);
    }

    // Goi ca tu double-click va nut
    @FXML
    private void handleRowDoubleClick(javafx.scene.input.MouseEvent e) {
        if (e.getClickCount() == 2) handleViewDetail();
    }

    private void openItemDetail(AuctionRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/ItemDetail.fxml"));
            Parent root = loader.load();
            ItemDetailController ctrl = loader.getController();
            // Truyen du lieu sang ItemDetail
            ctrl.setAuctionData(
                row.id, row.itemName, row.category,
                row.startPrice, row.curPrice, row.endTime, row.status
            );
            Stage stage = (Stage) tblAuctions.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}

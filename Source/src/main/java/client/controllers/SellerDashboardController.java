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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * SellerDashboardController
 * FXML: SellerDashboard.fxml
 *
 * Quan ly san pham cua Seller:
 *   - Form them/sua san pham (fields dong theo danh muc)
 *   - TableView danh sach san pham
 *   - Tim kiem
 *   - Xoa san pham (confirm dialog)
 */
public class SellerDashboardController implements Initializable {

    // ── Header ────────────────────────────────────────────────
    @FXML private Label  lblUsername;
    @FXML private Button btnLogout;

    // ── Form (left sidebar) ───────────────────────────────────
    @FXML private Label       lblFormTitle;
    @FXML private TextField   txtName;
    @FXML private TextArea    txtDescription;
    @FXML private ComboBox<String> cmbCategory;

    // Fields dong theo danh muc
    @FXML private VBox        vboxElectronics;
    @FXML private TextField   txtBrand;
    @FXML private TextField   txtWarranty;

    @FXML private VBox        vboxArt;
    @FXML private TextField   txtArtist;
    @FXML private TextField   txtMedium;

    @FXML private VBox        vboxVehicle;
    @FXML private TextField   txtEngineType;
    @FXML private TextField   txtModelYear;

    @FXML private TextField   txtStartingPrice;
    @FXML private DatePicker  datePicker;
    @FXML private Label       lblFormError;
    @FXML private Button      btnSave;
    @FXML private Button      btnCancel;

    // ── Table ─────────────────────────────────────────────────
    @FXML private TextField   txtSearchProduct;
    @FXML private TableView<ProductRow>           tblProducts;
    @FXML private TableColumn<ProductRow, String> colItemId;
    @FXML private TableColumn<ProductRow, String> colName;
    @FXML private TableColumn<ProductRow, String> colCat;
    @FXML private TableColumn<ProductRow, String> colPrice;
    @FXML private TableColumn<ProductRow, String> colCurPrice;
    @FXML private TableColumn<ProductRow, String> colAucStatus;
    @FXML private TableColumn<ProductRow, String> colActions;

    // ── Stats ─────────────────────────────────────────────────
    @FXML private Label lblTotalProducts;
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblFinishedAuctions;

    // ── State ─────────────────────────────────────────────────
    private ObservableList<ProductRow> allData = FXCollections.observableArrayList();
    private FilteredList<ProductRow>   filtered;
    private ProductRow editingRow = null; // null = them moi, != null = dang sua

    // ═════════════════════════════════════════════════════════
    // INNER DTO
    // ═════════════════════════════════════════════════════════
    public static class ProductRow {
        public String id, name, category, startPrice, curPrice, aucStatus;
        // Them cac truong spec neu can
        public String brand, warranty, artist, medium, engineType, modelYear;

        public ProductRow(String id, String name, String category,
                          String startPrice, String curPrice, String aucStatus) {
            this.id = id; this.name = name; this.category = category;
            this.startPrice = startPrice; this.curPrice = curPrice;
            this.aucStatus = aucStatus;
        }
    }

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCategory.setItems(FXCollections.observableArrayList(
            "Electronics", "Art", "Vehicle"));

        bindColumns();
        setupSearch();
        loadData();
        resetForm();

        lblUsername.setText("Seller A");

        // Double-click dong -> load vao form de sua
        tblProducts.setRowFactory(tv -> {
            TableRow<ProductRow> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseButton.PRIMARY
                        && !row.isEmpty()) {
                    loadRowIntoForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void bindColumns() {
        colItemId   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        colName     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colCat      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colPrice    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().startPrice));
        colCurPrice .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().curPrice));
        colAucStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().aucStatus));

        // Cot hanh dong: nut Sua + Xoa
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Sua");
            private final Button btnDelete = new Button("Xoa");
            {
                btnEdit.setStyle(
                    "-fx-background-color: #1565C0; -fx-text-fill: white;" +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnDelete.setStyle(
                    "-fx-background-color: #C62828; -fx-text-fill: white;" +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 3 10;");
                btnEdit.setOnAction(e -> loadRowIntoForm(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, btnEdit, btnDelete);
                setGraphic(box);
            }
        });

        // Mau trang thai phien dau gia
        colAucStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                switch (val) {
                    case "RUNNING" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "OPEN"    -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    default        -> setStyle("-fx-text-fill: #757575;");
                }
            }
        });
    }

    private void setupSearch() {
        filtered = new FilteredList<>(allData, p -> true);
        tblProducts.setItems(filtered);
        txtSearchProduct.textProperty().addListener((o, old, nw) -> {
            String kw = nw.trim().toLowerCase();
            filtered.setPredicate(row ->
                kw.isEmpty() || row.name.toLowerCase().contains(kw)
                             || row.id.toLowerCase().contains(kw));
            updateStats();
        });
    }

    private void loadData() {
        // Thay bang: dao.getItemsBySeller(currentSeller.getId())
        allData.setAll(
            new ProductRow("ITM001","iPhone 15 Pro", "Electronics","15,000,000","17,500,000","RUNNING"),
            new ProductRow("ITM002","Samsung TV 65", "Electronics","20,000,000","22,000,000","OPEN"),
            new ProductRow("ITM003","Tranh Son Dau",  "Art",        "8,000,000",  "8,500,000","RUNNING"),
            new ProductRow("ITM004","Toyota Camry",   "Vehicle",  "600,000,000","650,000,000","FINISHED")
        );
        updateStats();
    }

    private void updateStats() {
        long active   = allData.stream().filter(r -> r.aucStatus.equals("RUNNING")).count();
        long finished = allData.stream().filter(r -> r.aucStatus.equals("FINISHED")).count();
        lblTotalProducts  .setText("Tong: " + allData.size() + " san pham");
        lblActiveAuctions .setText("Dang dau gia: " + active);
        lblFinishedAuctions.setText("Da ket thuc: " + finished);
    }

    // ═════════════════════════════════════════════════════════
    // FORM LOGIC
    // ═════════════════════════════════════════════════════════

    /** Hien/an cac VBox spec theo danh muc duoc chon */
    @FXML
    private void handleCategoryChange() {
        String cat = cmbCategory.getValue();
        vboxElectronics.setVisible(false); vboxElectronics.setManaged(false);
        vboxArt        .setVisible(false); vboxArt        .setManaged(false);
        vboxVehicle    .setVisible(false); vboxVehicle    .setManaged(false);
        if (cat == null) return;
        switch (cat) {
            case "Electronics" -> { vboxElectronics.setVisible(true); vboxElectronics.setManaged(true); }
            case "Art"         -> { vboxArt        .setVisible(true); vboxArt        .setManaged(true); }
            case "Vehicle"     -> { vboxVehicle    .setVisible(true); vboxVehicle    .setManaged(true); }
        }
    }

    /** Dat form ve trang thai "Them moi" */
    @FXML
    private void handleNew() {
        editingRow = null;
        resetForm();
    }

    private void resetForm() {
        editingRow = null;
        lblFormTitle.setText("Them san pham moi");
        txtName.clear(); txtDescription.clear();
        txtBrand.clear(); txtWarranty.clear();
        txtArtist.clear(); txtMedium.clear();
        txtEngineType.clear(); txtModelYear.clear();
        txtStartingPrice.clear();
        datePicker.setValue(null);
        cmbCategory.getSelectionModel().clearSelection();
        lblFormError.setText("");
        vboxElectronics.setVisible(false); vboxElectronics.setManaged(false);
        vboxArt        .setVisible(false); vboxArt        .setManaged(false);
        vboxVehicle    .setVisible(false); vboxVehicle    .setManaged(false);
    }

    /** Load du lieu cua dong duoc chon vao form de chinh sua */
    private void loadRowIntoForm(ProductRow row) {
        editingRow = row;
        lblFormTitle.setText("Chinh sua san pham");
        txtName.setText(row.name);
        cmbCategory.setValue(row.category);
        handleCategoryChange();
        txtStartingPrice.setText(row.startPrice.replace(",", ""));
        lblFormError.setText("");

        // Dien spec tuong ung
        switch (row.category) {
            case "Electronics" -> { txtBrand.setText(row.brand); txtWarranty.setText(row.warranty); }
            case "Art"         -> { txtArtist.setText(row.artist); txtMedium.setText(row.medium); }
            case "Vehicle"     -> { txtEngineType.setText(row.engineType); txtModelYear.setText(row.modelYear); }
        }
    }

    @FXML
    private void handleSave() {
        // Validate
        if (txtName.getText().trim().isEmpty()) {
            lblFormError.setText("Ten san pham khong duoc de trong."); return;
        }
        if (cmbCategory.getValue() == null) {
            lblFormError.setText("Vui long chon danh muc."); return;
        }
        if (txtStartingPrice.getText().trim().isEmpty()) {
            lblFormError.setText("Vui long nhap gia khoi diem."); return;
        }
        try {
            Double.parseDouble(txtStartingPrice.getText().trim());
        } catch (NumberFormatException e) {
            lblFormError.setText("Gia khoi diem khong hop le."); return;
        }
        if (datePicker.getValue() == null) {
            lblFormError.setText("Vui long chon ngay ket thuc."); return;
        }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            lblFormError.setText("Ngay ket thuc phai o tuong lai."); return;
        }

        lblFormError.setText("");

        if (editingRow == null) {
            // Them moi
            String newId = "ITM" + String.format("%03d", allData.size() + 1);
            ProductRow newRow = new ProductRow(
                newId, txtName.getText().trim(),
                cmbCategory.getValue(),
                txtStartingPrice.getText().trim(),
                txtStartingPrice.getText().trim(),
                "OPEN"
            );
            // Luu spec
            switch (cmbCategory.getValue()) {
                case "Electronics" -> { newRow.brand = txtBrand.getText(); newRow.warranty = txtWarranty.getText(); }
                case "Art"         -> { newRow.artist = txtArtist.getText(); newRow.medium = txtMedium.getText(); }
                case "Vehicle"     -> { newRow.engineType = txtEngineType.getText(); newRow.modelYear = txtModelYear.getText(); }
            }
            allData.add(newRow);
            showInfo("Da them san pham: " + newRow.name);
            // TODO: dao.saveItem(item)
        } else {
            // Cap nhat
            editingRow.name      = txtName.getText().trim();
            editingRow.category  = cmbCategory.getValue();
            editingRow.startPrice= txtStartingPrice.getText().trim();
            tblProducts.refresh();
            showInfo("Da cap nhat: " + editingRow.name);
            // TODO: dao.updateItem(item)
        }

        updateStats();
        resetForm();
    }

    @FXML
    private void handleCancel() { resetForm(); }

    @FXML
    private void handleSearchProduct() { /* filter tu dong qua listener */ }

    private void confirmDelete(ProductRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xac nhan xoa");
        alert.setHeaderText("Xoa san pham: " + row.name);
        alert.setContentText("Hanh dong nay khong the hoan tac.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                allData.remove(row);
                updateStats();
                // TODO: dao.deleteItem(row.id)
            }
        });
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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

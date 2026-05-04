package client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * BidderDashboardController
 * FXML: BidderDashboard.fxml
 *
 * Xu ly:
 *   - Tab loc danh muc (show/hide card theo Electronics/Art/Vehicles)
 *   - Click 6 product card -> hien detail panel ben phai
 *   - Click seller card -> thong bao / mo trang seller
 *   - Click "Dat gia ngay" -> mo BidConfirmation popup
 *   - Click nut User -> mo UserProfilePopup
 */
public class BidderDashboardController implements Initializable {

    // ── Header ────────────────────────────────────────────────
    @FXML private Button profileButton;

    // ── Filter ────────────────────────────────────────────────
    @FXML private Button    btnTabAll;
    @FXML private Button    btnTabElec;
    @FXML private Button    btnTabArt;
    @FXML private Button    btnTabVehicles;
    @FXML private TextField txtSearch;

    // ── 6 Product cards ───────────────────────────────────────
    @FXML private VBox cardElec1;   // MacBook Pro
    @FXML private VBox cardElec2;   // iPhone 15 Pro
    @FXML private VBox cardArt1;    // Tranh Mua Xuan
    @FXML private VBox cardArt2;    // Tuong Da Phat
    @FXML private VBox cardVeh1;    // Tesla Model 3
    @FXML private VBox cardVeh2;    // Honda CBR

    @FXML private FlowPane itemGridPane;

    // ── Detail panel ──────────────────────────────────────────
    @FXML private VBox   itemDetailPanel;
    @FXML private Label  detailImagePlaceholder;
    @FXML private Label  detailTitleLabel;
    @FXML private Label  detailCurrentPrice;
    @FXML private Label  detailTimeRemaining;
    @FXML private Circle detailShopAvatar;
    @FXML private Label  detailShopName;
    @FXML private Label  detailShopRating;
    @FXML private Label  descKey1;
    @FXML private Label  descVal1;
    @FXML private Label  descKey2;
    @FXML private Label  descVal2;
    @FXML private Label  descKey3;
    @FXML private Label  descVal3;
    @FXML private Button placeBidButton;

    // ── State ─────────────────────────────────────────────────
    private double selectedBidPrice = 0;
    private String selectedItemName = "";
    private String selectedAuctionId = "";

    // Style tab
    private static final String STYLE_TAB_ACTIVE =
        "-fx-background-color: #2c3e50; -fx-text-fill: white;" +
        "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 18; -fx-font-size: 13;";
    private static final String STYLE_TAB_INACTIVE =
        "-fx-background-color: white; -fx-border-color: #dde1e7;" +
        "-fx-border-radius: 20; -fx-background-radius: 20;" +
        "-fx-cursor: hand; -fx-padding: 6 18; -fx-font-size: 13;";

    // Danh sach tat ca card theo category (dung cho filter)
    private List<VBox> allElecCards;
    private List<VBox> allArtCards;
    private List<VBox> allVehCards;

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nhom card theo danh muc
        allElecCards = List.of(cardElec1, cardElec2);
        allArtCards  = List.of(cardArt1,  cardArt2);
        allVehCards  = List.of(cardVeh1,  cardVeh2);

        // Hover effect cho tung card
        addHoverEffect(cardElec1);
        addHoverEffect(cardElec2);
        addHoverEffect(cardArt1);
        addHoverEffect(cardArt2);
        addHoverEffect(cardVeh1);
        addHoverEffect(cardVeh2);

        // Tab mac dinh: Tat ca
        btnTabAll.setStyle(STYLE_TAB_ACTIVE);
        btnTabElec    .setStyle(STYLE_TAB_INACTIVE);
        btnTabArt     .setStyle(STYLE_TAB_INACTIVE);
        btnTabVehicles.setStyle(STYLE_TAB_INACTIVE);
    }

    // ═════════════════════════════════════════════════════════
    // HOVER EFFECT
    // ═════════════════════════════════════════════════════════

    private void addHoverEffect(VBox card) {
        String base = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(
            base + "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle(base));
    }

    // ═════════════════════════════════════════════════════════
    // TAB LOC DANH MUC
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleCategory(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();

        // Reset tat ca tab
        btnTabAll     .setStyle(STYLE_TAB_INACTIVE);
        btnTabElec    .setStyle(STYLE_TAB_INACTIVE);
        btnTabArt     .setStyle(STYLE_TAB_INACTIVE);
        btnTabVehicles.setStyle(STYLE_TAB_INACTIVE);
        src.setStyle(STYLE_TAB_ACTIVE);

        // Hien/an card theo category
        if (src == btnTabAll) {
            setCardsVisible(allElecCards, true);
            setCardsVisible(allArtCards,  true);
            setCardsVisible(allVehCards,  true);
        } else if (src == btnTabElec) {
            setCardsVisible(allElecCards, true);
            setCardsVisible(allArtCards,  false);
            setCardsVisible(allVehCards,  false);
        } else if (src == btnTabArt) {
            setCardsVisible(allElecCards, false);
            setCardsVisible(allArtCards,  true);
            setCardsVisible(allVehCards,  false);
        } else if (src == btnTabVehicles) {
            setCardsVisible(allElecCards, false);
            setCardsVisible(allArtCards,  false);
            setCardsVisible(allVehCards,  true);
        }

        // Dong detail panel khi doi tab
        closeDetailPanel();
    }

    private void setCardsVisible(List<VBox> cards, boolean visible) {
        for (VBox c : cards) {
            c.setVisible(visible);
            c.setManaged(visible);
        }
    }

    // ═════════════════════════════════════════════════════════
    // CLICK CARD SAN PHAM -> HIEN DETAIL PANEL
    // ═════════════════════════════════════════════════════════

    // Moi handleXxxClick() goi navigateToItemDetail() voi du lieu cua card do.
    // Category phai dung chinh xac "Electronics" / "Art" / "Vehicle"
    // de ItemDetailController build spec fields dung.

    @FXML private void handleElec1Click() {
        navigateToItemDetail("AUC001", "MacBook Pro M2 2022",
                "Electronics", "32,000,000", "32,000,000", "1h 30m", "RUNNING");
    }

    @FXML private void handleElec2Click() {
        navigateToItemDetail("AUC002", "iPhone 15 Pro Max 256GB",
                "Electronics", "25,500,000", "25,500,000", "3h 45m", "RUNNING");
    }

    @FXML private void handleArt1Click() {
        navigateToItemDetail("AUC003", "Buc tranh Mua Xuan",
                "Art", "15,000,000", "15,000,000", "10h 5m", "RUNNING");
    }

    @FXML private void handleArt2Click() {
        navigateToItemDetail("AUC004", "Tuong Da Phat co Nhat",
                "Art", "8,500,000", "8,500,000", "5h 20m", "RUNNING");
    }

    @FXML private void handleVeh1Click() {
        navigateToItemDetail("AUC005", "Tesla Model 3 - 2023",
                "Vehicle", "1,200,000,000", "1,200,000,000", "2d 7h 5m", "RUNNING");
    }

    @FXML private void handleVeh2Click() {
        navigateToItemDetail("AUC006", "Honda CBR 650R 2022",
                "Vehicle", "85,000,000", "85,000,000", "1d 3h", "RUNNING");
    }

    /**
     * Chuyen toan bo man hinh sang ItemDetail.fxml
     * va truyen du lieu cua card vua click vao ItemDetailController.
     *
     * ItemDetailController.setAuctionData() se tu dong:
     *   - Hien thong tin san pham, gia, trang thai
     *   - Build spec fields theo category (Electronics / Art / Vehicle)
     *   - Bat countdown
     *   - Load lich su bid
     *   - Hien BidPriceLineChart
     */
    private void navigateToItemDetail(String auctionId, String itemName,
                                      String category,  String startPrice,
                                      String curPrice,  String endTime,
                                      String status) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/ItemDetail.fxml"));
            Parent root = loader.load();

            ItemDetailController ctrl = loader.getController();
            ctrl.setAuctionData(
                auctionId, itemName, category,
                startPrice, curPrice, endTime, status
            );

            // Lay Stage tu bat ky node nao dang hien thi
            Stage stage = (Stage) btnTabAll.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // SELLER CARDS
    // ═════════════════════════════════════════════════════════

    @FXML private void handleSeller1Click() {
        System.out.println("Mo trang Shop O to Ha Noi");
        // TODO: navigate to SellerProfile.fxml
    }

    @FXML private void handleSeller2Click() {
        System.out.println("Mo trang Gallery Nghe Thuat");
    }

    @FXML private void handleSeller3Click() {
        System.out.println("Mo trang Tech Store VN");
    }

    // ═════════════════════════════════════════════════════════
    // "DAT GIA NGAY" -> MO BidConfirmation popup
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handlePlaceBid() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/BidConfirmation.fxml"));
            Parent root = loader.load();

            BidConfirmationController ctrl = loader.getController();
            ctrl.setAuctionInfo(
                selectedAuctionId,
                selectedItemName,
                selectedBidPrice,
                5_000_000       // So du demo — thay bang currentBidder.getBalance()
            );

            Stage popup = new Stage();
            popup.initStyle(StageStyle.DECORATED);
            popup.setTitle("Dat gia: " + selectedItemName);
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.initOwner(placeBidButton.getScene().getWindow());
            popup.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // USER POPUP
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/UserProfilePopup.fxml"));
            Parent root = loader.load();

            UserProfilePopupController ctrl = loader.getController();
            ctrl.setUserInfo("Nguyen Van A", "vana@email.com", 25, 5_000_000);
            ctrl.setOwnerStage((Stage) profileButton.getScene().getWindow());

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setScene(new Scene(root));
            popup.initOwner(profileButton.getScene().getWindow());

            // Vi tri: ngay duoi nut User
            double x = profileButton.localToScreen(0, 0).getX();
            double y = profileButton.localToScreen(0, 0).getY()
                       + profileButton.getHeight() + 4;
            popup.setX(x - 120);
            popup.setY(y);

            // Tu dong dong khi mat focus
            popup.focusedProperty().addListener((o, old, nw) -> {
                if (!nw) popup.close();
            });
            popup.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // CAC HANDLER CON LAI
    // ═════════════════════════════════════════════════════════

    @FXML private void handleHome()           { System.out.println("Trang chu"); }
    @FXML private void handleSupport()        { System.out.println("Ho tro"); }
    @FXML private void handleCart()           { System.out.println("Gio hang"); }
    @FXML private void handleNotify()         { System.out.println("Thong bao"); }
    @FXML private void handleSearch()         { System.out.println("Tim: " + txtSearch.getText()); }
    @FXML private void handleFilter()         { System.out.println("Bo loc"); }
    @FXML private void handleViewAll()        { System.out.println("Xem tat ca san pham"); }
    @FXML private void handleViewAllSellers() { System.out.println("Xem tat ca seller"); }

    // ═════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }
}

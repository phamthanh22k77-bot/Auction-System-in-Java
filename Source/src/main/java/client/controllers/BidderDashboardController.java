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

    @FXML private void handleElec1Click() {
        showDetail(
            "[MacBook Pro M2]", "MacBook Pro M2 2022",
            "32,000,000", "1h 30m",
            "Tech Store VN", "99.2% uy tin", "#d5f5e3",
            "Brand", "Apple",
            "Chip",  "Apple M2 Pro",
            "Nam",   "2022",
            32_000_000, "AUC001"
        );
    }

    @FXML private void handleElec2Click() {
        showDetail(
            "[iPhone 15 Pro]", "iPhone 15 Pro Max 256GB",
            "25,500,000", "3h 45m",
            "Tech Store VN", "99.2% uy tin", "#d5f5e3",
            "Brand",   "Apple",
            "Storage", "256 GB",
            "Mau",     "Titan Den",
            25_500_000, "AUC002"
        );
    }

    @FXML private void handleArt1Click() {
        showDetail(
            "[Tranh Son Dau]", "Buc tranh Mua Xuan",
            "15,000,000", "10h 5m",
            "Gallery Nghe Thuat", "98.5% uy tin", "#fdebd0",
            "Nghe si",   "Nguyen Van Binh",
            "Chat lieu", "Son dau tren vai",
            "Kich thuoc","80 x 100 cm",
            15_000_000, "AUC003"
        );
    }

    @FXML private void handleArt2Click() {
        showDetail(
            "[Tuong Da Phat]", "Tuong Da Phat co Nhat",
            "8,500,000", "5h 20m",
            "Gallery Nghe Thuat", "98.5% uy tin", "#fdebd0",
            "Vat lieu", "Da tu nhien",
            "Nguon goc","Nhat Ban TK 18",
            "Chieu cao","45 cm",
            8_500_000, "AUC004"
        );
    }

    @FXML private void handleVeh1Click() {
        showDetail(
            "[Tesla Model 3]", "Tesla Model 3 - 2023",
            "1,200,000,000", "2d 7h 5m",
            "Shop O to Ha Noi", "99.8% uy tin", "#d6eaf8",
            "Dong co",  "Dien (Long Range)",
            "Mau",      "Trang Ngoc Trai",
            "Xuat xuong","2023",
            1_200_000_000, "AUC005"
        );
    }

    @FXML private void handleVeh2Click() {
        showDetail(
            "[Honda CBR 650]", "Honda CBR 650R 2022",
            "85,000,000", "1d 3h",
            "Shop O to Ha Noi", "99.8% uy tin", "#d6eaf8",
            "Dong co",  "4 xy lanh, 649cc",
            "Cong suat","95 HP",
            "Nam",      "2022",
            85_000_000, "AUC006"
        );
    }

    /**
     * Dien thong tin vao detail panel va hien no.
     */
    private void showDetail(
            String imgText, String title,
            String price,   String time,
            String shopName,String shopRating, String avatarColor,
            String key1, String val1,
            String key2, String val2,
            String key3, String val3,
            double bidPrice, String auctionId) {

        selectedBidPrice  = bidPrice;
        selectedItemName  = title;
        selectedAuctionId = auctionId;

        detailImagePlaceholder.setText(imgText);
        detailTitleLabel     .setText(title);
        detailCurrentPrice   .setText("Gia hien tai: " + formatMoney(bidPrice) + " d");
        detailTimeRemaining  .setText("Con lai: " + time);
        detailShopName       .setText(shopName);
        detailShopRating     .setText(shopRating);
        detailShopAvatar     .setFill(javafx.scene.paint.Color.web(avatarColor));

        descKey1.setText(key1 + ":"); descVal1.setText(val1);
        descKey2.setText(key2 + ":"); descVal2.setText(val2);
        descKey3.setText(key3 + ":"); descVal3.setText(val3);

        itemDetailPanel.setVisible(true);
        itemDetailPanel.setManaged(true);
    }

    @FXML
    private void handleCloseDetail() {
        closeDetailPanel();
    }

    private void closeDetailPanel() {
        itemDetailPanel.setVisible(false);
        itemDetailPanel.setManaged(false);
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

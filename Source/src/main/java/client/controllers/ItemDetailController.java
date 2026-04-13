package client.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * ItemDetailController
 * FXML: ItemDetail.fxml
 *
 * Nhan du lieu tu AuctionListController hoac BidderDashboardController.
 * Hien thi:
 *   - Thong tin san pham (ten, gia, mo ta, seller)
 *   - Specs dong theo loai (Electronics / Art / Vehicle)
 *   - Dem nguoc thoi gian con lai
 *   - Lich su bid gan day
 *   - Nut "Vao phong dau gia" -> BiddingScreen
 */
public class ItemDetailController implements Initializable {

    @FXML private Label  lblPageTitle;
    @FXML private Label  lblItemName;
    @FXML private Label  lblCategory;
    @FXML private Label  lblStartPrice;
    @FXML private Label  lblCurrentPrice;
    @FXML private Label  lblSeller;
    @FXML private VBox   vboxSpecFields;
    @FXML private Label  lblDescription;
    @FXML private Label  lblCountdown;
    @FXML private Label  lblStatus;
    @FXML private ListView<String> listBidHistory;

    // State
    private Timeline countdownTimer;
    private String   currentAuctionId;
    private String   currentCategory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Du lieu duoc nap qua setAuctionData() tu man hinh truoc
        // Neu mo truc tiep thi load du lieu demo
        loadDemoIfEmpty();
    }

    // ═════════════════════════════════════════════════════════
    // NHAN DU LIEU TU MAN HINH TRUOC
    // ═════════════════════════════════════════════════════════

    /**
     * Goi tu AuctionListController hoac BidderDashboardController
     * sau loader.load():
     *     ItemDetailController ctrl = loader.getController();
     *     ctrl.setAuctionData(...);
     */
    public void setAuctionData(String auctionId, String itemName, String category,
                               String startPrice, String curPrice,
                               String endTime,   String status) {
        this.currentAuctionId = auctionId;
        this.currentCategory  = category;

        lblPageTitle    .setText("Chi tiet: " + itemName);
        lblItemName     .setText(itemName);
        lblCategory     .setText("Danh muc: " + category);
        lblStartPrice   .setText(startPrice + " d");
        lblCurrentPrice .setText(curPrice   + " d");
        lblSeller       .setText("Tech Shop VN");  // thay bang auction.getSeller().getCompanyName()
        lblStatus       .setText(status);
        lblDescription  .setText("Mo ta chi tiet cua san pham " + itemName + ".");

        // Specs dong theo loai
        buildSpecFields(category);

        // Lich su bid
        loadBidHistory(auctionId);

        // Dem nguoc
        startCountdown(endTime);

        // Mau trang thai
        switch (status) {
            case "RUNNING"  -> lblStatus.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 18; -fx-font-weight: bold;");
            case "OPEN"     -> lblStatus.setStyle("-fx-text-fill: #1565C0; -fx-font-size: 18; -fx-font-weight: bold;");
            case "FINISHED" -> lblStatus.setStyle("-fx-text-fill: #757575; -fx-font-size: 18; -fx-font-weight: bold;");
        }
    }

    // ═════════════════════════════════════════════════════════
    // SPEC FIELDS DONG THEO LOAI ITEM
    // ═════════════════════════════════════════════════════════

    private void buildSpecFields(String category) {
        vboxSpecFields.getChildren().clear();
        switch (category) {
            case "Electronics" -> {
                addSpec("Thuong hieu:", "Samsung");       // thay bang item.getBrand()
                addSpec("Bao hanh:",    "12 thang");      // item.getWarranty()
                addSpec("Xuat xu:",     "Han Quoc");
            }
            case "Art" -> {
                addSpec("Nghe si:",    "Nguyen Van Binh"); // item.getArtist()
                addSpec("Chat lieu:",  "Son dau");         // item.getMedium()
                addSpec("Kich thuoc:", "80x100 cm");
            }
            case "Vehicle" -> {
                addSpec("Dong co:",    "Xang 2.0L");       // item.getEngineType()
                addSpec("Nam SX:",     "2022");            // item.getModelYear()
                addSpec("Bien so:",    "51A-12345");       // item.getLicensePlate()
            }
        }
    }

    private void addSpec(String key, String value) {
        HBox row = new HBox(12);
        Label lblKey = new Label(key);
        lblKey.setStyle("-fx-text-fill: #757575; -fx-font-size: 13; -fx-min-width: 100;");
        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: #212121; -fx-font-size: 13;");
        row.getChildren().addAll(lblKey, lblVal);
        vboxSpecFields.getChildren().add(row);
    }

    // ═════════════════════════════════════════════════════════
    // BID HISTORY
    // ═════════════════════════════════════════════════════════

    private void loadBidHistory(String auctionId) {
        // Thay bang: server.getBidHistory(auctionId)
        listBidHistory.getItems().setAll(
            "17:50:05  user_abc       17,500,000 d",
            "17:48:22  bidder_xyz     17,000,000 d",
            "17:45:10  nguyen_van_a   16,500,000 d",
            "17:42:00  user_abc       16,000,000 d",
            "17:40:33  tran_thi_b     15,500,000 d"
        );
    }

    // ═════════════════════════════════════════════════════════
    // COUNTDOWN
    // ═════════════════════════════════════════════════════════

    /**
     * Parse endTime dang "15/04 18:00" hoac dung so giay truc tiep.
     * Hien tai dung 30 phut demo.
     */
    private void startCountdown(String endTime) {
        if (countdownTimer != null) countdownTimer.stop();

        final long[] secsLeft = {30 * 60}; // 30 phut demo

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secsLeft[0]--;
            if (secsLeft[0] <= 0) {
                lblCountdown.setText("DA KET THUC");
                countdownTimer.stop();
                return;
            }
            long h = secsLeft[0] / 3600;
            long m = (secsLeft[0] % 3600) / 60;
            long s = secsLeft[0] % 60;
            lblCountdown.setText(String.format("%02d:%02d:%02d", h, m, s));

            if (secsLeft[0] <= 300) { // 5 phut cuoi: doi mau do
                lblCountdown.setStyle(
                    "-fx-text-fill: #BF360C; -fx-font-size: 28; -fx-font-weight: bold;");
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private void loadDemoIfEmpty() {
        if (lblItemName.getText() == null || lblItemName.getText().isEmpty()
                || lblItemName.getText().equals("Ten san pham")) {
            setAuctionData("AUC001","iPhone 15 Pro","Electronics",
                "15,000,000","17,500,000","15/04 18:00","RUNNING");
        }
    }

    // ═════════════════════════════════════════════════════════
    // HANDLERS
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleBack() {
        if (countdownTimer != null) countdownTimer.stop();
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/AuctionList.fxml"));
            Stage stage = (Stage) lblItemName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleEnterBidding() {
        if (countdownTimer != null) countdownTimer.stop();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/BiddingScreen.fxml"));
            Parent root = loader.load();
            // Truyen auction sang BiddingController
            // BiddingController ctrl = loader.getController();
            // ctrl.setAuction(currentAuction, currentBidder);
            Stage stage = (Stage) lblItemName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}

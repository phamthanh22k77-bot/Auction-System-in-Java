package client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * BidConfirmationController
 * FXML: BidConfirmation.fxml
 *
 * Duoc goi tu BidderDashboardController:
 *     FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/BidConfirmation.fxml"));
 *     Parent root = loader.load();
 *     BidConfirmationController ctrl = loader.getController();
 *     ctrl.setAuctionInfo(itemName, currentHighestBid, userBalance, auctionId);
 *     Stage popup = new Stage();
 *     popup.initOwner(mainStage);
 *     popup.initStyle(StageStyle.DECORATED);
 *     popup.setScene(new Scene(root));
 *     popup.show();
 *
 * Tinh nang:
 *   - Tang/giam gia bang nut +/- theo BID_STEP
 *   - 3 nut quick-step: +100k / +500k / +1tr
 *   - Tinh phi tu dong (1% phi thanh toan + 0.5% phi dv)
 *   - Canh bao khi so du khong du
 *   - Validate truoc khi gui
 *   - Dong popup sau khi dat thanh cong
 */
public class BidConfirmationController implements Initializable {

    // ── FXML fields ───────────────────────────────────────────
    @FXML private Label     lblItemName;
    @FXML private Label     lblCurrentHighest;
    @FXML private TextField bidAmountField;
    @FXML private Button    btnDecrease;
    @FXML private Button    btnIncrease;
    @FXML private Button    btnStep1;   // +100k
    @FXML private Button    btnStep2;   // +500k
    @FXML private Button    btnStep3;   // +1tr
    @FXML private Label     lblBalance;
    @FXML private Label     lblPaymentFee;
    @FXML private Label     lblServiceFee;
    @FXML private Label     lblTotal;
    @FXML private Label     lblBalanceWarning;
    @FXML private Label     lblError;
    @FXML private Button    btnConfirm;

    // ── Config phi ────────────────────────────────────────────
    private static final double PAYMENT_FEE_RATE = 0.01;   // 1%
    private static final double SERVICE_FEE_RATE = 0.005;  // 0.5%
    private static final double DEFAULT_STEP     = 100_000; // Buoc mac dinh cua +/-

    // ── Du lieu phien ─────────────────────────────────────────
    private String auctionId          = "";
    private String itemName           = "San pham";
    private double currentHighestBid  = 0;
    private double userBalance        = 0;
    private double bidStep            = DEFAULT_STEP;

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cap nhat phi moi khi nguoi dung thay doi so tien
        bidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            hideError();
            updateFeeDisplay();
            checkBalanceWarning();
        });
    }

    // ═════════════════════════════════════════════════════════
    // API: NHAN DU LIEU TU MAN HINH TRUOC
    // ═════════════════════════════════════════════════════════

    /**
     * Goi ngay sau loader.load() de dien thong tin phien vao popup.
     *
     * @param auctionId       ID phien dau gia
     * @param itemName        Ten san pham
     * @param currentHighest  Gia dat cao nhat hien tai
     * @param userBalance     So du vi cua bidder hien tai
     */
    public void setAuctionInfo(String auctionId, String itemName,
                               double currentHighest, double userBalance) {
        this.auctionId         = auctionId;
        this.itemName          = itemName;
        this.currentHighestBid = currentHighest;
        this.userBalance       = userBalance;

        // Tinh buoc tang/giam phu hop voi muc gia
        this.bidStep = calcSmartStep(currentHighest);

        // Gia mac dinh de xuat = gia cao nhat + 1 buoc
        double suggested = currentHighest + bidStep;

        // Hien thi
        lblItemName       .setText(itemName);
        lblCurrentHighest .setText(formatMoney(currentHighest) + " d");
        lblBalance        .setText(formatMoney(userBalance)     + " d");
        bidAmountField    .setText(formatMoney(suggested));

        updateFeeDisplay();
        checkBalanceWarning();
    }

    /**
     * Tinh buoc tang/giam hop ly theo muc gia:
     *   < 10 trieu    -> buoc 100k
     *   10tr - 100tr  -> buoc 500k
     *   100tr - 1ty   -> buoc 1tr
     *   > 1ty         -> buoc 5tr
     */
    private double calcSmartStep(double price) {
        if      (price < 10_000_000)  return 100_000;
        else if (price < 100_000_000) return 500_000;
        else if (price < 1_000_000_000) return 1_000_000;
        else                          return 5_000_000;
    }

    // ═════════════════════════════════════════════════════════
    // TANG / GIAM GIA
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleIncrease() {
        double current = parseAmount();
        bidAmountField.setText(formatMoney(current + bidStep));
    }

    @FXML
    private void handleDecrease() {
        double current = parseAmount();
        double newAmt  = current - bidStep;

        if (newAmt <= currentHighestBid) {
            showError("Gia phai cao hon gia hien tai: " + formatMoney(currentHighestBid) + " d");
            return;
        }
        bidAmountField.setText(formatMoney(newAmt));
    }

    /** 3 nut quick-step: +100k / +500k / +1tr */
    @FXML
    private void handleQuickStep(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        double step;
        if      (src == btnStep1) step = 100_000;
        else if (src == btnStep2) step = 500_000;
        else                      step = 1_000_000;

        double current = parseAmount();
        bidAmountField.setText(formatMoney(current + step));
    }

    // ═════════════════════════════════════════════════════════
    // HIEN THI PHI (tu dong khi thay doi so tien)
    // ═════════════════════════════════════════════════════════

    private void updateFeeDisplay() {
        try {
            double bid     = parseAmount();
            double payFee  = bid * PAYMENT_FEE_RATE;
            double svcFee  = bid * SERVICE_FEE_RATE;
            double total   = bid + payFee + svcFee;

            lblPaymentFee.setText(formatMoney(payFee) + " d");
            lblServiceFee.setText(formatMoney(svcFee) + " d");
            lblTotal     .setText(formatMoney(total)  + " d");

            // To mau tong tien neu vuot so du
            if (total > userBalance) {
                lblTotal.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 15; -fx-font-weight: bold;");
            } else {
                lblTotal.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 15; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException e) {
            lblPaymentFee.setText("-- d");
            lblServiceFee.setText("-- d");
            lblTotal     .setText("-- d");
        }
    }

    private void checkBalanceWarning() {
        try {
            double bid   = parseAmount();
            double total = bid * (1 + PAYMENT_FEE_RATE + SERVICE_FEE_RATE);
            if (total > userBalance) {
                lblBalanceWarning.setText(
                    "So du chua du! Can them: " +
                    formatMoney(total - userBalance) + " d");
                lblBalanceWarning.setVisible(true);
                lblBalanceWarning.setManaged(true);
            } else {
                lblBalanceWarning.setVisible(false);
                lblBalanceWarning.setManaged(false);
            }
        } catch (NumberFormatException ignored) {}
    }

    // ═════════════════════════════════════════════════════════
    // XAC NHAN DAT GIA
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleConfirm() {
        // 1. Parse so tien
        double bid;
        try {
            bid = parseAmount();
        } catch (NumberFormatException e) {
            showError("So tien khong hop le. Vui long nhap so.");
            return;
        }

        // 2. Phai cao hon gia hien tai
        if (bid <= currentHighestBid) {
            showError("Gia phai cao hon: " + formatMoney(currentHighestBid) + " d");
            return;
        }

        // 3. Kiem tra so du
        double total = bid * (1 + PAYMENT_FEE_RATE + SERVICE_FEE_RATE);
        if (total > userBalance) {
            showError("So du khong du. Vui long nap them tien.");
            return;
        }

        // 4. GUI LEN SERVER
        // TODO: ket noi Nguoi 3 (network layer)
        //   BidTransaction tx = networkClient.placeBid(auctionId, currentUser.getId(), bid);
        //   if (tx != null && tx.validate()) { ... dong popup ... }
        System.out.println("[BidConfirmation] Dat gia: " + formatMoney(bid) + " d | Phien: " + auctionId);

        // 5. Vo hieu nut tranh bam nhieu lan
        btnConfirm.setDisable(true);
        btnConfirm.setText("Dang xu ly...");

        // 6. Gia lap thanh cong (xoa khi co server thuc)
        javafx.application.Platform.runLater(() -> {
            closePopup();
        });
    }

    // ═════════════════════════════════════════════════════════
    // CAC HANDLER PHU
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleTopUp() {
        // TODO: mo man hinh nap tien (co the la popup rieng)
        System.out.println("[BidConfirmation] Mo man hinh nap tien");
    }

    @FXML
    private void handleClose() {
        closePopup();
    }

    // ═════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════

    /**
     * Parse TextField: bo dau phay, dau cham, khoang trang.
     * Vi du: "1,200,000" -> 1200000.0
     */
    private double parseAmount() {
        String raw = bidAmountField.getText()
                         .replace(",", "")
                         .replace(".", "")
                         .replace(" ", "")
                         .trim();
        return Double.parseDouble(raw);
    }

    /**
     * Format so thanh "1,200,000"
     */
    private String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void closePopup() {
        Stage stage = (Stage) btnConfirm.getScene().getWindow();
        stage.close();
    }
}

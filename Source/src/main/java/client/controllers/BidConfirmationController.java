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
 * Điều khiển cửa sổ xác nhận đặt giá (BidConfirmationController).
 * Giao diện tương ứng: BidConfirmation.fxml
 *
 * Được gọi từ màn hình BiddingController hoặc BidderDashboardController để mở Popup.
 *
 * Tính năng chính:
 * - Tăng/giảm giá thầu bằng nút +/- theo bước nhảy thông minh (BID_STEP)
 * - Cung cấp 3 nút đặt nhanh (Quick-step): +100k / +500k / +1tr
 * - Tự động tính phí phát sinh thời gian thực (1% phí thanh toán + 0.5% phí dịch vụ)
 * - Hiển thị cảnh báo màu đỏ chớp nháy và số tiền cần nạp thêm nếu số dư không đủ
 * - Xác thực toàn diện các điều kiện trước khi gửi gói tin lên Server
 * - Tự động đóng Popup sau khi gửi yêu cầu thầu thành công
 */
public class BidConfirmationController implements Initializable {

    // ═════════════════════════════════════════════════════════
    // CÁC THUỘC TÍNH GIAO DIỆN (FXML FIELDS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private Label lblItemName;
    @FXML
    private Label lblCurrentHighest;
    @FXML
    private TextField bidAmountField;
    @FXML
    private Button btnDecrease;
    @FXML
    private Button btnIncrease;
    @FXML
    private Button btnStep1; // +100k
    @FXML
    private Button btnStep2; // +500k
    @FXML
    private Button btnStep3; // +1tr
    @FXML
    private Button btnStep4; // +5tr
    @FXML
    private Label lblBalance;
    @FXML
    private Label lblPaymentFee;
    @FXML
    private Label lblServiceFee;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblBalanceWarning;
    @FXML
    private Label lblError;
    @FXML
    private Button btnConfirm;

    // ═════════════════════════════════════════════════════════
    // CẤU HÌNH CÁC LOẠI PHÍ (FEE CONFIGURATION)
    // ═════════════════════════════════════════════════════════
    private static final double PAYMENT_FEE_RATE = 0.01; // Phí thanh toán: 1%
    private static final double SERVICE_FEE_RATE = 0.005; // Phí dịch vụ: 0.5%
    private static final double DEFAULT_STEP = 100_000; // Bước mặc định của tăng/giảm +/-

    // ═════════════════════════════════════════════════════════
    // DỮ LIỆU PHIÊN ĐẤU GIÁ (DATA)
    // ═════════════════════════════════════════════════════════
    private String auctionId = "";
    private String itemName = "";
    private double currentHighestBid = 0;
    private double userBalance = 0;
    private double bidStep = DEFAULT_STEP;

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI TẠO (INITIALIZE)
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tự động tính toán lại các khoản phí phát sinh mỗi khi người dùng thay đổi số tiền thầu
        bidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            hideError();
            updateFeeDisplay();
            checkBalanceWarning();
        });
    }

    // ═════════════════════════════════════════════════════════
    // API: NHẬN DỮ LIỆU TỪ MÀN HÌNH TRƯỚC TRUYỀN SANG
    // ═════════════════════════════════════════════════════════

    /**
     * Điền thông tin chi tiết của phiên đấu giá vào Popup ngay sau khi tải FXML thành công.
     *
     * @param auctionId      Mã số phiên đấu giá định danh
     * @param itemName        Tên sản phẩm đang đấu giá
     * @param currentHighest  Mức giá đấu cao nhất hiện tại của phiên
     * @param userBalance     Số dư ví hiện tại của người dùng
     * @param initialBid      Mức giá thầu đề xuất ban đầu
     */
    public void setAuctionInfo(String auctionId, String itemName, double currentHighest, double userBalance,
                               double initialBid) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.currentHighestBid = currentHighest;
        this.userBalance = userBalance;

        // Tính toán bước tăng giảm thông minh phù hợp với khoảng giá hiện tại
        this.bidStep = calcSmartStep(currentHighest);

        // Giá mặc định đề xuất = giá cao nhất hiện tại + 1 bước nhảy thông minh
        double suggested = (initialBid > 0) ? initialBid : (currentHighest + bidStep);

        // Hiển thị thông tin lên giao diện người dùng
        lblItemName.setText(itemName);
        lblCurrentHighest.setText(formatMoney(currentHighest) + " đ");
        lblBalance.setText(formatMoney(userBalance) + " đ");
        bidAmountField.setText(formatMoney(suggested));

        updateFeeDisplay();
        checkBalanceWarning();
    }

    /**
     * Tự động tính toán bước tăng/giảm phù hợp theo phân khúc giá:
     * - Dưới 10 triệu đồng: Bước nhảy 100k
     * - Từ 10 triệu đến 100 triệu đồng: Bước nhảy 500k
     * - Từ 100 triệu đến 1 tỷ đồng: Bước nhảy 1tr
     * - Trên 1 tỷ đồng: Bước nhảy 5tr
     */
    private double calcSmartStep(double price) {
        if (price < 10_000_000)
            return 100_000;
        else if (price < 100_000_000)
            return 500_000;
        else if (price < 1_000_000_000)
            return 1_000_000;
        else
            return 5_000_000;
    }

    // ═════════════════════════════════════════════════════════
    // TĂNG / GIẢM GIÁ THẦU
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleIncrease() {
        double current = parseAmount();
        bidAmountField.setText(formatMoney(current + bidStep));
    }

    @FXML
    private void handleDecrease() {
        double current = parseAmount();
        double newAmt = current - bidStep;

        if (newAmt <= currentHighestBid) {
            showError("Giá đặt thầu phải cao hơn giá thầu hiện tại: " + formatMoney(currentHighestBid) + " đ");
            return;
        }
        bidAmountField.setText(formatMoney(newAmt));
    }

    /** 4 nút quick-step: +100k / +500k / +1tr / +5tr */
    @FXML
    private void handleQuickStep(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        double step;
        if (src == btnStep1)
            step = 100_000;
        else if (src == btnStep2)
            step = 500_000;
        else if (src == btnStep3)
            step = 1_000_000;
        else
            step = 5_000_000;

        double current = parseAmount();
        bidAmountField.setText(formatMoney(current + step));
    }

    // ═════════════════════════════════════════════════════════
    // HIỂN THỊ PHÍ DỊCH VỤ (TỰ ĐỘNG CẬP NHẬT)
    // ═════════════════════════════════════════════════════════
    private void updateFeeDisplay() {
        try {
            double bid = parseAmount();
            double payFee = bid * PAYMENT_FEE_RATE;
            double svcFee = bid * SERVICE_FEE_RATE;
            double total = bid + payFee + svcFee;

            lblPaymentFee.setText(formatMoney(payFee) + " đ");
            lblServiceFee.setText(formatMoney(svcFee) + " đ");
            lblTotal.setText(formatMoney(total) + " đ");

            // Đổi nhãn sang màu đỏ cảnh báo nếu tổng tiền vượt quá số dư tài khoản
            if (total > userBalance) {
                lblTotal.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 15; -fx-font-weight: bold;");
            } else {
                lblTotal.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 15; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException e) {
            lblPaymentFee.setText("-- đ");
            lblServiceFee.setText("-- đ");
            lblTotal.setText("-- đ");
        }
    }

    private void checkBalanceWarning() {
        try {
            double bid = parseAmount();
            double total = bid * (1 + PAYMENT_FEE_RATE + SERVICE_FEE_RATE);
            if (total > userBalance) {
                lblBalanceWarning.setText("Số dư tài khoản không đủ! Cần nạp thêm: " + formatMoney(total - userBalance) + " đ");
                lblBalanceWarning.setVisible(true);
                lblBalanceWarning.setManaged(true);
            } else {
                lblBalanceWarning.setVisible(false);
                lblBalanceWarning.setManaged(false);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    // ═════════════════════════════════════════════════════════
    // XÁC NHẬN ĐẶT GIÁ THẦU (CONFIRM)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleConfirm() {
        // 1. Phân tích số tiền từ ô nhập liệu
        double bid;
        try {
            bid = parseAmount();
        } catch (NumberFormatException e) {
            showError("Số tiền thầu nhập vào không hợp lệ. Vui lòng nhập ký tự số.");
            return;
        }

        // 2. Ràng buộc: Giá đặt thầu phải cao hơn giá hiện tại của phiên
        if (bid <= currentHighestBid) {
            showError("Giá đặt phải cao hơn mức hiện tại: " + formatMoney(currentHighestBid) + " đ");
            return;
        }

        // 3. Ràng buộc: Kiểm tra tổng tiền phát sinh so với số dư ví tài khoản
        double total = bid * (1 + PAYMENT_FEE_RATE + SERVICE_FEE_RATE);
        if (total > userBalance) {
            showError("Số dư ví không đủ để thanh toán. Vui lòng nạp thêm tiền.");
            return;
        }

        // 4. Gửi gói tin thầu lên Server qua Socket Connection
        client.network.ClientSocketManager.getInstance().sendPacket(new client.message.PacketMessage(
                client.message.MessageType.MAKE_BID, new server.payload.MakeBidPayload(auctionId, bid)));
        System.out
                .println("[BidConfirmation] Đã gửi yêu cầu đặt giá: " + formatMoney(bid) + " đ | Phiên: " + auctionId);

        // 5. Vô hiệu hóa nút thầu nhằm chặn hành vi bấm liên tiếp gây spam hệ thống
        btnConfirm.setDisable(true);
        btnConfirm.setText("Đang xử lý...");

        // 6. Đóng Popup ngay lập tức (Bảng tin đấu giá realtime sẽ tự vẽ lại thông tin mới gửi về từ Server)
        javafx.application.Platform.runLater(() -> {
            closePopup();
        });
    }

    // ═════════════════════════════════════════════════════════
    // CÁC HÀNH ĐỘNG KHÁC (HANDLERS)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleTopUp() {
        // [CẢI TIẾN] Mở nhanh màn hình nạp tiền của ví tài khoản
        System.out.println("[BidConfirmation] Mở màn hình nạp tiền hệ thống");
    }

    @FXML
    private void handleClose() {
        closePopup();
    }

    // ═════════════════════════════════════════════════════════
    // CÁC PHƯƠNG THỨC HỖ TRỢ (HELPERS)
    // ═════════════════════════════════════════════════════════

    /**
     * Chuẩn hóa văn bản số tiền thầu: Loại bỏ dấu phẩy, dấu chấm và khoảng trắng thô.
     * Ví dụ: Chuyển đổi "1,500,000 đ" về dạng số thực "1500000.0".
     */
    private double parseAmount() {
        String raw = bidAmountField.getText().replace(",", "").replace(".", "").replace(" ", "").trim();
        return Double.parseDouble(raw);
    }

    /**
     * Định dạng số thực thành chuỗi tiền tệ phân tách hàng nghìn dễ đọc.
     * Ví dụ: Định dạng 2500000 thành chuỗi "2,500,000".
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

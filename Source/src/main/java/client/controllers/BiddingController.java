package controllers.bidder;

import client.controllers.BidPriceLineChart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * BiddingController
 * FXML: BiddingScreen.fxml
 *
 * Luong su dung:
 *   1. Man hinh truoc (ItemDetail / BidderDashboard) goi setAuction() truoc khi hien
 *   2. initialize() khoi tao BidPriceLineChart + Countdown + Live blink
 *   3. Server (Nguoi 3) goi onBidUpdate() moi khi co bid moi -> cap nhat UI + chart
 *   4. onAuctionClosed() khi phien ket thuc -> khoa nut dat gia
 *
 * Implements BidObserver (bo comment khi Nguoi 3 xong network layer):
 *   public class BiddingController implements Initializable, BidObserver
 */
public class BiddingController implements Initializable {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Label    lblItemName;
    @FXML private Label    lblLiveIndicator;
    @FXML private Label    lblBalance;

    // ── Left panel ───────────────────────────────────────────
    @FXML private Label    lblCurrentPrice;
    @FXML private Label    lblHighestBidder;
    @FXML private Label    lblCountdown;
    @FXML private Label    lblCategory;
    @FXML private Label    lblSeller;
    @FXML private Label    lblStartPrice;
    @FXML private Label    lblEndTime;
    @FXML private ListView<String> listLiveBids;

    // ── Chart ────────────────────────────────────────────────
    @FXML private LineChart<String, Number> chartBidHistory;
    @FXML private CategoryAxis              axisTime;
    @FXML private NumberAxis                axisPrice;

    // ── Bid input ────────────────────────────────────────────
    @FXML private Button    btnQuick1;   // +100k
    @FXML private Button    btnQuick2;   // +500k
    @FXML private Button    btnQuick3;   // +1tr
    @FXML private Button    btnQuick4;   // +5tr
    @FXML private TextField txtBidAmount;
    @FXML private Button    btnPlaceBid;
    @FXML private Label     lblBidFeedback;

    // ── Helper objects ───────────────────────────────────────
    private BidPriceLineChart bidChart;
    private Timeline          countdownTimer;
    private Timeline          liveBlinkTimer;

    // ── Du lieu phien hien tai ───────────────────────────────
    private String auctionId         = "";
    private String itemName          = "Phiên đấu giá";
    private double currentHighestBid = 0;
    private double userBalance       = 0;
    private long   secondsLeft       = 0;

    // Quick bid steps khop voi text nut trong FXML
    private static final double[] QUICK_STEPS = {
        100_000,    // btnQuick1: +100k
        500_000,    // btnQuick2: +500k
        1_000_000,  // btnQuick3: +1tr
        5_000_000   // btnQuick4: +5tr
    };

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // ═════════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart();
        setupLiveBlink();
        setupBidInputListener();

        // Load du lieu demo neu chua duoc set tu man hinh truoc
        loadDemoData();
    }

    // ── Khoi tao BidPriceLineChart ───────────────────────────

    private void setupChart() {
        /*
         * BidPriceLineChart quan ly toan bo LineChart:
         *   - Them data point voi addBidPoint()
         *   - Tu dong gioi han 20 diem, fade-in, tooltip, highlight max
         *   - forceZeroInRange = false -> truc Y bat dau gan vung gia dat
         */
        bidChart = new BidPriceLineChart(chartBidHistory, axisTime, axisPrice);
        bidChart.setMaxPoints(20);
    }

    // ── Hieu ung nhay "TRUC TIEP" ────────────────────────────

    private void setupLiveBlink() {
        liveBlinkTimer = new Timeline(
            new KeyFrame(Duration.ZERO,
                e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(500),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(1000),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;"))
        );
        liveBlinkTimer.setCycleCount(Timeline.INDEFINITE);
        liveBlinkTimer.play();
    }

    // ── Xoa feedback khi nguoi dung bat dau nhap lai ─────────

    private void setupBidInputListener() {
        txtBidAmount.textProperty().addListener((obs, old, nw) -> {
            lblBidFeedback.setText("");
        });
    }

    // ═════════════════════════════════════════════════════════
    // API NHAN DU LIEU TU MAN HINH TRUOC
    // ═════════════════════════════════════════════════════════

    /**
     * Goi tu ItemDetailController hoac BidderDashboardController
     * ngay sau loader.load():
     *
     *     FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/BiddingScreen.fxml"));
     *     Parent root = loader.load();
     *     BiddingController ctrl = loader.getController();
     *     ctrl.setAuction("AUC001", "Tesla Model 3", "Vehicles",
     *                     "Shop Oto", "15/04 18:00",
     *                     1_200_000_000, 1_200_100_000,
     *                     5_000_000, 30 * 60);
     *
     * @param auctionId      ID phien
     * @param name           Ten san pham
     * @param category       Danh muc (Electronics / Art / Vehicles)
     * @param seller         Ten nguoi ban
     * @param endTimeStr     Chuoi ket thuc VD "15/04 18:00"
     * @param startPrice     Gia khoi diem
     * @param currentHighest Gia dat cao nhat hien tai
     * @param balance        So du vi cua bidder
     * @param totalSeconds   Tong so giay con lai cua phien
     */
    public void setAuction(String auctionId, String name, String category,
                           String seller,    String endTimeStr,
                           double startPrice, double currentHighest,
                           double balance,    long totalSeconds) {
        this.auctionId         = auctionId;
        this.itemName          = name;
        this.currentHighestBid = currentHighest;
        this.userBalance       = balance;
        this.secondsLeft       = totalSeconds;

        // Dien thong tin len UI
        lblItemName    .setText(name);
        lblCategory    .setText("Danh mục: "  + category);
        lblSeller      .setText("Người bán: " + seller);
        lblStartPrice  .setText("Giá khởi: "  + fmt(startPrice) + " đ");
        lblEndTime     .setText("Kết thúc: "  + endTimeStr);
        lblBalance     .setText(fmt(balance)   + " đ");
        lblCurrentPrice.setText(fmt(currentHighest) + " đ");
        lblHighestBidder.setText("Đang chờ lượt đặt...");

        // Bat dau dem nguoc
        startCountdown(totalSeconds);

        // TODO: Load lich su bid tu server
        // List<BidTransaction> history = server.getBidHistory(auctionId);
        // loadBidHistory(history);

        // TODO: Dang ky BidObserver
        // server.subscribe(auctionId, this);
    }

    // ═════════════════════════════════════════════════════════
    // BID OBSERVER — nhan cap nhat tu server (Nguoi 3)
    // ═════════════════════════════════════════════════════════

    /**
     * Server (network layer cua Nguoi 3) goi method nay tren background thread
     * moi khi co bid moi duoc xac nhan.
     *
     * Vi chay tren background thread, phai dung Platform.runLater()
     * de cap nhat JavaFX UI.
     *
     * Uncomment "@Override" khi Nguoi 3 tao interface BidObserver.
     */
    // @Override
    public void onBidUpdate(String bidderName, double newPrice) {
        Platform.runLater(() -> {
            currentHighestBid = newPrice;

            // Cap nhat gia hien tai
            lblCurrentPrice .setText(fmt(newPrice) + " đ");
            lblHighestBidder.setText("↑ " + bidderName);

            // Them diem vao chart
            bidChart.addBidPoint(newPrice);

            // Them vao live feed (moi nhat o tren cung)
            String entry = LocalTime.now().format(TIME_FMT)
                + "   " + bidderName
                + "   " + fmt(newPrice) + " đ";
            listLiveBids.getItems().add(0, entry);

            // Gioi han feed 50 dong
            if (listLiveBids.getItems().size() > 50) {
                listLiveBids.getItems().remove(50);
            }

            // Nhay live indicator nhanh khi co bid moi
            triggerLivePulse();
        });
    }

    /**
     * Goi khi phien dau gia ket thuc.
     */
    // @Override
    public void onAuctionClosed(String winnerName, double finalPrice) {
        Platform.runLater(() -> {
            // Dung dem nguoc
            if (countdownTimer != null) countdownTimer.stop();
            lblCountdown.setText("KẾT THÚC");
            lblCountdown.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 22; -fx-font-weight: bold;");

            // Khoa input
            btnPlaceBid  .setDisable(true);
            txtBidAmount .setDisable(true);
            btnQuick1    .setDisable(true);
            btnQuick2    .setDisable(true);
            btnQuick3    .setDisable(true);
            btnQuick4    .setDisable(true);

            // Hien ket qua
            String result = winnerName.isEmpty()
                ? "Phiên đã kết thúc — không có lượt đặt."
                : "Người thắng: " + winnerName + " — " + fmt(finalPrice) + " đ";
            showFeedback(result, true);
        });
    }

    // ═════════════════════════════════════════════════════════
    // COUNTDOWN
    // ═════════════════════════════════════════════════════════

    private void startCountdown(long totalSeconds) {
        if (countdownTimer != null) countdownTimer.stop();

        final long[] secs = {totalSeconds};

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secs[0]--;

            if (secs[0] <= 0) {
                lblCountdown.setText("00 : 00 : 00");
                countdownTimer.stop();
                onAuctionClosed("", 0);
                return;
            }

            long h = secs[0] / 3600;
            long m = (secs[0] % 3600) / 60;
            long s = secs[0] % 60;
            lblCountdown.setText(String.format("%02d : %02d : %02d", h, m, s));

            // Doi mau vang -> do khi duoi 5 phut
            if (secs[0] <= 300) {
                lblCountdown.setStyle(
                    "-fx-text-fill: #E74C3C; -fx-font-size: 22; -fx-font-weight: bold;");
            } else {
                lblCountdown.setStyle(
                    "-fx-text-fill: #D35400; -fx-font-size: 22; -fx-font-weight: bold;");
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    // ═════════════════════════════════════════════════════════
    // LIVE PULSE
    // ═════════════════════════════════════════════════════════

    /** Nhap nhanh 3 lan khi co bid moi, roi tro ve nhip binh thuong */
    private void triggerLivePulse() {
        liveBlinkTimer.stop();
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(100),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(200),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(300),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: transparent; -fx-font-size: 9;")),
            new KeyFrame(Duration.millis(400),
                e -> lblLiveIndicator.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 9;"))
        );
        pulse.setCycleCount(1);
        pulse.setOnFinished(e -> liveBlinkTimer.play());
        pulse.play();
    }

    // ═════════════════════════════════════════════════════════
    // FXML HANDLERS
    // ═════════════════════════════════════════════════════════

    /**
     * 4 nut quick bid: doc step tu mang QUICK_STEPS theo thu tu nut.
     * Text nut chi dung de hien thi, step thuc te lay tu QUICK_STEPS.
     */
    @FXML
    private void handleQuickBid(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();

        double step;
        if      (src == btnQuick1) step = QUICK_STEPS[0];   // 100k
        else if (src == btnQuick2) step = QUICK_STEPS[1];   // 500k
        else if (src == btnQuick3) step = QUICK_STEPS[2];   // 1tr
        else                       step = QUICK_STEPS[3];   // 5tr

        // De xuat = gia hien tai + step
        double suggested = currentHighestBid + step;
        txtBidAmount.setText(fmt(suggested));
        txtBidAmount.requestFocus();
        txtBidAmount.selectAll();
    }

    /**
     * Nut "DAT GIA":
     *   1. Parse so tien
     *   2. Validate (phai cao hon gia hien tai + du so du)
     *   3. Gui len server
     *   4. Cap nhat UI ngay (server se confirm lai qua onBidUpdate)
     */
    @FXML
    private void handlePlaceBid() {
        // 1. Parse
        double amount;
        try {
            String raw = txtBidAmount.getText()
                .replace(",", "").replace(".", "").replace(" ", "").trim();
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            showFeedback("Số tiền không hợp lệ. Vui lòng nhập số.", false);
            return;
        }

        // 2. Validate
        if (amount <= currentHighestBid) {
            showFeedback("Giá phải cao hơn giá hiện tại: " + fmt(currentHighestBid) + " đ", false);
            return;
        }
        if (amount > userBalance) {
            showFeedback("Số dư không đủ. Số dư hiện tại: " + fmt(userBalance) + " đ", false);
            return;
        }

        // 3. Gui len server
        // TODO: ket noi Nguoi 3
        //   server.sendBid(auctionId, currentUser.getId(), amount);
        System.out.println("[BiddingController] Dat gia: " + fmt(amount) + " d | Phien: " + auctionId);

        // 4. Cap nhat UI ngay (optimistic update)
        //    Server se confirm lai bang onBidUpdate() — neu bi tu choi thi hoan tac
        showFeedback("Đang xử lý... " + fmt(amount) + " đ", true);
        txtBidAmount.clear();

        // Gia lap xac nhan tu server (xoa dong nay khi co server thuc)
        onBidUpdate("Bạn", amount);
    }

    @FXML
    private void handleBack() {
        cleanup();
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/ItemDetail.fxml"));
            Stage stage = (Stage) btnPlaceBid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════
    // LOAD DU LIEU DEMO
    // ═════════════════════════════════════════════════════════

    /**
     * Du lieu demo de test giao dien ma khong can server.
     * Xoa khi ket noi server thuc.
     */
    private void loadDemoData() {
        setAuction(
            "AUC001",
            "Tesla Model 3 - 2023",
            "Vehicles",
            "Shop Oto Ha Noi",
            "15/04/2026 18:00",
            1_200_000_000,
            1_215_000_000,
            5_000_000,
            30 * 60  // 30 phut
        );

        // Them lich su bid demo vao chart va list
        double[] demoPrices  = {
            1_200_000_000, 1_202_000_000, 1_205_000_000,
            1_208_000_000, 1_210_000_000, 1_215_000_000
        };
        String[] demoTimes   = {"17:30", "17:35", "17:40", "17:44", "17:48", "17:52"};
        String[] demoBidders = {"user_a", "user_b", "user_a", "user_c", "user_b", "user_a"};

        for (int i = 0; i < demoPrices.length; i++) {
            // Them vao chart
            bidChart.addBidPoint(demoTimes[i], demoPrices[i]);

            // Them vao live feed
            listLiveBids.getItems().add(
                demoTimes[i] + "   " + demoBidders[i] + "   " + fmt(demoPrices[i]) + " đ"
            );
        }
    }

    // ═════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════

    /** Format so thanh "1,200,000,000" */
    private String fmt(double amount) {
        return String.format("%,.0f", amount);
    }

    /** Hien thong bao feedback, xanh = thanh cong, do = loi */
    private void showFeedback(String msg, boolean success) {
        lblBidFeedback.setText(msg);
        lblBidFeedback.setStyle(
            "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: "
            + (success ? "#27AE60" : "#E74C3C") + ";"
        );
    }

    /** Dung tat timer khi roi man hinh */
    private void cleanup() {
        if (countdownTimer  != null) countdownTimer.stop();
        if (liveBlinkTimer  != null) liveBlinkTimer.stop();
        // TODO: huy dang ky observer
        // server.unsubscribe(auctionId, this);
    }
}

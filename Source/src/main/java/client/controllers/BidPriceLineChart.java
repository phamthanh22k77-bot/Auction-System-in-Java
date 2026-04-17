package client.controllers;

import javafx.animation.FadeTransition;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * BidPriceLineChart
 *
 * Helper class quan ly LineChart gia dat theo thoi gian thuc.
 * Dung chung giua BiddingController va bat ky man hinh nao can hien thi chart.
 *
 * Su dung:
 *     // Khoi tao (FXML inject chart vao constructor)
 *     BidPriceLineChart chart = new BidPriceLineChart(chartBidHistory, axisTime, axisPrice);
 *     chart.setMaxPoints(20);
 *
 *     // Them diem khi co bid moi (goi tu onBidUpdate)
 *     chart.addBidPoint(17_500_000);
 *
 *     // Them du lieu lich su truoc do
 *     chart.loadHistory(listBidTransactions);
 *
 *     // Xoa trang
 *     chart.clear();
 */
public class BidPriceLineChart {

    private final LineChart<String, Number> chart;
    private final CategoryAxis              axisTime;
    private final NumberAxis                axisPrice;
    private final XYChart.Series<String, Number> series;

    private int    maxPoints  = 20;   // So diem toi da hien tren chart
    private double minBid     = 0;    // Gia thap nhat trong series hien tai
    private double maxBid     = 0;    // Gia cao nhat

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat      MONEY_FMT =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ═════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═════════════════════════════════════════════════════════

    public BidPriceLineChart(LineChart<String, Number> chart,
                             CategoryAxis axisTime,
                             NumberAxis   axisPrice) {
        this.chart     = chart;
        this.axisTime  = axisTime;
        this.axisPrice = axisPrice;

        // Tao series chinh
        this.series = new XYChart.Series<>();
        this.series.setName("Gia dat");
        chart.getData().add(this.series);

        applyStyle();
    }

    // ═════════════════════════════════════════════════════════
    // STYLE
    // ═════════════════════════════════════════════════════════

    private void applyStyle() {
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");

        // Truc Y: khong bat dau tu 0, tu tinh khoang theo du lieu
        axisPrice.setAutoRanging(true);
        axisPrice.setForceZeroInRange(false);
        axisPrice.setLabel("Gia (d)");

        // Truc X
        axisTime.setAutoRanging(true);
        axisTime.setLabel("Thoi gian");

        // CSS cho duong ke va diem
        chart.getStylesheets().add(
            "data:text/css," +
            ".chart-series-line { -fx-stroke: #FF7043; -fx-stroke-width: 2; }" +
            ".chart-line-symbol { -fx-background-color: #FF7043, white; " +
            "                     -fx-background-radius: 5; }" +
            ".chart-plot-background { -fx-background-color: transparent; }" +
            ".chart-vertical-grid-lines { -fx-stroke: rgba(255,255,255,0.05); }" +
            ".chart-horizontal-grid-lines{ -fx-stroke: rgba(255,255,255,0.08); }" +
            ".axis { -fx-tick-label-fill: #90A4AE; }" +
            ".axis-label { -fx-text-fill: #78909C; -fx-font-size: 11; }"
        );
    }

    // ═════════════════════════════════════════════════════════
    // THEM DU LIEU
    // ═════════════════════════════════════════════════════════

    /**
     * Them 1 diem moi vao chart voi timestamp hien tai.
     * Goi tu BiddingController.onBidUpdate().
     */
    public void addBidPoint(double price) {
        addBidPoint(LocalTime.now().format(TIME_FMT), price);
    }

    /**
     * Them 1 diem voi timestamp chi dinh (dung khi load lich su cu).
     */
    public void addBidPoint(String timestamp, double price) {
        XYChart.Data<String, Number> point = new XYChart.Data<>(timestamp, price);
        series.getData().add(point);

        // Cap nhat min/max de tinh ket thuc truc Y
        if (series.getData().size() == 1) {
            minBid = maxBid = price;
        } else {
            if (price < minBid) minBid = price;
            if (price > maxBid) maxBid = price;
        }

        // Giu toi da maxPoints diem
        if (series.getData().size() > maxPoints) {
            series.getData().remove(0);
        }

        // Them tooltip cho diem moi them
        addTooltip(point, timestamp, price);

        // Hieu ung fade-in cho diem moi
        if (point.getNode() != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), point.getNode());
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }

        // Lam noi bat diem cao nhat (mau vang)
        highlightMaxPoint();
    }

    /**
     * Load danh sach gia lich su truoc khi bat dau theo doi real-time.
     * Vi du:
     *     chart.loadHistory(auction.getBidHistory());
     *     // Sau do moi bat dau lang nghe onBidUpdate
     */
    public void loadHistory(java.util.List<double[]> history) {
        // history: moi phan tu la double[]{timestamp_millis, price}
        // Nhung o day dung string timestamp cho don gian
        clear();
        for (double[] entry : history) {
            String ts = LocalTime.ofSecondOfDay((long)(entry[0]) % 86400)
                                 .format(TIME_FMT);
            addBidPoint(ts, entry[1]);
        }
    }

    // ═════════════════════════════════════════════════════════
    // TOOLTIP
    // ═════════════════════════════════════════════════════════

    private void addTooltip(XYChart.Data<String, Number> point,
                            String timestamp, double price) {
        // Tooltip duoc gan sau khi node duoc tao (co the null ngay sau add)
        // JavaFX tao node async, dung runLater de dam bao
        javafx.application.Platform.runLater(() -> {
            if (point.getNode() != null) {
                Tooltip tip = new Tooltip(
                    timestamp + "\n" + MONEY_FMT.format((long) price) + " d"
                );
                tip.setStyle(
                    "-fx-background-color: #1A237E; -fx-text-fill: white;" +
                    "-fx-font-size: 12; -fx-background-radius: 6;");
                Tooltip.install(point.getNode(), tip);
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // LAM NOI BAT DIEM CAO NHAT
    // ═════════════════════════════════════════════════════════

    private void highlightMaxPoint() {
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() == null) continue;
                boolean isMax = d.getYValue().doubleValue() == maxBid;
                d.getNode().setStyle(isMax
                    ? "-fx-background-color: #FFD54F, white; -fx-background-radius: 6;"
                    : "-fx-background-color: #FF7043, white; -fx-background-radius: 4;"
                );
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // UTILITY
    // ═════════════════════════════════════════════════════════

    /** Xoa toan bo du lieu tren chart */
    public void clear() {
        series.getData().clear();
        minBid = maxBid = 0;
    }

    /** Lay gia cao nhat hien tai tren chart */
    public double getMaxBid() { return maxBid; }

    /** Lay gia thap nhat hien tai tren chart */
    public double getMinBid() { return minBid; }

    /** Lay so luong diem hien tai */
    public int getPointCount() { return series.getData().size(); }

    /** Thay doi so diem toi da (default: 20) */
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }

    /** Lay series de tuy chinh them neu can */
    public XYChart.Series<String, Number> getSeries() { return series; }
}

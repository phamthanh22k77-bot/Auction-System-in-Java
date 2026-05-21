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
 * Lớp hỗ trợ quản lý biểu đồ (BidPriceLineChart).
 *
 * Quản lý và vẽ biểu đồ LineChart diễn biến giá thầu theo thời gian thực.
 * Dùng chung giữa BiddingController và bất kỳ màn hình nào cần hiển thị biến động giá.
 *
 * Hướng dẫn sử dụng:
 * // Khởi tạo (FXML tự động chèn biểu đồ vào hàm khởi dựng)
 * BidPriceLineChart chart = new BidPriceLineChart(chartBidHistory, axisTime, axisPrice);
 * chart.setMaxPoints(20);
 *
 * // Thêm điểm mới khi có lượt thầu mới (gọi từ hàm onBidUpdate)
 * chart.addBidPoint(17_500_000);
 *
 * // Nạp dữ liệu lịch sử thầu trước đó
 * chart.loadHistory(listBidTransactions);
 *
 * // Xóa sạch dữ liệu biểu đồ
 * chart.clear();
 */
public class BidPriceLineChart {

    private final LineChart<String, Number> chart;
    private final CategoryAxis axisTime;
    private final NumberAxis axisPrice;
    private final XYChart.Series<String, Number> series;

    private int maxPoints = 20; // Số lượng điểm thầu tối đa hiển thị trên biểu đồ
    private double minBid = 0;  // Mức giá thầu thấp nhất trong chuỗi dữ liệu hiện tại
    private double maxBid = 0;  // Mức giá thầu cao nhất hiện tại

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat MONEY_FMT = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));

    // ═════════════════════════════════════════════════════════
    // HÀM KHỞI DỰNG (CONSTRUCTOR)
    // ═════════════════════════════════════════════════════════
    public BidPriceLineChart(LineChart<String, Number> chart, CategoryAxis axisTime, NumberAxis axisPrice) {
        this.chart = chart;
        this.axisTime = axisTime;
        this.axisPrice = axisPrice;

        // Tạo chuỗi dữ liệu (Series) chính biểu diễn giá đấu
        this.series = new XYChart.Series<>();
        this.series.setName("Giá đặt thầu");
        chart.getData().add(this.series);

        applyStyle();
    }

    // ═════════════════════════════════════════════════════════
    // THIẾT LẬP KIỂU DÁNG (STYLE CSS)
    // ═════════════════════════════════════════════════════════
    private void applyStyle() {
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");

        // Trục Y: Không bắt đầu từ số 0, tự động tính toán khoảng thầu phù hợp theo dữ liệu thực tế
        axisPrice.setAutoRanging(true);
        axisPrice.setForceZeroInRange(false);
        axisPrice.setLabel("Mức giá (đ)");

        // Trục X: Trục thời gian
        axisTime.setAutoRanging(true);
        axisTime.setLabel("Thời gian");

        // Nhúng mã CSS để tùy biến màu sắc đường kẻ và các điểm nút thầu trực quan
        chart.getStylesheets()
                .add("data:text/css," + ".chart-series-line { -fx-stroke: #FF7043; -fx-stroke-width: 2; }"
                        + ".chart-line-symbol { -fx-background-color: #FF7043, white; "
                        + "                     -fx-background-radius: 5; }"
                        + ".chart-plot-background { -fx-background-color: transparent; }"
                        + ".chart-vertical-grid-lines { -fx-stroke: rgba(255,255,255,0.05); }"
                        + ".chart-horizontal-grid-lines{ -fx-stroke: rgba(255,255,255,0.08); }"
                        + ".axis { -fx-tick-label-fill: #90A4AE; }"
                        + ".axis-label { -fx-text-fill: #78909C; -fx-font-size: 11; }");
    }

    // ═════════════════════════════════════════════════════════
    // THÊM DỮ LIỆU ĐẤU THẦU (ADD DATA)
    // ═════════════════════════════════════════════════════════

    /**
     * Thêm một điểm mới vào biểu đồ với mốc thời gian hiện tại. Gọi từ BiddingController.onBidUpdate().
     */
    public void addBidPoint(double price) {
        addBidPoint(LocalTime.now().format(TIME_FMT), price);
    }

    /**
     * Thêm một điểm mới với mốc thời gian chỉ định (thường dùng khi nạp lịch sử thầu cũ).
     */
    public void addBidPoint(String timestamp, double price) {
        XYChart.Data<String, Number> point = new XYChart.Data<>(timestamp, price);
        series.getData().add(point);

        // Cập nhật giá thầu nhỏ nhất/lớn nhất để điều chỉnh trục Y hợp lý
        if (series.getData().size() == 1) {
            minBid = maxBid = price;
        } else {
            if (price < minBid)
                minBid = price;
            if (price > maxBid)
                maxBid = price;
        }

        // Đảm bảo chỉ giữ lại tối đa số lượng điểm thầu theo maxPoints để tối ưu bộ nhớ
        if (series.getData().size() > maxPoints) {
            series.getData().remove(0);
        }

        // Thêm bong bóng thông tin (Tooltip) cho điểm thầu vừa tạo
        addTooltip(point, timestamp, price);

        // Tạo hiệu ứng xuất hiện mượt mà (Fade-in) cho điểm thầu mới vẽ
        if (point.getNode() != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), point.getNode());
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }

        // Làm nổi bật điểm thầu cao nhất hiện tại bằng màu vàng hổ phách sáng
        highlightMaxPoint();
    }

    /**
     * Nạp lại danh sách lịch sử giá đấu trước khi bắt đầu theo dõi thầu thời gian thực.
     */
    public void loadHistory(java.util.List<double[]> history) {
        // history: Mỗi phần tử chứa mốc thời gian dạng Millis và giá đấu
        clear();
        for (double[] entry : history) {
            String ts = LocalTime.ofSecondOfDay((long) (entry[0]) % 86400).format(TIME_FMT);
            addBidPoint(ts, entry[1]);
        }
    }

    // ═════════════════════════════════════════════════════════
    // BONG BÓNG THÔNG TIN ĐIỂM THẦU (TOOLTIP)
    // ═════════════════════════════════════════════════════════
    private void addTooltip(XYChart.Data<String, Number> point, String timestamp, double price) {
        // JavaFX khởi tạo Node bất đồng bộ (async), sử dụng runLater để đảm bảo an toàn tuyệt đối tránh lỗi Null
        javafx.application.Platform.runLater(() -> {
            if (point.getNode() != null) {
                Tooltip tip = new Tooltip(timestamp + "\n" + MONEY_FMT.format((long) price) + " đ");
                tip.setStyle("-fx-background-color: #1A237E; -fx-text-fill: white;"
                        + "-fx-font-size: 12; -fx-background-radius: 6;");
                Tooltip.install(point.getNode(), tip);
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // LÀM NỔI BẬT ĐIỂM THẦU CAO NHẤT (HIGHLIGHT)
    // ═════════════════════════════════════════════════════════
    private void highlightMaxPoint() {
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() == null)
                    continue;
                boolean isMax = d.getYValue().doubleValue() == maxBid;
                d.getNode().setStyle(isMax ? "-fx-background-color: #FFD54F, white; -fx-background-radius: 6;"
                        : "-fx-background-color: #FF7043, white; -fx-background-radius: 4;");
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // PHƯƠNG THỨC TIỆN ÍCH (UTILITIES)
    // ═════════════════════════════════════════════════════════

    /** Xóa toàn bộ dữ liệu đang hiển thị trên biểu đồ */
    public void clear() {
        series.getData().clear();
        minBid = maxBid = 0;
    }

    /** Lấy mức giá thầu cao nhất hiện tại trên biểu đồ */
    public double getMaxBid() {
        return maxBid;
    }

    /** Lấy mức giá thầu thấp nhất hiện tại trên biểu đồ */
    public double getMinBid() {
        return minBid;
    }

    /** Lấy tổng số lượng điểm thầu đang hiển thị trên biểu đồ */
    public int getPointCount() {
        return series.getData().size();
    }

    /** Thay đổi số lượng điểm thầu tối đa được hiển thị (Mặc định: 20) */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    /** Lấy đối tượng chuỗi dữ liệu (Series) để tùy biến thêm nếu cần */
    public XYChart.Series<String, Number> getSeries() {
        return series;
    }
}

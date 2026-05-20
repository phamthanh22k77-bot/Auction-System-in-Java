package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.dao.UserDAO;
import server.models.user.User;
import client.network.ClientSocketManager;
import client.message.PacketMessage;
import client.message.MessageType;
import server.payload.LoginPayload;
import server.payload.LoginResponsePayload;
import javafx.application.Platform;

/**
 * Điều khiển màn hình đăng nhập tài khoản (LoginController).
 * Giao diện tương ứng: Login.fxml
 */
public class LoginController implements Initializable {

    @FXML
    private TextField tname;
    @FXML
    private PasswordField tpass;
    @FXML
    private Button btnCon;
    @FXML
    private Button btnSignUp;
    @FXML
    private Label lblError;
    @FXML
    private ImageView imgBackground;
    @FXML
    private AnchorPane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Tự động kéo dãn kích thước ảnh nền theo kích thước thực tế của cửa sổ hiển thị
        imgBackground.fitWidthProperty().bind(rootPane.widthProperty());
        imgBackground.fitHeightProperty().bind(rootPane.heightProperty());

        // Cho phép người dùng nhấn phím Enter trên ô mật khẩu để đăng nhập nhanh chóng
        tpass.setOnAction(e -> handleLogin());

        // Xóa sạch thông báo lỗi cũ ngay khi người dùng bắt đầu gõ nhập liệu lại
        tname.textProperty().addListener((o, old, nw) -> clearError());
        tpass.textProperty().addListener((o, old, nw) -> clearError());
    }

    // ═════════════════════════════════════════════════════════
    // XỬ LÝ ĐĂNG NHẬP (LOGIN ACTION)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleLogin() {
        String username = tname.getText().trim();
        String password = tpass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin tài khoản và mật khẩu.");
            return;
        }

        // Tự động kết nối tới Server thông qua Socket nếu phát hiện kết nối chưa sẵn sàng
        ClientSocketManager socketManager = ClientSocketManager.getInstance();
        if (!socketManager.isConnected()) {
            if (!socketManager.connect("localhost", 9090)) {
                showError("Không thể kết nối tới Server. Vui lòng bật Server hoặc kiểm tra kết nối mạng!");
                return;
            }
        }

        // Gửi yêu cầu đăng nhập lên Server
        socketManager.sendPacket(new PacketMessage(MessageType.LOGIN, new LoginPayload(username, password)));

        // Đăng ký bộ lắng nghe phản hồi gói tin kết quả đăng nhập từ Server
        socketManager.addMessageListener(new java.util.function.Consumer<PacketMessage>() {
            @Override
            public void accept(PacketMessage msg) {
                if (msg.getType() == MessageType.LOGIN_SUCCESS) {
                    socketManager.removeMessageListener(this);
                    LoginResponsePayload res = (LoginResponsePayload) msg.getPayload();
                    Platform.runLater(() -> {
                        SessionManager.getInstance().setCurrentUser(res.getUser());
                        navigateByRole(res.getUser().getRole());
                    });
                } else if (msg.getType() == MessageType.LOGIN_FAILURE) {
                    socketManager.removeMessageListener(this);
                    LoginResponsePayload res = (LoginResponsePayload) msg.getPayload();
                    Platform.runLater(() -> showError(res.getMessage()));
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG MÀN HÌNH THEO VAI TRÒ (ROUTING & NAVIGATION)
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở màn hình đăng ký tài khoản.");
        }
    }

    private void navigateByRole(String role) {
        // Vai trò (Role) được lưu trữ trong tệp JSON dưới dạng chữ HOA: BIDDER, SELLER, ADMIN
        String fxmlPath = switch (role.toUpperCase()) {
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN" -> "/client/views/AdminDashboard.fxml";
            default -> null;
        };

        if (fxmlPath == null) {
            showError("Vai trò của tài khoản không xác định trên hệ thống.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnCon.getScene().getWindow();

            // Tự động phân tách kích thước cửa sổ chuẩn tối ưu riêng cho từng giao diện vai trò
            switch (role.toUpperCase()) {
                case "BIDDER" -> {
                    stage.setWidth(1200);
                    stage.setHeight(750);
                }
                case "SELLER" -> {
                    stage.setWidth(1100);
                    stage.setHeight(700);
                }
                case "ADMIN" -> {
                    stage.setWidth(1200);
                    stage.setHeight(750);
                }
            }

            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể tải giao diện màn hình. Vui lòng kiểm tra lại đường dẫn FXML.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // PHƯƠNG THỨC TRỢ GIÚP (HELPERS)
    // ═════════════════════════════════════════════════════════
    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
        }
    }

    private void clearError() {
        if (lblError != null) {
            lblError.setText("");
        }
    }
}

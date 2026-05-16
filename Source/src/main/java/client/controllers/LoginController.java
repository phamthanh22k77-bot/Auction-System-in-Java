package client.controllers;

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
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
import server.models.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
        // Bind ảnh nền theo kích thước cửa sổ
        imgBackground.fitWidthProperty().bind(rootPane.widthProperty());
        imgBackground.fitHeightProperty().bind(rootPane.heightProperty());

        // Enter trên ô mật khẩu → đăng nhập luôn
        tpass.setOnAction(e -> handleLogin());

        // Xoá thông báo lỗi khi người dùng bắt đầu gõ lại
        tname.textProperty().addListener((o, ov, nv) -> clearError());
        tpass.textProperty().addListener((o, ov, nv) -> clearError());
    }

    // ═══════════════════════════════════════════════════════════════════
    // ĐĂNG NHẬP
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        String username = tname.getText().trim();
        String password = tpass.getText();

        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập.");
            tname.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu.");
            tpass.requestFocus();
            return;
        }

        // Gọi server trên thread riêng — không block JavaFX UI thread
        setFormDisabled(true);
        new Thread(() -> {
            try {
                ClientSocketManager csm = ClientSocketManager.getInstance();
                csm.connect("localhost", 9090);

                String[] credentials = { username, password };
                csm.sendMessage(new PacketMessage(MessageType.LOGIN_REQUEST, credentials));
                PacketMessage response = csm.receiveMessage();

                if (response != null && response.getType() == MessageType.AUTH_SUCCESS) {
                    User user = (User) response.getPayload();
                    Platform.runLater(() -> {
                        SessionManager.getInstance().setCurrentUser(user);
                        navigateByRole(user.getRole());
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Sai tên đăng nhập hoặc mật khẩu.");
                        tpass.clear();
                        tpass.requestFocus();
                        setFormDisabled(false);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Không thể kết nối đến máy chủ.");
                    setFormDisabled(false);
                });
            }
        }, "login-thread").start();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleSignUp() {
        try {
            Parent root = new FXMLLoader(getClass().getResource("/client/views/SignUp.fxml")).load();
            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Không thể mở màn hình đăng ký.");
        }
    }

    private void navigateByRole(String role) {
        String path = switch (role.toUpperCase()) {
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN" -> "/client/views/AdminDashboard.fxml";
            default -> null;
        };

        if (path == null) {
            showError("Vai trò không xác định: " + role);
            setFormDisabled(false);
            return;
        }

        try {
            Parent root = new FXMLLoader(getClass().getResource(path)).load();
            Stage stage = (Stage) btnCon.getScene().getWindow();
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
            showError("Không thể tải màn hình. Kiểm tra đường dẫn FXML.");
            setFormDisabled(false);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private void showError(String msg) {
        if (lblError != null)
            lblError.setText(msg);
    }

    private void clearError() {
        if (lblError != null)
            lblError.setText("");
    }

    private void setFormDisabled(boolean off) {
        tname.setDisable(off);
        tpass.setDisable(off);
        btnCon.setDisable(off);
        btnSignUp.setDisable(off);
        if (off)
            showError("Đang kết nối server…");
    }
}
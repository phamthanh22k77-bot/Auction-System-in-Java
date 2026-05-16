package client.controllers;

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import server.payload.ErrorMessagePayload;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField tname;
    @FXML private PasswordField tpass;
    @FXML private Button btnCon;
    @FXML private Button btnSignUp;
    @FXML private Label lblError;
    @FXML private ImageView imgBackground;
    @FXML private AnchorPane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bind ảnh nền theo kích thước cửa sổ
        imgBackground.fitWidthProperty().bind(rootPane.widthProperty());
        imgBackground.fitHeightProperty().bind(rootPane.heightProperty());

        // Enter trên ô mật khẩu → đăng nhập luôn
        tpass.setOnAction(e -> handleLogin());

        // Xoá thông báo lỗi khi người dùng bắt đầu gõ lại
        tname.textProperty().addListener((o, old, nw) -> clearError());
        tpass.textProperty().addListener((o, old, nw) -> clearError());
    }

    // ═══════════════════════════════════════════════════════════
    // ĐĂNG NHẬP
    // ═══════════════════════════════════════════════════════════

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

        // Disable form trong lúc chờ
        setFormDisabled(true);
        showError("Đang kết nối...");

        // Chạy network call trên background thread - KHÔNG block JavaFX thread
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authenticate(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            setFormDisabled(false);

            if (user == null) {
                // Lỗi có thể đã được hiển thị qua authenticate (ErrorMessagePayload)
                if (lblError.getText().equals("Đang kết nối...")) {
                    showError("Sai tên đăng nhập hoặc mật khẩu.");
                }
                tpass.clear();
                tpass.requestFocus();
                return;
            }

            LOGGER.info("Đăng nhập thành công: " + user.getUsername() + " [" + user.getRole() + "]");
            SessionManager.getInstance().setCurrentUser(user);
            navigateByRole(user.getRole());
        });

        loginTask.setOnFailed(event -> {
            setFormDisabled(false);
            Throwable ex = loginTask.getException();
            LOGGER.log(Level.SEVERE, "Login task thất bại", ex);
            showError("Lỗi kết nối: " + (ex != null ? ex.getMessage() : "không rõ"));
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    private User authenticate(String username, String password) throws Exception {
        ClientSocketManager csm = ClientSocketManager.getInstance();
        csm.connect("localhost", 9090);

        String[] credentials = {username, password};
        PacketMessage request = new PacketMessage(MessageType.LOGIN_REQUEST, credentials);
        csm.sendMessage(request);
        LOGGER.info("Gửi " + MessageType.LOGIN_REQUEST + " cho user: " + username);

        PacketMessage response = csm.receiveMessage();
        LOGGER.info("Nhận phản hồi: " + (response != null ? response.getType() : "null"));

        if (response == null) {
            throw new Exception("Server không phản hồi.");
        }

        if (response.getType() == MessageType.AUTH_SUCCESS) {
            return (User) response.getPayload();
        }

        if (response.getType() == MessageType.ERROR) {
            ErrorMessagePayload err = (ErrorMessagePayload) response.getPayload();
            Platform.runLater(() -> showError(err.getErrorMessage()));
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG
    // ═══════════════════════════════════════════════════════════

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Không thể mở màn hình đăng ký", e);
            showError("Không thể mở màn hình đăng ký.");
        }
    }

    private void navigateByRole(String role) {
        LOGGER.info("Điều hướng theo vai trò: '" + role + "'");

        String fxmlPath = switch (role.toUpperCase()) {
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN" -> "/client/views/AdminDashboard.fxml";
            default -> null;
        };

        if (fxmlPath == null) {
            LOGGER.severe("Vai trò không xác định: '" + role + "'");
            showError("Vai trò không hợp lệ: " + role);
            return;
        }

        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                LOGGER.severe("Không tìm thấy FXML: " + fxmlPath);
                showError("Không tìm thấy file giao diện: " + fxmlPath);
                return;
            }

            LOGGER.info("Đang nạp FXML: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) btnCon.getScene().getWindow();
            switch (role.toUpperCase()) {
                case "BIDDER", "ADMIN" -> {
                    stage.setWidth(1200);
                    stage.setHeight(750);
                }
                case "SELLER" -> {
                    stage.setWidth(1100);
                    stage.setHeight(700);
                }
            }
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            LOGGER.info("Điều hướng thành công -> " + fxmlPath);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi nạp FXML: " + fxmlPath, e);
            showError("Lỗi tải màn hình: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private void showError(String msg) {
        if (lblError != null)
            Platform.runLater(() -> lblError.setText(msg));
    }

    private void clearError() {
        if (lblError != null)
            lblError.setText("");
    }

    private void setFormDisabled(boolean off) {
        Platform.runLater(() -> {
            tname.setDisable(off);
            tpass.setDisable(off);
            btnCon.setDisable(off);
            btnSignUp.setDisable(off);
        });
    }
}
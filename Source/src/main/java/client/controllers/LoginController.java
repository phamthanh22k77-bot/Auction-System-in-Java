package client.controllers;

<<<<<<< Updated upstream
import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
=======
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
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
=======

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import server.models.user.User;
import server.payload.ErrorMessagePayload;

public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField     tname;
    @FXML private PasswordField tpass;
    @FXML private Button        btnCon;
    @FXML private Button        btnSignUp;
    @FXML private Label         lblError;
    @FXML private ImageView     imgBackground;
    @FXML private AnchorPane    rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        imgBackground.fitWidthProperty().bind(rootPane.widthProperty());
        imgBackground.fitHeightProperty().bind(rootPane.heightProperty());
        tpass.setOnAction(e -> handleLogin());
        tname.textProperty().addListener((o, old, nw) -> clearError());
        tpass.textProperty().addListener((o, old, nw) -> clearError());
    }

    // ═══════════════════════════════════════════════════════════
    // DANG NHAP
    // ═══════════════════════════════════════════════════════════
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
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
=======
        // Disable nút trong lúc chờ để tránh double-click
        btnCon.setDisable(true);
        showError("Dang ket noi...");

        // Chạy network call trên background thread - KHÔNG block JavaFX thread
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authenticate(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            btnCon.setDisable(false);

            if (user == null) {
                showError("Sai ten dang nhap hoac mat khau.");
                tpass.clear();
                tpass.requestFocus();
                return;
            }

            LOGGER.info("Dang nhap thanh cong: " + user.getUsername() + " [" + user.getRole() + "]");
            SessionManager.getInstance().setCurrentUser(user);
            navigateByRole(user.getRole());
        });

        loginTask.setOnFailed(event -> {
            btnCon.setDisable(false);
            Throwable ex = loginTask.getException();
            LOGGER.log(Level.SEVERE, "Login task that bai", ex);
            showError("Loi ket noi: " + (ex != null ? ex.getMessage() : "unknown"));
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    // ═══════════════════════════════════════════════════════════
    // XAC THUC — goi qua socket toi Server (chay tren background thread)
    // ═══════════════════════════════════════════════════════════

    private User authenticate(String username, String password) throws Exception {
        ClientSocketManager csm = ClientSocketManager.getInstance();
        csm.connect("localhost", 9090);

        String[] credentials = {username, password};
        PacketMessage request = new PacketMessage(MessageType.LOGIN_REQUEST, credentials);
        csm.sendMessage(request);
        LOGGER.info("Gui " + MessageType.LOGIN_REQUEST + " cho user: " + username);

        PacketMessage response = csm.receiveMessage();
        LOGGER.info("Nhan phan hoi: " + (response != null ? response.getType() : "null"));

        if (response == null) {
            throw new Exception("Server khong phan hoi.");
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
    // DIEU HUONG
    // ═══════════════════════════════════════════════════════════
>>>>>>> Stashed changes

    @FXML
    private void handleSignUp() {
        try {
<<<<<<< Updated upstream
            Parent root = new FXMLLoader(getClass().getResource("/client/views/SignUp.fxml")).load();
=======
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/SignUp.fxml"));
            Parent root = loader.load();
>>>>>>> Stashed changes
            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
<<<<<<< Updated upstream
            showError("Không thể mở màn hình đăng ký.");
=======
            LOGGER.log(Level.SEVERE, "Khong the mo man hinh dang ky", e);
            showError("Khong the mo man hinh dang ky.");
>>>>>>> Stashed changes
        }
    }

    private void navigateByRole(String role) {
<<<<<<< Updated upstream
        String path = switch (role.toUpperCase()) {
=======
        LOGGER.info("Chuyen huong theo vai tro: '" + role + "'");

        String fxmlPath = switch (role.toUpperCase()) {
>>>>>>> Stashed changes
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN" -> "/client/views/AdminDashboard.fxml";
            default -> null;
        };

<<<<<<< Updated upstream
        if (path == null) {
            showError("Vai trò không xác định: " + role);
            setFormDisabled(false);
=======
        if (fxmlPath == null) {
            LOGGER.severe("Vai tro khong xac dinh: '" + role + "'");
            showError("Vai tro khong hop le: " + role);
>>>>>>> Stashed changes
            return;
        }

        try {
<<<<<<< Updated upstream
            Parent root = new FXMLLoader(getClass().getResource(path)).load();
=======
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                LOGGER.severe("Khong tim thay FXML: " + fxmlPath);
                showError("Khong tim thay file giao dien: " + fxmlPath);
                return;
            }

            LOGGER.info("Dang nap FXML: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        } catch (IOException e) {
            showError("Không thể tải màn hình. Kiểm tra đường dẫn FXML.");
            setFormDisabled(false);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────
=======
            LOGGER.info("Chuyen huong thanh cong -> " + fxmlPath);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Loi khi nap FXML: " + fxmlPath, e);
            showError("Loi tai man hinh: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════
>>>>>>> Stashed changes

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
package client.controllers;

import client.message.MessageType;
import client.message.PacketMessage;
import client.network.ClientSocketManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField tUsername;
    // tEmail đã bị XOÁ — không khai báo, không dùng
    @FXML
    private PasswordField tPass;
    @FXML
    private PasswordField tConfirm;
    @FXML
    private ComboBox<String> cmbRole;
    @FXML
    private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbRole.setItems(FXCollections.observableArrayList("Bidder", "Seller"));

        // Tự xoá lỗi khi người dùng bắt đầu gõ lại
        tUsername.textProperty().addListener((o, ov, nv) -> clearError());
        tPass.textProperty().addListener((o, ov, nv) -> clearError());
        tConfirm.textProperty().addListener((o, ov, nv) -> clearError());
    }

    // ═══════════════════════════════════════════════════════════════════
    // ĐĂNG KÝ
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleSignUp() {
        String username = tUsername.getText().trim();
        String pass = tPass.getText();
        String confirm = tConfirm.getText();
        String role = cmbRole.getValue();

        // ── Validate ────────────────────────────────────────────────────
        if (username.isEmpty() || pass.isEmpty() || role == null) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if (username.length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự.");
            return;
        }
        if (pass.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp!");
            tConfirm.clear();
            return;
        }

        // ── Tạo User object — email để trống vì đã xoá ô nhập ──────────
        User newUser = role.equalsIgnoreCase("Bidder")
                ? new Bidder(username, "", pass, 0.0)
                : new Seller(username, "", pass, "");

        // ── Gửi lên server trên thread riêng (không block UI) ───────────
        setFormDisabled(true);
        new Thread(() -> {
            try {
                ClientSocketManager csm = ClientSocketManager.getInstance();
                csm.connect("localhost", 9090);

                csm.sendMessage(new PacketMessage(MessageType.SIGNUP_REQUEST, newUser));
                PacketMessage response = csm.receiveMessage();

                if (response != null && response.getType() == MessageType.AUTH_SUCCESS) {
                    // Server trả về User đã được lưu (có ID); nếu không thì dùng newUser
                    User saved = (response.getPayload() instanceof User u) ? u : newUser;
                    Platform.runLater(() -> {
                        SessionManager.getInstance().setCurrentUser(saved);
                        navigateByRole(role);
                    });
                } else {
                    String msg = (response != null && response.getPayload() != null)
                            ? response.getPayload().toString()
                            : "Không thể tạo tài khoản!";
                    Platform.runLater(() -> {
                        showError(msg);
                        setFormDisabled(false);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Lỗi mạng, không thể kết nối đến Server!");
                    setFormDisabled(false);
                });
            }
        }, "signup-thread").start();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG
    // ═══════════════════════════════════════════════════════════════════

    private void navigateByRole(String role) {
        String path = role.equalsIgnoreCase("Bidder")
                ? "/client/views/BidderDashboard.fxml"
                : "/client/views/SellerDashboard.fxml";
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (role.equalsIgnoreCase("Bidder")) {
                stage.setWidth(1200);
                stage.setHeight(750);
            } else {
                stage.setWidth(1100);
                stage.setHeight(700);
            }
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Không thể tải Dashboard: " + e.getMessage());
            setFormDisabled(false);
        }
    }

    @FXML
    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Lỗi khi quay về Login!");
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
        tUsername.setDisable(off);
        tPass.setDisable(off);
        tConfirm.setDisable(off);
        cmbRole.setDisable(off);
        if (off)
            showError("Đang kết nối server…");
    }
}
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

/**
 * SignUpController
 * FXML: SignUp.fxml
 *
 * Điều khiển màn hình đăng ký tài khoản (Sign Up Hub):
 * - Hỗ trợ đăng ký đa vai trò (Bidder / Seller).
 * - Kết nối Socket bất đồng bộ bảo vệ Thread UI an toàn.
 * - Tự động định hướng tới Dashboard tương ứng sau khi đăng ký thành công.
 */
public class SignUpController implements Initializable {

    // ════════════════════════════════════════════════════════
    // CẤU HÌNH MẠNG & THÔNG SỐ HỆ THỐNG
    // ════════════════════════════════════════════════════════
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9090;

    // ════════════════════════════════════════════════════════
    // FXML COMPONENTS
    // ════════════════════════════════════════════════════════
    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField tUsername;
    @FXML
    private PasswordField tPass;
    @FXML
    private PasswordField tConfirm;
    @FXML
    private ComboBox<String> cmbRole;
    @FXML
    private Label lblError;

    // ════════════════════════════════════════════════════════
    // KHỞI TẠO (INITIALIZE)
    // ════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbRole.setItems(FXCollections.observableArrayList("Bidder", "Seller"));

        // Lắng nghe sự kiện để tự động xóa thông báo lỗi khi người dùng gõ lại
        tUsername.textProperty().addListener((o, ov, nv) -> clearError());
        tPass.textProperty().addListener((o, ov, nv) -> clearError());
        tConfirm.textProperty().addListener((o, ov, nv) -> clearError());
    }

    // ════════════════════════════════════════════════════════
    // THAO TÁC XỬ LÝ ĐĂNG KÝ (SIGN UP ACTION)
    // ════════════════════════════════════════════════════════
    @FXML
    private void handleSignUp() {
        String username = tUsername.getText().trim();
        String pass = tPass.getText();
        String confirm = tConfirm.getText();
        String role = cmbRole.getValue();

        // ── Kiểm tra hợp lệ dữ liệu (Form Validation) ──────────
        if (username.isEmpty() || pass.isEmpty() || role == null) {
            showError("Vui lòng điền đầy đủ thông tin đăng ký!");
            return;
        }
        if (username.length() < 3) {
            showError("Tên đăng nhập tối thiểu phải từ 3 ký tự trở lên.");
            return;
        }
        if (pass.length() < 3) {
            showError("Mật khẩu bảo mật phải có ít nhất 3 ký tự.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp! Vui lòng kiểm tra lại.");
            tConfirm.clear();
            return;
        }

        // ── Gửi yêu cầu đăng ký lên Server ở luồng riêng (Tránh treo UI Thread) ──
        setFormDisabled(true);
        new Thread(() -> {
            try {
                ClientSocketManager csm = ClientSocketManager.getInstance();

                // Thiết lập kết nối Socket, ném lỗi ngay nếu Server offline
                boolean connected = csm.connect(DEFAULT_HOST, DEFAULT_PORT);
                if (!connected) {
                    throw new IOException("Không thể thiết lập kết nối tới máy chủ.");
                }

                // Truyền gói tin đăng ký tài khoản mới lên Server
                csm.sendPacket(new PacketMessage(MessageType.REGISTER,
                        new server.payload.RegisterPayload(username, "", pass, role)));

                // Lắng nghe gói phản hồi bất đồng bộ từ Server qua mạng
                csm.addMessageListener(new java.util.function.Consumer<PacketMessage>() {
                    @Override
                    public void accept(PacketMessage response) {
                        if (response.getType() == MessageType.REGISTER_SUCCESS) {
                            csm.removeMessageListener(this);
                            server.payload.LoginResponsePayload res =
                                    (server.payload.LoginResponsePayload) response.getPayload();

                            // Cập nhật phiên người dùng an toàn trên luồng UI
                            Platform.runLater(() -> {
                                SessionManager.getInstance().setCurrentUser(res.getUser());
                                navigateByRole(role);
                            });
                        } else if (response.getType() == MessageType.REGISTER_FAILURE) {
                            csm.removeMessageListener(this);
                            server.payload.LoginResponsePayload res =
                                    (server.payload.LoginResponsePayload) response.getPayload();

                            Platform.runLater(() -> {
                                showError(res.getMessage());
                                setFormDisabled(false);
                            });
                        }
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Lỗi kết nối mạng: Không thể kết nối tới Server!");
                    setFormDisabled(false);
                });
            }
        }, "signup-thread").start();
    }

    // ════════════════════════════════════════════════════════
    // ĐIỀU HƯỚNG MÀN HÌNH (NAVIGATION)
    // ════════════════════════════════════════════════════════
    private void navigateByRole(String role) {
        String path = role.equalsIgnoreCase("Bidder") ? "/client/views/BidderDashboard.fxml"
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
            showError("Không thể tải bảng điều khiển: " + e.getMessage());
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
            showError("Lỗi hệ thống: Không thể quay trở về trang đăng nhập!");
        }
    }

    // ════════════════════════════════════════════════════════
    // HÀM BỔ TRỢ TIỆN ÍCH (HELPERS)
    // ════════════════════════════════════════════════════════
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

    private void setFormDisabled(boolean off) {
        tUsername.setDisable(off);
        tPass.setDisable(off);
        tConfirm.setDisable(off);
        cmbRole.setDisable(off);
        if (off) {
            showError("Đang thiết lập kết nối tới server…");
        }
    }
}

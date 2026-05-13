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

public class LoginController implements Initializable {

    @FXML private TextField     tname;
    @FXML private PasswordField tpass;
    @FXML private Button        btnCon;
    @FXML private Button        btnSignUp;
    @FXML private Label         lblError;
    @FXML private ImageView     imgBackground;
    @FXML private AnchorPane    rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bind anh nen theo kich thuoc cua so
        imgBackground.fitWidthProperty().bind(rootPane.widthProperty());
        imgBackground.fitHeightProperty().bind(rootPane.heightProperty());

        // Cho phep bam Enter tren o mat khau de dang nhap
        tpass.setOnAction(e -> handleLogin());

        // Xoa thong bao loi khi nguoi dung bat dau go lai
        tname.textProperty().addListener((o, old, nw) -> clearError());
        tpass.textProperty().addListener((o, old, nw) -> clearError());
    }

    // ═════════════════════════════════════════════════════════
    // DANG NHAP
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        String username = tname.getText().trim();
        String password = tpass.getText();

        // Validate trong
        if (username.isEmpty()) {
            showError("Vui long nhap ten dang nhap.");
            tname.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui long nhap mat khau.");
            tpass.requestFocus();
            return;
        }

        // Xac thuc & tao User object
        // TODO: thay bang goi server thuc te:
        //   User user = networkClient.login(username, password);
        //   if (user == null) { showError("Sai tai khoan hoac mat khau."); return; }
        User user = authenticate(username, password);

        if (user == null) {
            showError("Sai ten dang nhap hoac mat khau.");
            tpass.clear();
            tpass.requestFocus();
            return;
        }

        // Luu vao SessionManager de cac Controller sau dung chung
        SessionManager.getInstance().setCurrentUser(user);

        // Dieu huong theo vai tro
        navigateByRole(user.getRole());
    }

    // ═════════════════════════════════════════════════════════
    // XAC THUC — doc tu users.json qua UserDAO
    // ═════════════════════════════════════════════════════════

    /**
     * Kiem tra username + password voi du lieu trong users.json.
     * Tra ve User neu hop le, null neu sai.
     *
     * TODO: khi co server, xoa ham nay va goi network.login() thay the.
     */
    private User authenticate(String username, String password) {
        try {
            List<User> users = new UserDAO().loadAll();
            return users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(username)
                              && u.getPassword().equals(password))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            showError("Khong the doc du lieu nguoi dung.");
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════
    // DIEU HUONG
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/views/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Khong the mo man hinh dang ky.");
        }
    }

    private void navigateByRole(String role) {
        // Role trong JSON la chữ HOA: BIDDER, SELLER, ADMIN
        String fxmlPath = switch (role.toUpperCase()) {
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN"  -> "/client/views/AdminDashboard.fxml";
            default       -> null;
        };

        if (fxmlPath == null) {
            showError("Vai tro khong xac dinh.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnCon.getScene().getWindow();

            switch (role.toUpperCase()) {
                case "BIDDER" -> { stage.setWidth(1200); stage.setHeight(750); }
                case "SELLER" -> { stage.setWidth(1100); stage.setHeight(700); }
                case "ADMIN"  -> { stage.setWidth(1200); stage.setHeight(750); }
            }

            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Khong the tai man hinh. Kiem tra duong dan FXML.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // HELPER
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
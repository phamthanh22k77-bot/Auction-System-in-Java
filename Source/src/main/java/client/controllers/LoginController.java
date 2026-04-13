package client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController
 * FXML: Login.fxml
 *
 * Xu ly dang nhap va dieu huong theo vai tro:
 *   Bidder  -> BidderDashboard.fxml
 *   Seller  -> SellerDashboard.fxml
 *   Admin   -> AdminDashboard.fxml
 *
 * fx:id giu nguyen theo Login.fxml cu:
 *   tname   -> TextField username
 *   tpass   -> PasswordField password
 *   btnCon  -> Button dang nhap
 * Them 1 Label lblError de hien thong bao loi
 * (them fx:id="lblError" vao Login.fxml neu chua co)
 */
public class LoginController implements Initializable {

    @FXML private TextField     tname;
    @FXML private PasswordField tpass;
    @FXML private Button        btnCon;

    // Them dong nay vao Login.fxml:
    // <Label fx:id="lblError" text="" style="-fx-text-fill: #C62828; -fx-font-size: 12;"/>
    @FXML private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        // Xac thuc & lay vai tro
        // TODO: thay bang goi server thuc te:
        //   String role = networkClient.login(username, password);
        //   if (role == null) { showError("Sai tai khoan hoac mat khau."); return; }
        String role = mockAuthenticate(username, password);

        if (role == null) {
            showError("Sai ten dang nhap hoac mat khau.");
            tpass.clear();
            tpass.requestFocus();
            return;
        }

        // Dieu huong theo vai tro
        navigateByRole(role);
    }

    // ═════════════════════════════════════════════════════════
    // XAC THUC (MOCK — thay bang server thuc)
    // ═════════════════════════════════════════════════════════

    /**
     * Gia lap xac thuc.
     * Tai khoan test:
     *   bidder  / 123  -> Bidder
     *   seller  / 123  -> Seller
     *   admin   / 123  -> Admin
     *
     * Khi ket noi server: xoa ham nay, goi network.login() thay the.
     */
    private String mockAuthenticate(String username, String password) {
        if (!password.equals("123")) return null;
        return switch (username.toLowerCase()) {
            case "bidder" -> "Bidder";
            case "seller" -> "Seller";
            case "admin"  -> "Admin";
            default       -> null;
        };
    }

    // ═════════════════════════════════════════════════════════
    // DIEU HUONG
    // ═════════════════════════════════════════════════════════

    private void navigateByRole(String role) {
        String fxmlPath = switch (role) {
            case "Bidder" -> "/client/views/BidderDashboard.fxml";
            case "Seller" -> "/client/views/SellerDashboard.fxml";
            case "Admin"  -> "/client/views/AdminDashboard.fxml";
            default       -> null;
        };

        if (fxmlPath == null) {
            showError("Vai tro khong xac dinh.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Truyen thong tin user sang controller tiep theo (neu can)
            // Vi du voi BidderDashboard:
            // BidderDashboardController ctrl = loader.getController();
            // ctrl.setCurrentUser(username, role);

            Stage stage = (Stage) btnCon.getScene().getWindow();
            Scene scene = new Scene(root);

            // Dat kich thuoc phu hop voi tung vai tro
            switch (role) {
                case "Bidder" -> { stage.setWidth(1200); stage.setHeight(750); }
                case "Seller" -> { stage.setWidth(1100); stage.setHeight(700); }
                case "Admin"  -> { stage.setWidth(1200); stage.setHeight(750); }
            }

            stage.setScene(scene);
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

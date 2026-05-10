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
import server.dao.UserDAO;
import server.models.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * Logic dang nhap:
 *   1. Thu xac thuc bang UserDAO (doc users.json)
 *      - 2 tk mẫu trong users.json mới có vai trò bidder nên tạo thêm tk admin/seller mock giả ở đây, ko ảnh hưởng code phần khác
 *      - User.login() chi check username -> kiem tra email rieng
 *   2. Neu khong doc duoc file -> fallback mock de test
 *   3. Dieu huong theo role: BIDDER / SELLER / ADMIN
 *
 * Tai khoan that (users.json):
 *   ThanhBot   / VNU123@      -> BIDDER
 *   ThanhTop   / DECKQUANTAM  -> BIDDER (email: thanhtop@thanhtopgmai.com)
 *
 * Tai khoan mock (test khi chua co SELLER/ADMIN trong file):
 *   seller / 123 -> SELLER
 *   admin  / 123 -> ADMIN
 */
public class LoginController implements Initializable {

    @FXML private TextField     tname;
    @FXML private PasswordField tpass;
    @FXML private Button        btnCon;
    @FXML private Label         lblError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bam Enter tren o password -> tu dong dang nhap
        tpass.setOnAction(e -> handleLogin());

        // Xoa thong bao loi khi bat dau go lai
        tname.textProperty().addListener((o, old, nw) -> clearError());
        tpass.textProperty().addListener((o, old, nw) -> clearError());
    }

    // ═════════════════════════════════════════════════════════
    // HANDLER CHINH
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        String input    = tname.getText().trim(); // username hoac email
        String password = tpass.getText();

        // Validate trong
        if (input.isEmpty()) {
            showError("Vui long nhap username hoac email.");
            tname.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui long nhap mat khau.");
            tpass.requestFocus();
            return;
        }

        // Thu DAO truoc, neu loi file thi dung mock
        String role = authenticateWithDAO(input, password);
        if (role == null) {
            role = mockAuthenticate(input, password);
        }

        if (role == null) {
            showError("Sai tai khoan hoac mat khau.");
            tpass.clear();
            tpass.requestFocus();
            return;
        }

        navigateByRole(role);
    }

    // ═════════════════════════════════════════════════════════
    // XAC THUC BANG UserDAO
    // ═════════════════════════════════════════════════════════

    /**
     * Doc users.json, tim user khop voi input (username HOAC email) + password.
     *
     * User.login() chi check username -> xu ly email rieng.
     *
     * @return role "BIDDER"/"SELLER"/"ADMIN" neu thanh cong, null neu that bai
     */
    private String authenticateWithDAO(String input, String password) {
        try {
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.loadAll();

            for (User u : users) {
                boolean matched = false;

                // Thu 1: khop username (dung phuong thuc User.login() co san)
                if (u.login(input, password)) {
                    matched = true;
                }

                // Thu 2: khop email (User.login() khong ho tro -> tu kiem tra)
                if (!matched
                        && u.getEmail() != null
                        && u.getEmail().trim().equalsIgnoreCase(input)
                        && u.getPassword().equals(password)) {
                    matched = true;
                }

                if (matched) {
                    SessionManager.getInstance().setCurrentUser(u);
                    return u.getRole(); // "BIDDER" / "SELLER" / "ADMIN"
                }
            }
        } catch (Exception e) {
            // File khong ton tai hoac loi doc -> se dung mock
            System.out.println("[Login] Khong doc duoc users.json: " + e.getMessage());
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════
    // MOCK — fallback khi file chua co du tai khoan
    // ═════════════════════════════════════════════════════════

    /**
     * XOA 2 dong mock nay khi them SELLER/ADMIN vao users.json:
     *   case "seller" -> "SELLER";
     *   case "admin"  -> "ADMIN";
     */
    private String mockAuthenticate(String input, String password) {
        if (!password.equals("123")) return null;
        return switch (input.toLowerCase()) {
            case "seller" -> "SELLER";
            case "admin"  -> "ADMIN";
            default       -> null;
        };
    }

    // ═════════════════════════════════════════════════════════
    // DIEU HUONG
    // ═════════════════════════════════════════════════════════

    private void navigateByRole(String role) {
        String fxmlPath = switch (role) {
            case "BIDDER" -> "/client/views/BidderDashboard.fxml";
            case "SELLER" -> "/client/views/SellerDashboard.fxml";
            case "ADMIN"  -> "/client/views/AdminDashboard.fxml";
            default       -> null;
        };

        if (fxmlPath == null) {
            showError("Vai tro khong xac dinh: " + role);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnCon.getScene().getWindow();
            switch (role) {
                case "BIDDER" -> { stage.setWidth(1280); stage.setHeight(800); }
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
        if (lblError != null) lblError.setText(msg);
    }

    private void clearError() {
        if (lblError != null) lblError.setText("");
    }
}

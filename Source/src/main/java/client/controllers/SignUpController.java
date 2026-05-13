package client.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.dao.UserDAO;
import server.models.user.Bidder;
import server.models.user.Seller;
import server.models.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TextField tUsername, tEmail;
    @FXML private PasswordField tPass, tConfirm;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Them lua chon vai tro
        cmbRole.setItems(FXCollections.observableArrayList("Bidder", "Seller"));
    }

    @FXML
    private void handleSignUp() {
        String user = tUsername.getText().trim();
        String email = tEmail.getText().trim();
        String pass = tPass.getText();
        String confirm = tConfirm.getText();
        String role = cmbRole.getValue();

        // 🟢 Bước 1: Kiểm tra rỗng
        if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || role == null) {
            lblError.setText("Please fill in all fields!");
            return;
        }

        // 🟢 Bước 2: Kiểm tra mật khẩu khớp
        if (!pass.equals(confirm)) {
            lblError.setText("Passwords do not match!");
            return;
        }

        // Bước 3: Tạo User object đúng constructor
        User newUser = role.equals("Bidder")
                ? new Bidder(user, email, pass)
                : new Seller(user, email, pass, "");

        // Luu vao users.json
        try {
            new UserDAO().them(newUser);
        } catch (IOException e) {
            lblError.setText("Khong the luu tai khoan, thu lai!");
            return;
        }

        // 🟢 Bước 4: Tự động đăng nhập và điều hướng
        SessionManager.getInstance().setCurrentUser(newUser);
        navigateByRole(role);
    }

    private void navigateByRole(String role) {
        String fxmlPath = role.equalsIgnoreCase("Bidder")
                ? "/client/views/BidderDashboard.fxml"
                : "/client/views/SellerDashboard.fxml";
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) rootPane.getScene().getWindow();

            if (role.equalsIgnoreCase("Bidder")) { stage.setWidth(1200); stage.setHeight(750); }
            else                                 { stage.setWidth(1100); stage.setHeight(700); }

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            lblError.setText("Error loading Dashboard!");
        }
    }

    @FXML
    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/views/Login.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            lblError.setText("Error returning to Login!");
        }
    }
}
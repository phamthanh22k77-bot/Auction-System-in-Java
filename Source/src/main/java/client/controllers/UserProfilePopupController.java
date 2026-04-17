package client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * UserProfilePopupController
 * FXML: UserProfilePopup.fxml
 *
 * Duoc mo tu BidderDashboardController khi bam nut "User":
 *     Stage popup = new Stage();
 *     FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/UserProfilePopup.fxml"));
 *     Parent root = loader.load();
 *     UserProfilePopupController ctrl = loader.getController();
 *     ctrl.setUserInfo("Nguyen Van A", "vana@email.com", 25, 5_000_000);
 *     popup.initOwner(mainStage);
 *     popup.initStyle(StageStyle.UNDECORATED);
 *     popup.setScene(new Scene(root));
 *     popup.show();
 */
public class UserProfilePopupController {

    @FXML private Label userNameLabel;
    @FXML private Label userRankLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userOrdersCount;
    @FXML private Label userBalanceLabel;

    // Giu tham chieu Stage chinh de dong popup va quay lai Login
    private Stage ownerStage;

    // ═════════════════════════════════════════════════════════
    // NAP THONG TIN USER
    // ═════════════════════════════════════════════════════════

    /**
     * Goi tu BidderDashboardController sau loader.load().
     */
    public void setUserInfo(String name, String email,
                            int ordersCount, double balance) {
        userNameLabel  .setText(name);
        userEmailLabel .setText(email);
        userOrdersCount.setText(ordersCount + " don");
        userBalanceLabel.setText(String.format("%,.0f d", balance));

        // Tinh hang theo so don
        String rank = ordersCount >= 50 ? "Vang" :
                      ordersCount >= 20 ? "Bac" : "Dong";
        userRankLabel.setText("Hang: " + rank);
    }

    /** Giu tham chieu stage chinh (de dong popup khi dang xuat) */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    // ═════════════════════════════════════════════════════════
    // HANDLERS
    // ═════════════════════════════════════════════════════════

    @FXML
    private void handleBidHistory() {
        // Dong popup
        closePopup();
        // TODO: navigate to BidHistory screen
        System.out.println("Mo lich su dau gia");
    }

    @FXML
    private void handleLogout() {
        closePopup();
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/client/views/Login.fxml"));

            // Lay stage chinh de chuyen ve Login
            Stage stage = ownerStage != null
                ? ownerStage
                : new Stage();
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closePopup() {
        // Dong cua so popup (UserProfilePopup la Stage rieng)
        Stage popupStage = (Stage) userNameLabel.getScene().getWindow();
        popupStage.close();
    }
}

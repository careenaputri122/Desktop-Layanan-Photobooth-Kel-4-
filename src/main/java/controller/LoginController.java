package controller;

import dao.UserDAO;
import model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email dan password harus diisi.");
            return;
        }

        User user = UserDAO.getInstance().login(email, password);
        if (user != null) {
            try {
                SceneManager.closeCurrentPopup();

                if ("admin".equalsIgnoreCase(user.getRole())) {
                    // ── ADMIN → ke Admin Dashboard ──
                    SceneManager.showAdminDashboard();

                } else if ("pemesanan".equals(SceneManager.afterLoginDestination)) {
                    // ── Pelanggan dari alur pesan → lanjut pemesanan ──
                    SceneManager.afterLoginDestination = null;
                    SceneManager.showPemesanan();

                } else {
                    // ── Pelanggan biasa → home ──
                    SceneManager.showHome();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showError("Email atau password salah.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            SceneManager.closeCurrentPopup();
            SceneManager.showRegister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.getStyleClass().removeAll("label-success");
        if (!errorLabel.getStyleClass().contains("label-error"))
            errorLabel.getStyleClass().add("label-error");
        errorLabel.setText(msg);
    }
}
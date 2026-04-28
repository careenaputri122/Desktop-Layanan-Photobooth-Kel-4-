package controller;

import dao.UserDAO;
import model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email dan password harus diisi.");
            return;
        }

        User user = UserDAO.getInstance().login(email, password);
        if (user != null) {
            try {
                SceneManager.closeCurrentPopup(); // tutup popup login
                SceneManager.showHome();          // navigasi ke home
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
            SceneManager.closeCurrentPopup(); // tutup popup login
            SceneManager.showRegister();      // buka popup register
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.getStyleClass().removeAll("label-success");
        if (!errorLabel.getStyleClass().contains("label-error")) {
            errorLabel.getStyleClass().add("label-error");
        }
        errorLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        errorLabel.getStyleClass().removeAll("label-error");
        if (!errorLabel.getStyleClass().contains("label-success")) {
            errorLabel.getStyleClass().add("label-success");
        }
        errorLabel.setText(msg);
    }
}
package controller;

import dao.UserDAO;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class RegisterController {

    @FXML private TextField     namaDepanField;
    @FXML private TextField     namaBelakangField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;

    @FXML
    private void handleRegister() {
        String namaDepan       = namaDepanField.getText().trim();
        String namaBelakang    = namaBelakangField.getText().trim();
        String email           = emailField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validasi tidak boleh kosong
        if (namaDepan.isEmpty() || namaBelakang.isEmpty()
                || email.isEmpty() || password.isEmpty()) {
            showError("Semua field wajib diisi.");
            return;
        }

        // Validasi format email
        if (!email.contains("@") || !email.contains(".")) {
            showError("Format email tidak valid.");
            return;
        }

        // Validasi panjang password
        if (password.length() < 6) {
            showError("Password minimal 6 karakter.");
            return;
        }

        // Validasi konfirmasi password
        if (!password.equals(confirmPassword)) {
            showError("Password dan konfirmasi tidak cocok.");
            return;
        }

        User user    = new User(0, namaDepan, namaBelakang, email, password, "user");
        boolean ok   = UserDAO.getInstance().register(user);

        if (ok) {
            showSuccess("Registrasi berhasil! Silakan login.");
            clearFields();
        } else {
            showError("Email sudah terdaftar.");
        }
    }

    @FXML
    private void goToLogin() {
        try {
            SceneManager.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        namaDepanField.clear();
        namaBelakangField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String msg) {
        errorLabel.getStyleClass().removeAll("label-success");
        if (!errorLabel.getStyleClass().contains("label-error"))
            errorLabel.getStyleClass().add("label-error");
        errorLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        errorLabel.getStyleClass().removeAll("label-error");
        if (!errorLabel.getStyleClass().contains("label-success"))
            errorLabel.getStyleClass().add("label-success");
        errorLabel.setText(msg);
    }
}

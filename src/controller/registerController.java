package controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import dao.UserDAO;

public class registerController {

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfNamaBelakang;

    @FXML
    private TextField tfNamaDepan;

    @FXML
    void handleRegister(ActionEvent event) {
            String namaDepan = tfNamaDepan.getText();
            String namaBelakang = tfNamaBelakang.getText();
            String email = tfEmail.getText();
            String password = pfPassword.getText();
    
            // Validasi input
            if (namaDepan.isEmpty() || namaBelakang.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("Semua field harus diisi!");
                return;
            }
    
            // Simpan data ke database
            try {
                UserDAO.register(namaDepan, namaBelakang, email, password);
                System.out.println("Registrasi berhasil!");
            } catch (Exception e) {
                System.out.println("Gagal registrasi: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }



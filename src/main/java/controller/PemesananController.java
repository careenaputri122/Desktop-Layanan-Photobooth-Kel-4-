package controller;

import dao.BookingDAO;
import dao.PaketDAO;
import dao.UserDAO;
import model.Booking;
import model.Paket;
import model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


public class PemesananController {

    // ── Navbar ────────────────────────────────────────────────────
    @FXML private HBox authBox;

    // ── Step Indicator ────────────────────────────────────────────
    @FXML private Label stepCircle1, stepCircle2, stepCircle3, stepCircle4;
    @FXML private Label stepLabel1,  stepLabel2,  stepLabel3,  stepLabel4;
    @FXML private Label stepLine1,   stepLine2,   stepLine3;

    // ── Panels ────────────────────────────────────────────────────
    @FXML private VBox step1Panel, step2Panel, step3Panel, step4Panel;

    // ── Step 1: Paket ─────────────────────────────────────────────
    @FXML private VBox cardStarter, cardSilver, cardGold, cardDigital;

    // ── Step 2: Tanggal ───────────────────────────────────────────
    @FXML private VBox calendarBox;
    @FXML private TextField jamMulaiField;
    @FXML private TextField lokasiField;

    // ── Step 3: Data Pemesan ──────────────────────────────────────
    @FXML private TextField namaDepanField, emailField, phoneField;
    @FXML private TextArea  catatanField;
    @FXML private Label     errorStep3;

    // ── Step 4: Ringkasan ─────────────────────────────────────────
    @FXML private Label ringPaket, ringTipe, ringTanggal, ringJamMulai, ringLokasi;
    @FXML private Label ringNama, ringContact;
    @FXML private Label payHarga, payDiskon, payTotal; 
    @FXML private HBox  diskonRow;

    // ── State ─────────────────────────────────────────────────────
    private int       currentStep    = 1;
    private String    selectedPaket  = "";
    private String    selectedHarga  = "";
    private String    selectedTipe   = "";
    private LocalDate selectedDate   = null;
    private YearMonth currentMonth   = YearMonth.now();
    private VBox      activePackCard = null;

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupNavbar();
        Platform.runLater(() -> buildCalendar(currentMonth));
    }

    // ── Navbar ────────────────────────────────────────────────────
    private void setupNavbar() {
        User user = UserDAO.getInstance().getCurrentUser();
        authBox.getChildren().clear();
        if (user != null) {
            Label namaLabel = new Label("Hi, " + user.getNamaDepan() + "!");
            namaLabel.setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold;");
            Button btnLogout = new Button("Logout");
            btnLogout.getStyleClass().add("btn-masuk");
            btnLogout.setOnAction(e -> {
                UserDAO.getInstance().logout();
                try { SceneManager.showHome(); } catch (Exception ex) { ex.printStackTrace(); }
            });
            authBox.getChildren().addAll(namaLabel, btnLogout);
        } else {
            Button btnLogin = new Button("Login");
            btnLogin.getStyleClass().add("btn-masuk");
            btnLogin.setOnAction(e -> { try { SceneManager.showLogin(); } catch (Exception ex) { ex.printStackTrace(); } });
            Button btnSignin = new Button("Sign in");
            btnSignin.getStyleClass().add("btn-masuk");
            btnSignin.setOnAction(e -> { try { SceneManager.showRegister(); } catch (Exception ex) { ex.printStackTrace(); } });
            authBox.getChildren().addAll(btnLogin, btnSignin);
        }
    }

    // ── Step 1: Pilih Paket ───────────────────────────────────────
    @FXML private void pilihStarter()  { pilihPaket("Paket Starter",  "Rp1.275.000", "Cetak",       cardStarter); }
    @FXML private void pilihSilver()   { pilihPaket("Paket Silver",   "Rp1.700.000", "Cetak",       cardSilver);  }
    @FXML private void pilihGold()     { pilihPaket("Paket Gold",     "Rp2.337.500", "Cetak",       cardGold);    }
    @FXML private void pilihDigital()  { pilihPaket("Paket Digital",  "Rp1.062.500", "Tanpa Cetak", cardDigital); }

    private void pilihPaket(String nama, String harga, String tipe, VBox card) {
        selectedPaket = nama;
        selectedHarga = harga;
        selectedTipe  = tipe;

        for (VBox c : new VBox[]{cardStarter, cardSilver, cardGold, cardDigital}) {
            c.getStyleClass().removeAll("paket-card-selected");
        }
        card.getStyleClass().add("paket-card-selected");
        activePackCard = card;

        goToStep(2);
    }

    // ── Step 2: Tanggal & Jam Mulai ──────────────────────────────
    @FXML private void backToStep1() { goToStep(1); }
    @FXML private void goToStep3() {
        if (selectedDate == null) { showAlert("Pilih tanggal terlebih dahulu."); return; }

        String jam = jamMulaiField.getText().trim();
        if (jam.isEmpty()) { showAlert("Masukkan jam mulai acara."); return; }
        if (!jam.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            showAlert("Format jam tidak valid. Gunakan HH:mm, contoh: 09:00"); return;
        }
        int jamInt = Integer.parseInt(jam.split(":")[0]);
        if (jamInt < 8 || jamInt >= 20) {
            showAlert("Jam mulai harus antara 08:00 – 20:00 WIB."); return;
        }

        if (lokasiField.getText().trim().isEmpty()) { showAlert("Masukkan lokasi acara."); return; }
        goToStep(3);
    }

    // ── Step 3: Data Pemesan ──────────────────────────────────────
    @FXML private void backToStep2() { goToStep(2); }
    @FXML private void goToStep4() {
        if (namaDepanField.getText().trim().isEmpty()) {
            errorStep3.setText("Nama lengkap harus diisi."); return;
        }
        if (emailField.getText().trim().isEmpty()) {
            errorStep3.setText("Email harus diisi."); return;
        }
        if (phoneField.getText().trim().isEmpty()) {
            errorStep3.setText("No. WhatsApp harus diisi."); return;
        }
        errorStep3.setText("");

        // isi ringkasan
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        ringPaket.setText(selectedPaket);
        ringTipe.setText(selectedTipe);
        ringTanggal.setText(selectedDate.format(fmt));
        ringJamMulai.setText("Jam Mulai: " + jamMulaiField.getText().trim());
        ringLokasi.setText(lokasiField.getText().trim());
        ringNama.setText(namaDepanField.getText().trim());
        ringContact.setText(phoneField.getText().trim() + " • " + emailField.getText().trim());

        // hitung harga & diskon
        int hargaInt = Integer.parseInt(selectedHarga.replace("Rp", "").replace(".", ""));
User currentUser = UserDAO.getInstance().getCurrentUser();

// cek jumlah pesanan user
boolean berhakDiskon = false;
if (currentUser != null) {
    long jumlahPesanan = BookingDAO.getInstance().findAll()
        .stream()
        .filter(b -> b.getUser() != null && b.getUser().getId() == currentUser.getId())
        .count();
    berhakDiskon = jumlahPesanan >= 3;
}

int diskon = berhakDiskon ? (int)(hargaInt * 0.15) : 0;
int total  = hargaInt - diskon;

payHarga.setText(formatRp(hargaInt));

if (berhakDiskon) {
    diskonRow.setVisible(true);
    diskonRow.setManaged(true);
    payDiskon.setText("-" + formatRp(diskon));
} else {
    diskonRow.setVisible(false);
    diskonRow.setManaged(false);
}

payTotal.setText(formatRp(total));

goToStep(4);
       }

       

    // ── Step 4: Konfirmasi ────────────────────────────────────────
    @FXML private void backToStep3() { goToStep(3); }

    @FXML private void konfirmasi() {
        Booking booking = new Booking();
        
//menambahkan logic agar tersimpan ke db booking
// user
User user = UserDAO.getInstance().getCurrentUser();
if (user == null) {
    showAlert("Login dulu!");
    return;
}
booking.setUser(user);

// paket (mapping dari nama)
Paket paket = PaketDAO.getInstance().findAll()
    .stream()
    .filter(p -> p.getNama().equals(selectedPaket))
    .findFirst()
    .orElse(null);

if (paket == null) {
    showAlert("Paket gak ditemukan!");
    return;
}
booking.setPaket(paket);

// isi data
booking.setTanggal(java.sql.Date.valueOf(selectedDate));
booking.setJamMulai(jamMulaiField.getText().trim());
booking.setLokasi(lokasiField.getText().trim());
booking.setNamaPemesan(namaDepanField.getText());
booking.setEmail(emailField.getText());
booking.setPhone(phoneField.getText());
booking.setCatatan(catatanField.getText());

// nomor & status
String nomor = BookingDAO.generateNomorPesanan();
booking.setNomorPesanan(nomor);
booking.setStatus("Menunggu Konfirmasi");

// harga
int harga = Integer.parseInt(selectedHarga.replace("Rp","").replace(".",""));
booking.setTotalHarga(harga);

// SAVE
boolean success = BookingDAO.getInstance().save(booking);

if (!success) {
    showAlert("Gagal simpan!");
    return;
}
        int nomorAcak = (int)(Math.random() * 900) + 100;
        String nomorPesanan = "FTM-2026-" + nomorAcak;

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(420);

        Label ikon = new Label("✅");
        ikon.setStyle("-fx-font-size: 40px;");

        Label judul = new Label("Pesanan Dikonfirmasi!");
        judul.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        Label sub = new Label("Nomor pesanan Anda:");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        Label nomorLabel = new Label(nomorPesanan);
        nomorLabel.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #EC4899;" +
            "-fx-background-color: #fff0f7; -fx-padding: 10 30; -fx-background-radius: 8;"
        );

        Label info = new Label("Tim kami akan menghubungi Anda via WhatsApp\ndalam 1×24 jam untuk konfirmasi DP dan detail event.");
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        info.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btnWa = new Button("🟢  Konfirmasi via WhatsApp");
        btnWa.setMaxWidth(Double.MAX_VALUE);
        btnWa.setStyle(
            "-fx-background-color: #16a34a; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnWa.setOnAction(e -> {
            try {
                String pesan = "Halo Aksaf Photobooth, saya ingin konfirmasi pesanan " + nomorPesanan;
                java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("https://wa.me/6281234567890?text=" + pesan.replace(" ", "+"))
                );
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button btnHome = new Button("Kembali ke Beranda");
        btnHome.setMaxWidth(Double.MAX_VALUE);
        btnHome.setStyle(
            "-fx-background-color: white; -fx-text-fill: #374151;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12 0; -fx-background-radius: 8;" +
            "-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-cursor: hand;"
        );
        btnHome.setOnAction(e -> {
            popup.close();
            try { SceneManager.showHome(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        root.getChildren().addAll(ikon, judul, sub, nomorLabel, info, btnWa, btnHome);
        popup.setScene(new javafx.scene.Scene(root, 460, 420));
        popup.showAndWait();
    }

    // ── Helper ────────────────────────────────────────────────────
    private String formatRp(int amount) {
        return "Rp" + String.format("%,d", amount).replace(",", ".");
    }

    // ── Kalender ──────────────────────────────────────────────────
    private void buildCalendar(YearMonth ym) {
        calendarBox.getChildren().clear();

        DateTimeFormatter headerFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER);
        Button prev = new Button("‹");
        prev.getStyleClass().add("cal-nav-btn");
        prev.setOnAction(e -> { currentMonth = currentMonth.minusMonths(1); buildCalendar(currentMonth); });

        Label monthLabel = new Label(ym.format(headerFmt));
        monthLabel.getStyleClass().add("cal-month-label");
        HBox.setHgrow(monthLabel, Priority.ALWAYS);
        monthLabel.setMaxWidth(Double.MAX_VALUE);
        monthLabel.setAlignment(Pos.CENTER);

        Button next = new Button("›");
        next.getStyleClass().add("cal-nav-btn");
        next.setOnAction(e -> { currentMonth = currentMonth.plusMonths(1); buildCalendar(currentMonth); });

        header.getChildren().addAll(prev, monthLabel, next);
        calendarBox.getChildren().add(header);

        GridPane grid = new GridPane();
        grid.setHgap(4); grid.setVgap(4);
        grid.setAlignment(Pos.CENTER);
        String[] days = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.getStyleClass().add("cal-day-header");
            d.setMinWidth(36); d.setAlignment(Pos.CENTER);
            grid.add(d, i, 0);
        }

        LocalDate first = ym.atDay(1);
        int startCol = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        int col = startCol, row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = ym.atDay(day);
            Button btn = new Button(String.valueOf(day));
            btn.setMinWidth(36); btn.setMinHeight(36);
            btn.setMaxWidth(36); btn.setMaxHeight(36);

            if (date.equals(selectedDate)) {
                btn.getStyleClass().add("cal-day-selected");
            } else if (date.isBefore(LocalDate.now())) {
                btn.getStyleClass().add("cal-day");
                btn.setDisable(true);
            } else {
                btn.getStyleClass().add("cal-day");
            }

            btn.setOnAction(e -> {
                selectedDate = date;
                buildCalendar(currentMonth);
            });

            grid.add(btn, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        calendarBox.getChildren().add(grid);
    }

    // ── Ganti Step ────────────────────────────────────────────────
    private void goToStep(int step) {
        currentStep = step;
        step1Panel.setVisible(step == 1); step1Panel.setManaged(step == 1);
        step2Panel.setVisible(step == 2); step2Panel.setManaged(step == 2);
        step3Panel.setVisible(step == 3); step3Panel.setManaged(step == 3);
        step4Panel.setVisible(step == 4); step4Panel.setManaged(step == 4);
        updateStepIndicator(step);
    }

    private void updateStepIndicator(int step) {
        Label[] circles = {stepCircle1, stepCircle2, stepCircle3, stepCircle4};
        Label[] labels  = {stepLabel1,  stepLabel2,  stepLabel3,  stepLabel4};
        Label[] lines   = {stepLine1,   stepLine2,   stepLine3};
        String[] nums   = {"1", "2", "3", "4"};

        for (int i = 0; i < 4; i++) {
            circles[i].getStyleClass().setAll(
                i + 1 < step  ? "step-circle-done"  :
                i + 1 == step ? "step-circle-active" : "step-circle"
            );
            circles[i].setText(i + 1 < step ? "✓" : nums[i]);
            labels[i].getStyleClass().setAll(i + 1 <= step ? "step-label-active" : "step-label");
        }
        for (int i = 0; i < 3; i++) {
            lines[i].getStyleClass().setAll(i + 1 < step ? "step-line step-line-done" : "step-line");
        }
    }

    // ── Navigasi ──────────────────────────────────────────────────
    @FXML private void goHome()      { nav(() -> SceneManager.showHome());      }
    @FXML private void goPricelist() { nav(() -> SceneManager.showPricelist()); }
    @FXML private void goGaleri()    { nav(() -> SceneManager.showGaleri());    }
    @FXML private void goPemesanan() { nav(() -> SceneManager.showPemesanan()); }
    @FXML private void goLogin()     { nav(() -> SceneManager.showLogin());     }
    @FXML private void goSignin()    { nav(() -> SceneManager.showRegister());  }

    @FunctionalInterface interface Nav { void go() throws Exception; }
    private void nav(Nav fn) { try { fn.go(); } catch (Exception e) { e.printStackTrace(); } }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

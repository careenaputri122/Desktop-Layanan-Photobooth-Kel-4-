package controller;

import dao.BookingDAO;
import dao.BlockedDateDAO;
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
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


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
    @FXML private VBox paketListBox;

    // ── Step 2: Tanggal ───────────────────────────────────────────
    @FXML private VBox calendarBox;
    @FXML private ComboBox<String> jamMulaiField;
    @FXML private Label jamSelesaiInfo;
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
    @FXML private Button btnKonfirmasi;

    // ── State ─────────────────────────────────────────────────────
    private int       currentStep    = 1;
    private Paket     selectedPaket  = null;
    private LocalDate selectedDate   = null;
    private YearMonth currentMonth   = YearMonth.now();
    private VBox      activePackCard = null;
    private int       totalFinal     = 0;
    private boolean   sedangKonfirmasi = false;
    private static final LocalTime JAM_BUKA = LocalTime.of(8, 0);
    private static final LocalTime JAM_TUTUP = LocalTime.of(21, 0);

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupNavbar();
        loadPaketFromDatabase();
        jamMulaiField.valueProperty().addListener((obs, oldVal, newVal) -> updateJamSelesaiInfo());
        updateAvailableTimeSlots();
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
    private void loadPaketFromDatabase() {
        List<Paket> paketList = PaketDAO.getInstance().findAll();
        boolean diskonMemberAktif = UserDAO.getInstance().currentUserHasMemberDiscount();
        paketListBox.getChildren().clear();

        if (paketList.isEmpty()) {
            Label empty = new Label("Belum ada paket yang tersedia.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
            paketListBox.getChildren().add(empty);
            return;
        }

        HBox row = null;
        for (int i = 0; i < paketList.size(); i++) {
            if (i % 2 == 0) {
                row = new HBox(20);
                row.setAlignment(Pos.CENTER);
                paketListBox.getChildren().add(row);
            }

            VBox card = createPaketCard(paketList.get(i), diskonMemberAktif);
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
    }

    private VBox createPaketCard(Paket paket, boolean diskonMemberAktif) {
        VBox card = new VBox(12);
        card.getStyleClass().add("paket-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label nama = new Label(paket.getNama());
        nama.getStyleClass().add("paket-name");

        Label tipe = new Label(paket.getTipe());
        tipe.getStyleClass().add("paket-type");

        HBox hargaRow = new HBox(8);
        hargaRow.setAlignment(Pos.CENTER_LEFT);
        int hargaNormal = paket.getHarga();
        DiscountInfo discountInfo = resolveDiscount(paket, diskonMemberAktif);
        int diskonPersen = discountInfo.percent();
        if (diskonPersen > 0) {
            int hargaDiskon = hargaNormal - (hargaNormal * diskonPersen / 100);

            Label hargaAwal = new Label(formatRp(hargaNormal));
            hargaAwal.getStyleClass().add("paket-price-old");
            Label persen = new Label("-" + diskonPersen + "%");
            persen.getStyleClass().add("paket-discount-badge");
            HBox hargaAwalRow = new HBox(8, hargaAwal, persen);
            hargaAwalRow.setAlignment(Pos.CENTER_LEFT);

            Label hargaAkhir = new Label(formatRp(hargaDiskon));
            hargaAkhir.getStyleClass().add("paket-price");
            Label labelDiskon = new Label(discountInfo.label());
            labelDiskon.getStyleClass().add("paket-discount-source");
            HBox hargaAkhirRow = new HBox(8, hargaAkhir, labelDiskon);
            hargaAkhirRow.setAlignment(Pos.CENTER_LEFT);

            VBox hargaBox = new VBox(4, hargaAwalRow, hargaAkhirRow);
            hargaRow.getChildren().add(hargaBox);
        } else {
            Label harga = new Label(formatRp(hargaNormal));
            harga.getStyleClass().add("paket-price");
            hargaRow.getChildren().add(harga);
        }

        VBox features = new VBox(6);
        for (String feature : buildFeatures(paket)) {
            Label item = new Label("✓  " + feature);
            item.getStyleClass().add("paket-feature");
            item.setWrapText(true);
            features.getChildren().add(item);
        }

        Button pilih = new Button("Pilih Paket Ini");
        pilih.getStyleClass().add("btn-pilih");
        pilih.setMaxWidth(Double.MAX_VALUE);
        pilih.setOnAction(e -> pilihPaket(paket, card));

        card.getChildren().addAll(nama, tipe, hargaRow, features, pilih);
        return card;
    }

    private void pilihPaket(Paket paket, VBox card) {
        selectedPaket = paket;
        updateAvailableTimeSlots();
        updateJamSelesaiInfo();

        if (activePackCard != null) {
            activePackCard.getStyleClass().removeAll("paket-card-selected");
        }
        card.getStyleClass().add("paket-card-selected");
        activePackCard = card;

        goToStep(2);
    }

    // ── Step 2: Tanggal & Jam Mulai ──────────────────────────────
    @FXML private void backToStep1() { goToStep(1); }
    @FXML private void goToStep3() {
        if (selectedPaket == null) { showAlert("Pilih paket terlebih dahulu."); goToStep(1); return; }
        if (selectedDate == null) { showAlert("Pilih tanggal terlebih dahulu."); return; }

        // ── Validasi backend: tanggal sudah lewat atau penuh ──────────
        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert("Tanggal yang dipilih sudah lewat. Silakan pilih tanggal lain.");
            selectedDate = null;
            buildCalendar(currentMonth);
            return;
        }
        if (BlockedDateDAO.getInstance().isBlocked(selectedDate)) {
            showAlert("Tanggal ini sedang diblokir admin. Silakan pilih tanggal lain.");
            selectedDate = null;
            buildCalendar(currentMonth);
            return;
        }
        if (!hasAnyAvailableSlot(selectedDate)) {
            showAlert("Tanggal " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    + " sudah penuh dipesan. Silakan pilih tanggal lain.");
            selectedDate = null;
            buildCalendar(currentMonth);
            return;
        }
        // ──────────────────────────────────────────────────────────────

        String jam = getSelectedJamMulai();
        if (jam.isEmpty()) { showAlert("Masukkan jam mulai acara."); return; }
        if (!jam.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            showAlert("Format jam tidak valid. Gunakan HH:mm, contoh: 09:00"); return;
        }
        int jamInt = Integer.parseInt(jam.split(":")[0]);
        if (jamInt < 8 || jamInt >= 21) {
            showAlert("Jam mulai harus antara 08:00 - 21:00 WIB."); return;
        }
        if (parseDurasiMenit(resolveJamOperasional(selectedPaket)) <= 0) {
            showAlert("Jam operasional paket belum diatur admin.");
            return;
        }
        if (!isSlotAvailable(selectedDate, jam, parseDurasiMenit(resolveJamOperasional(selectedPaket)))) {
            showAlert("Jam " + jam + " sudah tidak tersedia. Silakan pilih jam lain.");
            updateAvailableTimeSlots();
            return;
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
        if (selectedPaket == null) {
            showAlert("Pilih paket terlebih dahulu.");
            goToStep(1);
            return;
        }
        ringPaket.setText(selectedPaket.getNama());
        ringTipe.setText(selectedPaket.getTipe());
        ringTanggal.setText(selectedDate.format(fmt));
        ringJamMulai.setText("Jam Mulai: " + getSelectedJamMulai() + " | " + buildJamSelesaiText());
        ringLokasi.setText(lokasiField.getText().trim());
        ringNama.setText(namaDepanField.getText().trim());
        ringContact.setText(phoneField.getText().trim() + " • " + emailField.getText().trim());

        // hitung harga & promo/diskon
        int hargaInt = selectedPaket.getHarga();
        boolean berhakDiskon = UserDAO.getInstance().currentUserHasMemberDiscount();
        DiscountInfo discountInfo = resolveDiscount(selectedPaket, berhakDiskon);
        int diskonPersen = discountInfo.percent();

        int diskon = diskonPersen > 0 ? (hargaInt * diskonPersen / 100) : 0;
        int total  = hargaInt - diskon;
        totalFinal = total;

        payHarga.setText(formatRp(hargaInt));

        if (diskonPersen > 0) {
            diskonRow.setVisible(true);
            diskonRow.setManaged(true);
            payDiskon.setText("-" + formatRp(diskon) + " (" + discountInfo.label() + " " + diskonPersen + "%)");
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
        if (sedangKonfirmasi) return;
        sedangKonfirmasi = true;
        if (btnKonfirmasi != null) btnKonfirmasi.setDisable(true);

        Booking booking = new Booking();
        
//menambahkan logic agar tersimpan ke db booking
// ── Validasi ulang tanggal sebelum simpan ─────────────────────
if (selectedDate == null || selectedDate.isBefore(LocalDate.now())) {
    showAlert("Tanggal tidak valid. Silakan pilih ulang tanggal.");
    goToStep(2);
    resetKonfirmasiState();
    return;
}
// ── CEK TANGGAL SUDAH DIPESAN (persyaratan tugas tambahan) ────
if (BookingDAO.getInstance().isTanggalSudahDipesan(selectedDate)) {
    showAlert("Tanggal ini sudah dipesan.\nSilakan pilih tanggal lain.");
    selectedDate = null;
    buildCalendar(currentMonth);
    goToStep(2);
    resetKonfirmasiState();
    return;
}
// ─────────────────────────────────────────────────────────────
if (!hasAnyAvailableSlot(selectedDate)) {
    showAlert("Maaf, tanggal " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            + " baru saja penuh dipesan. Silakan pilih tanggal lain.");
    selectedDate = null;
    buildCalendar(currentMonth);
    goToStep(2);
    resetKonfirmasiState();
    return;
}
// ──────────────────────────────────────────────────────────────

String jamMulai = getSelectedJamMulai();
int durasiMenit = parseDurasiMenit(resolveJamOperasional(selectedPaket));
if (jamMulai.isEmpty() || durasiMenit <= 0 || !isSlotAvailable(selectedDate, jamMulai, durasiMenit)) {
    showAlert("Slot jam yang dipilih sudah tidak tersedia. Silakan pilih ulang jam.");
    updateAvailableTimeSlots();
    goToStep(2);
    resetKonfirmasiState();
    return;
}

// user
User user = UserDAO.getInstance().getCurrentUser();
if (user == null) {
    showAlert("Login dulu!");
    resetKonfirmasiState();
    return;
}
booking.setUser(user);

// paket
Paket paket = selectedPaket != null ? PaketDAO.getInstance().findById(selectedPaket.getId()) : null;

if (paket == null) {
    showAlert("Paket gak ditemukan!");
    resetKonfirmasiState();
    return;
}
booking.setPaket(paket);

// isi data
booking.setTanggal(java.sql.Date.valueOf(selectedDate));
booking.setJamMulai(jamMulai);
booking.setLokasi(lokasiField.getText().trim());
booking.setNamaPemesan(namaDepanField.getText());
booking.setEmail(emailField.getText());
booking.setPhone(phoneField.getText());
booking.setCatatan(catatanField.getText());

// nomor & status
String nomor = BookingDAO.generateNomorPesanan();
booking.setNomorPesanan(nomor);
booking.setStatus("Menunggu Konfirmasi");

booking.setTotalHarga(totalFinal);

// SAVE
boolean success = BookingDAO.getInstance().save(booking);

if (!success) {
    showAlert("Gagal simpan!");
    resetKonfirmasiState();
    return;
}
        String nomorPesanan = nomor;

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

    private DiscountInfo resolveDiscount(Paket paket, boolean userMember) {
        int promoUmum = normalizePercent(paket.getPromoUmum());
        int diskonMember = userMember ? normalizePercent(paket.getDiskonMember()) : 0;

        if (diskonMember >= promoUmum && diskonMember > 0) {
            return new DiscountInfo(diskonMember, "Member");
        }
        if (promoUmum > 0) {
            return new DiscountInfo(promoUmum, "Promo");
        }
        return new DiscountInfo(0, "");
    }

    private int normalizePercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private record DiscountInfo(int percent, String label) {}

    private void updateJamSelesaiInfo() {
        if (jamSelesaiInfo == null) return;
        String text = buildJamSelesaiText();
        jamSelesaiInfo.setText(text.isBlank() ? "" : "Jam selesai otomatis: " + text);
    }

    private String buildJamSelesaiText() {
        if (selectedPaket == null || jamMulaiField == null) return "";

        String jamMulai = getSelectedJamMulai();
        if (!jamMulai.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) return "";

        String durasi = resolveJamOperasional(selectedPaket);
        int durasiMenit = parseDurasiMenit(durasi);
        if (durasiMenit <= 0) return "";

        LocalTime mulai = LocalTime.parse(jamMulai.length() == 4 ? "0" + jamMulai : jamMulai);
        LocalTime selesai = mulai.plusMinutes(durasiMenit);
        return selesai.format(DateTimeFormatter.ofPattern("HH:mm")) + " (" + durasi + ")";
    }

    private String getSelectedJamMulai() {
        return jamMulaiField != null && jamMulaiField.getValue() != null
            ? jamMulaiField.getValue().trim()
            : "";
    }

    private void updateAvailableTimeSlots() {
        if (jamMulaiField == null) return;

        String current = getSelectedJamMulai();
        jamMulaiField.getItems().clear();
        jamMulaiField.setPromptText("Pilih paket dan tanggal dulu");

        if (selectedPaket == null) {
            jamMulaiField.setDisable(true);
            jamMulaiField.setValue(null);
            updateJamSelesaiInfo();
            return;
        }

        int durasiMenit = parseDurasiMenit(resolveJamOperasional(selectedPaket));
        if (durasiMenit <= 0) {
            jamMulaiField.setDisable(true);
            jamMulaiField.setPromptText("Durasi paket belum diatur");
            jamMulaiField.setValue(null);
            updateJamSelesaiInfo();
            return;
        }

        if (selectedDate == null) {
            jamMulaiField.setDisable(true);
            jamMulaiField.setPromptText("Pilih tanggal dulu");
            jamMulaiField.setValue(null);
            updateJamSelesaiInfo();
            return;
        }

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        List<String> slots = new ArrayList<>();
        for (LocalTime mulai = JAM_BUKA;
             !mulai.plusMinutes(durasiMenit).isAfter(JAM_TUTUP);
             mulai = mulai.plusMinutes(durasiMenit)) {
            String jam = mulai.format(timeFmt);
            if (isSlotAvailable(selectedDate, jam, durasiMenit)) {
                slots.add(jam);
            }
        }

        jamMulaiField.getItems().setAll(slots);
        jamMulaiField.setDisable(slots.isEmpty());
        jamMulaiField.setPromptText(slots.isEmpty() ? "Slot hari ini penuh" : "Pilih jam mulai");
        jamMulaiField.setValue(slots.contains(current) ? current : null);
        updateJamSelesaiInfo();
    }

    private boolean isSlotAvailable(LocalDate tanggal, String jamMulai, int durasiMenit) {
        if (tanggal == null || jamMulai == null || jamMulai.isBlank() || durasiMenit <= 0) return false;

        LocalTime mulai = parseJam(jamMulai);
        if (mulai == null) return false;

        LocalTime selesai = mulai.plusMinutes(durasiMenit);
        if (mulai.isBefore(JAM_BUKA) || selesai.isAfter(JAM_TUTUP)) return false;

        for (Booking booking : BookingDAO.getInstance().findActiveByDate(tanggal)) {
            LocalTime bookedStart = parseJam(booking.getJamMulai());
            if (bookedStart == null) continue;

            int bookedDuration = resolveDurasiMenit(booking.getPaket());
            if (bookedDuration <= 0) bookedDuration = durasiMenit;

            LocalTime bookedEnd = bookedStart.plusMinutes(bookedDuration);
            if (mulai.isBefore(bookedEnd) && selesai.isAfter(bookedStart)) return false;
        }

        return true;
    }

    private boolean hasAnyAvailableSlot(LocalDate tanggal) {
        if (tanggal == null || selectedPaket == null) return false;

        int durasiMenit = parseDurasiMenit(resolveJamOperasional(selectedPaket));
        if (durasiMenit <= 0) return false;

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        for (LocalTime mulai = JAM_BUKA;
             !mulai.plusMinutes(durasiMenit).isAfter(JAM_TUTUP);
             mulai = mulai.plusMinutes(durasiMenit)) {
            if (isSlotAvailable(tanggal, mulai.format(timeFmt), durasiMenit)) return true;
        }
        return false;
    }

    private LocalTime parseJam(String value) {
        if (value == null || !value.trim().matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) return null;
        String normalized = value.trim();
        return LocalTime.parse(normalized.length() == 4 ? "0" + normalized : normalized);
    }

    private int resolveDurasiMenit(Paket paket) {
        if (paket == null) return 0;

        int durasi = parseDurasiMenit(paket.getJamOperasional());
        if (durasi > 0) return durasi;

        Paket fresh = PaketDAO.getInstance().findById(paket.getId());
        return fresh != null ? parseDurasiMenit(fresh.getJamOperasional()) : 0;
    }

    private String resolveJamOperasional(Paket paket) {
        if (paket == null) return "";

        String jamOperasional = paket.getJamOperasional();
        if (jamOperasional != null && !jamOperasional.isBlank()) {
            return jamOperasional.trim();
        }

        Paket fresh = PaketDAO.getInstance().findById(paket.getId());
        if (fresh != null) {
            selectedPaket = fresh;
            String freshJam = fresh.getJamOperasional();
            if (freshJam != null && !freshJam.isBlank()) {
                return freshJam.trim();
            }
        }

        return "";
    }

    private int parseDurasiMenit(String value) {
        if (value == null || value.isBlank()) return 0;

        String text = value.toLowerCase().replace(',', '.');
        int total = 0;

        java.util.regex.Matcher jamMatcher = java.util.regex.Pattern
            .compile("(\\d+(?:\\.\\d+)?)\\s*(jam|j)")
            .matcher(text);
        while (jamMatcher.find()) {
            double jam = Double.parseDouble(jamMatcher.group(1));
            total += (int) Math.round(jam * 60);
        }

        java.util.regex.Matcher menitMatcher = java.util.regex.Pattern
            .compile("(\\d+)\\s*(menit|min|m)")
            .matcher(text);
        while (menitMatcher.find()) {
            total += Integer.parseInt(menitMatcher.group(1));
        }

        if (total == 0) {
            java.util.regex.Matcher angkaOnly = java.util.regex.Pattern
                .compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*$")
                .matcher(text);
            if (angkaOnly.find()) total = (int) Math.round(Double.parseDouble(angkaOnly.group(1)) * 60);
        }

        return total;
    }

    private void resetKonfirmasiState() {
        sedangKonfirmasi = false;
        if (btnKonfirmasi != null) btnKonfirmasi.setDisable(false);
    }

    private List<String> buildFeatures(Paket paket) {
        List<String> items = new ArrayList<>();
        String jamOperasional = paket.getJamOperasional();
        if (jamOperasional != null && !jamOperasional.isBlank()) {
            items.add("Jam operasional: " + jamOperasional.trim());
        }

        String keterangan = paket.getKeterangan();
        if (keterangan != null && !keterangan.isBlank()) {
            String normalized = keterangan.trim();
            String[] rawItems = normalized.contains("\n")
                ? normalized.split("\\R+")
                : normalized.split("\\s*[;,]\\s*");

            Arrays.stream(rawItems)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .filter(item -> jamOperasional == null || jamOperasional.isBlank()
                    || !item.toLowerCase().contains("jam operasional"))
                .forEach(items::add);
        }

        if (!items.isEmpty()) return items;

        // Fallback: default berdasarkan tipe jika keterangan belum diisi admin
        return getDefaultFeatures(paket.getTipe());
    }

    private List<String> getDefaultFeatures(String tipe) {
        if (tipe != null && tipe.equalsIgnoreCase("Tanpa Cetak")) {
            return List.of(
                "2 jam operasional",
                "Backdrop 1 pilihan",
                "Props standar 10 pcs",
                "Digital file semua foto",
                "Share via QR Code",
                "1 operator profesional"
            );
        }
        return List.of(
            "4 jam operasional",
            "Backdrop 3 pilihan",
            "Props standar + tematik 30 pcs",
            "Cetak foto 4R unlimited",
            "Digital file + Share via QR Code",
            "1 operator profesional"
        );
    }

    // ── Kalender ──────────────────────────────────────────────────
    private void buildCalendar(YearMonth ym) {
        calendarBox.getChildren().clear();

        // ── Muat tanggal penuh dari DB ─────────────────────────────────
        Set<LocalDate> fullDates = BookingDAO.getInstance().getFullyBookedDatesInMonth(ym);
        Set<LocalDate> blockedDates = BlockedDateDAO.getInstance().getBlockedDatesInMonth(ym);

        DateTimeFormatter headerFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

        // ── Header navigasi bulan ──────────────────────────────────────
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

        // ── Grid kalender ─────────────────────────────────────────────
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

        LocalDate today    = LocalDate.now();
        LocalDate first    = ym.atDay(1);
        int startCol       = first.getDayOfWeek().getValue() % 7;
        int daysInMonth    = ym.lengthOfMonth();

        int col = startCol, row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = ym.atDay(day);
            Button btn = new Button(String.valueOf(day));
            btn.setMinWidth(36); btn.setMinHeight(36);
            btn.setMaxWidth(36); btn.setMaxHeight(36);

            boolean isPast = date.isBefore(today);
            boolean isBlocked = blockedDates.contains(date);
            boolean isFull = selectedPaket == null ? fullDates.contains(date) : !hasAnyAvailableSlot(date);

            if (date.equals(selectedDate)) {
                btn.getStyleClass().add("cal-day-selected");
                btn.setOnAction(e -> { selectedDate = date; updateAvailableTimeSlots(); buildCalendar(currentMonth); });
            } else if (isPast) {
                // tanggal lewat – disable, warna abu
                btn.getStyleClass().add("cal-day-past");
                btn.setDisable(true);
            } else if (isBlocked) {
                // tanggal diblokir admin – merah dan tidak bisa diklik pelanggan
                btn.getStyleClass().add("cal-day-blocked");
                btn.setDisable(true);
                btn.setTooltip(new Tooltip("Tanggal diblokir admin"));
            } else if (isFull) {
                // tanggal penuh – disable, warna merah
                btn.getStyleClass().add("cal-day-full");
                btn.setDisable(true);
                btn.setOnAction(e -> showAlert("Tanggal " + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        + " sudah penuh dipesan. Silakan pilih tanggal lain."));
            } else {
                // tersedia – warna hijau
                btn.getStyleClass().add("cal-day-available");
                btn.setOnAction(e -> { selectedDate = date; updateAvailableTimeSlots(); buildCalendar(currentMonth); });
            }

            grid.add(btn, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        calendarBox.getChildren().add(grid);

        // ── Legend ────────────────────────────────────────────────────
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(10, 0, 0, 0));
        legend.getChildren().addAll(
            makeLegendItem("cal-legend-available", "Tersedia"),
            makeLegendItem("cal-legend-blocked",   "Diblokir Admin"),
            makeLegendItem("cal-legend-full",      "Penuh"),
            makeLegendItem("cal-legend-past",       "Sudah Lewat"),
            makeLegendItem("cal-legend-selected",   "Dipilih")
        );
        calendarBox.getChildren().add(legend);
    }

    /** Buat satu item legend (dot + label). */
    private HBox makeLegendItem(String dotStyle, String text) {
        Label dot = new Label();
        dot.getStyleClass().add(dotStyle);
        dot.setMinWidth(12); dot.setMinHeight(12);
        dot.setMaxWidth(12); dot.setMaxHeight(12);
        Label lbl = new Label(text);
        lbl.getStyleClass().add("cal-legend-label");
        HBox item = new HBox(5, dot, lbl);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
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

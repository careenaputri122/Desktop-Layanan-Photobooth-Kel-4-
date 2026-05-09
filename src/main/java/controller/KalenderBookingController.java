package controller;

import dao.BookingDAO;
import dao.BlockedDateDAO;
import dao.UserDAO;
import model.Booking;
import model.User;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * KalenderBookingController — Halaman Kalender Booking Admin
 *
 * Fitur:
 *  - Kalender bulanan dengan navigasi Previous/Next
 *  - Tanggal yang sudah booked ditandai warna pink
 *  - Klik tanggal booked → tampil panel detail di kanan
 *  - Legend: Putih = tersedia, Pink = booked, Abu-abu = luar bulan
 */
public class KalenderBookingController {

    // ── Sidebar Profile ───────────────────────────────────────────
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelAdminInitial;

    // ── Kalender ──────────────────────────────────────────────────
    @FXML private Label labelBulanTahun;
    @FXML private GridPane calendarGrid;

    // ── Panel Detail (kanan) ──────────────────────────────────────
    @FXML private VBox detailPanel;
    @FXML private Label detailTitle;
    @FXML private VBox  detailContent;

    // ── State ─────────────────────────────────────────────────────
    private YearMonth currentMonth = YearMonth.now();

    private static final String[] NAMA_HARI = {
        "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"
    };

    private static final DateTimeFormatter FMT_TANGGAL =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    private final NumberFormat rupiahFmt =
        NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupSidebarProfile();
        buildCalendar(currentMonth);
        showPlaceholderDetail();
    }

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    // ── Navigasi Bulan ────────────────────────────────────────────
    @FXML
    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        buildCalendar(currentMonth);
        showPlaceholderDetail();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        buildCalendar(currentMonth);
        showPlaceholderDetail();
    }

    // ── Build Kalender ────────────────────────────────────────────
    private void buildCalendar(YearMonth ym) {
        // Update label bulan & tahun
        String namaBulan = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
        String kapitalisasi = namaBulan.substring(0, 1).toUpperCase() + namaBulan.substring(1);
        labelBulanTahun.setText(kapitalisasi + " " + ym.getYear());

        // Ambil data booked dari DB
        Map<LocalDate, List<Booking>> bookingMap = getBookingMapForMonth(ym);
        Set<LocalDate> blockedDates = BlockedDateDAO.getInstance().getBlockedDatesInMonth(ym);

        calendarGrid.getChildren().clear();
        calendarGrid.setHgap(6);
        calendarGrid.setVgap(6);

        // Set column constraints (7 kolom sama lebar)
        calendarGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // Row 0: Header nama hari
        for (int i = 0; i < 7; i++) {
            Label hdr = new Label(NAMA_HARI[i]);
            hdr.setMaxWidth(Double.MAX_VALUE);
            hdr.setAlignment(Pos.CENTER);
            hdr.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #EC4899;" +
                "-fx-padding: 6 0 6 0;"
            );
            calendarGrid.add(hdr, i, 0);
        }

        // Hitung hari pertama bulan (0=Senin, 6=Minggu dalam ISO)
        LocalDate firstDay   = ym.atDay(1);
        int       startCol   = firstDay.getDayOfWeek().getValue() - 1; // ISO: Mon=1
        int       daysInMonth= ym.lengthOfMonth();
        LocalDate today      = LocalDate.now();

        int row = 1;
        int col = startCol;

        // Isi tanggal kosong sebelum hari pertama (abu-abu)
        for (int i = 0; i < startCol; i++) {
            LocalDate prevDate = firstDay.minusDays(startCol - i);
            calendarGrid.add(createDayCell(prevDate, true, Collections.emptyList(), today, false), i, row);
        }

        // Isi tanggal bulan aktif
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date   = ym.atDay(day);
            List<Booking> bs = bookingMap.getOrDefault(date, Collections.emptyList());

            VBox cell = createDayCell(date, false, bs, today, blockedDates.contains(date));
            calendarGrid.add(cell, col, row);

            col++;
            if (col == 7) { col = 0; row++; }
        }

        // Isi sisa kolom setelah akhir bulan (abu-abu)
        if (col > 0) {
            int extraDay = 1;
            LocalDate nextFirst = ym.atEndOfMonth().plusDays(1);
            while (col < 7) {
                calendarGrid.add(createDayCell(nextFirst.plusDays(extraDay - 1), true,
                        Collections.emptyList(), today, false), col, row);
                col++;
                extraDay++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, boolean outsideMonth,
                               List<Booking> bookings, LocalDate today, boolean isBlocked) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(6, 4, 6, 4));
        cell.setMinHeight(72);
        cell.setMaxWidth(Double.MAX_VALUE);

        boolean hasBooking = !bookings.isEmpty();
        boolean isToday    = date.equals(today);

        // Warna background
        String bg;
        if (outsideMonth) {
            bg = "#F3F4F6"; // Abu-abu luar bulan
        } else if (isBlocked) {
            bg = "#FEE2E2"; // Merah = diblokir admin
        } else if (hasBooking) {
            bg = "#FDE8F4"; // Pink = booked
        } else {
            bg = "white";   // Putih = tersedia
        }

        String border = isToday && !outsideMonth
            ? "-fx-border-color: #EC4899; -fx-border-width: 2;"
            : "-fx-border-color: #E5E7EB; -fx-border-width: 1;";

        cell.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: 8;" +
            border +
            "-fx-border-radius: 8;" +
            (!outsideMonth ? "-fx-cursor: hand;" : "")
        );

        // Label nomor tanggal
        Label lblDay = new Label(String.valueOf(date.getDayOfMonth()));
        lblDay.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (outsideMonth ? "#D1D5DB" :
                                  isBlocked   ? "#991B1B"  :
                                  isToday     ? "#EC4899"  : "#374151") + ";"
        );
        cell.getChildren().add(lblDay);

        // Badge "Diblokir" jika admin memblokir tanggal
        if (isBlocked && !outsideMonth) {
            Label badge = new Label("Diblokir");
            badge.setStyle(
                "-fx-background-color: #DC2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 2 5 2 5;"
            );
            badge.setMaxWidth(Double.MAX_VALUE);
            badge.setAlignment(Pos.CENTER);
            cell.getChildren().add(badge);
        }

        // Badge "Booked" jika ada booking
        if (hasBooking && !outsideMonth) {
            Label badge = new Label(bookings.size() == 1 ? "Booked" : bookings.size() + " Booked");
            badge.setStyle(
                "-fx-background-color: #EC4899;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 2 5 2 5;"
            );
            badge.setMaxWidth(Double.MAX_VALUE);
            badge.setAlignment(Pos.CENTER);
            cell.getChildren().add(badge);
        }

        // Klik handler admin: semua tanggal bulan aktif bisa dibuka detailnya.
        if (!outsideMonth) {
            final List<Booking> finalBookings = bookings;
            final LocalDate     finalDate     = date;
            final boolean       finalBlocked  = isBlocked;
            cell.setOnMouseClicked(e -> showDetailForDate(finalDate, finalBookings, finalBlocked));

            // Hover effect
            cell.setOnMouseEntered(ev -> cell.setStyle(cell.getStyle().replace(bg, finalBlocked ? "#FCA5A5" : "#F9A8D4")));
            cell.setOnMouseExited(ev  -> cell.setStyle(cell.getStyle().replace(finalBlocked ? "#FCA5A5" : "#F9A8D4", bg)));
        }

        return cell;
    }

    // ── Ambil data booking per tanggal ───────────────────────────
    private Map<LocalDate, List<Booking>> getBookingMapForMonth(YearMonth ym) {
        List<Booking> all = BookingDAO.getInstance().findAll();
        Map<LocalDate, List<Booking>> map = new LinkedHashMap<>();

        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        for (Booking b : all) {
            if (b.getTanggal() == null) continue;
            // Lewati status Ditolak
            if ("Ditolak".equalsIgnoreCase(b.getStatus())) continue;

            LocalDate tgl = ((java.sql.Date) b.getTanggal()).toLocalDate();

            if (!tgl.isBefore(start) && !tgl.isAfter(end)) {
                map.computeIfAbsent(tgl, k -> new ArrayList<>()).add(b);
            }
        }
        return map;
    }

    // ── Panel Detail ──────────────────────────────────────────────
    private void showPlaceholderDetail() {
        detailTitle.setText("Detail Booking");
        detailContent.getChildren().clear();

        Label ph = new Label("Klik tanggal yang berwarna\npink untuk melihat detail\nbooking.");
        ph.setStyle(
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-size: 13px;" +
            "-fx-text-alignment: center;"
        );
        ph.setAlignment(Pos.CENTER);
        ph.setWrapText(true);
        ph.setMaxWidth(Double.MAX_VALUE);

        VBox iconBox = new VBox(8);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPadding(new Insets(30, 0, 0, 0));

        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 40px;");
        iconBox.getChildren().addAll(icon, ph);

        detailContent.getChildren().add(iconBox);
    }

    private void showDetailForDate(LocalDate date, List<Booking> bookings, boolean isBlocked) {
        detailTitle.setText("📅 " + date.format(FMT_TANGGAL));
        detailContent.getChildren().clear();

        Label subTitle = new Label((isBlocked ? "Tanggal ini sedang DIBLOKIR admin • " : "") + bookings.size() + " booking pada tanggal ini");
        subTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (isBlocked ? "#DC2626" : "#9CA3AF") + "; -fx-font-weight: bold;");
        detailContent.getChildren().add(subTitle);

        Button toggleBlockBtn = new Button(isBlocked ? "🔓 Buka Tanggal" : "🚫 Blokir Tanggal");
        toggleBlockBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBlockBtn.setStyle(
            "-fx-background-color: " + (isBlocked ? "#10B981" : "#DC2626") + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 10 8 10;"
        );
        toggleBlockBtn.setOnAction(e -> toggleBlockedDate(date, isBlocked));
        detailContent.getChildren().add(toggleBlockBtn);

        Separator sep0 = new Separator();
        sep0.setStyle("-fx-background-color: #F3F4F6;");
        sep0.setPadding(new Insets(4, 0, 4, 0));
        detailContent.getChildren().add(sep0);

        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            VBox card = buildBookingCard(b, i + 1);
            detailContent.getChildren().add(card);

            if (i < bookings.size() - 1) {
                Separator sep = new Separator();
                sep.setPadding(new Insets(6, 0, 6, 0));
                detailContent.getChildren().add(sep);
            }
        }
    }

    private void toggleBlockedDate(LocalDate date, boolean currentlyBlocked) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(currentlyBlocked ? "Buka Tanggal" : "Blokir Tanggal");
        confirm.setHeaderText(currentlyBlocked ? "Buka kembali tanggal ini?" : "Blokir tanggal ini?");
        confirm.setContentText(currentlyBlocked
            ? "Tanggal " + date.format(FMT_TANGGAL) + " akan tersedia kembali untuk pelanggan."
            : "Tanggal " + date.format(FMT_TANGGAL) + " akan merah dan tidak bisa diklik oleh pelanggan.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        boolean success;
        if (currentlyBlocked) {
            success = BlockedDateDAO.getInstance().unblockDate(date);
        } else {
            User admin = UserDAO.getInstance().getCurrentUser();
            int adminId = admin != null ? admin.getId() : 0;
            success = BlockedDateDAO.getInstance().blockDate(date, "Diblokir melalui kalender admin", adminId);
        }

        if (success) {
            buildCalendar(currentMonth);
            showPlaceholderDetail();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR, "Gagal menyimpan perubahan blokir tanggal ke database.");
            error.showAndWait();
        }
    }

    private VBox buildBookingCard(Booking b, int nomor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 8, 10, 8));
        card.setStyle(
            "-fx-background-color: #FFF9FB;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #FBCFE8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );

        // Nomor urut
        Label no = new Label("Booking #" + nomor);
        no.setStyle("-fx-font-size: 11px; -fx-text-fill: #EC4899; -fx-font-weight: bold;");
        card.getChildren().add(no);

        // Badge status
        Label statusBadge = new Label(b.getStatus() != null ? b.getStatus() : "-");
        String statusColor = switch (b.getStatus() != null ? b.getStatus().toLowerCase() : "") {
            case "menunggu konfirmasi" -> "#D97706";
            case "disetujui"           -> "#059669";
            case "selesai"             -> "#7C3AED";
            case "ditolak"             -> "#DC2626";
            default                    -> "#6B7280";
        };
        statusBadge.setStyle(
            "-fx-text-fill: white;" +
            "-fx-background-color: " + statusColor + ";" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 2 7 2 7;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );
        card.getChildren().add(statusBadge);

        // Separator tipis
        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 0, 2, 0));
        card.getChildren().add(sep);

        // Baris-baris info
        card.getChildren().add(infoRow("No. Pesanan", b.getNomorPesanan()));
        card.getChildren().add(infoRow("Nama Pemesan", b.getNamaPemesan()));
        card.getChildren().add(infoRow("Paket",
            b.getPaket() != null ? b.getPaket().getNama() : "-"));
        card.getChildren().add(infoRow("Jam Mulai",
            b.getJamMulai() != null ? b.getJamMulai() : "-"));
        card.getChildren().add(infoRow("Lokasi",
            b.getLokasi() != null && !b.getLokasi().isEmpty() ? b.getLokasi() : "-"));
        card.getChildren().add(infoRow("Total",
            rupiahFmt.format(b.getTotalHarga())));

        return card;
    }

    private HBox infoRow(String key, String value) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lKey = new Label(key + ":");
        lKey.setMinWidth(90);
        lKey.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");

        Label lVal = new Label(value != null && !value.isEmpty() ? value : "-");
        lVal.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");
        lVal.setWrapText(true);
        HBox.setHgrow(lVal, Priority.ALWAYS);

        row.getChildren().addAll(lKey, lVal);
        return row;
    }

    // ── Navigasi Sidebar ──────────────────────────────────────────
    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goDashboard() {
        try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goKelolaPesanan() {
        try { SceneManager.showKelolaPesanan(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goKelolaPaket() {
        try { SceneManager.showKelolaPaket(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goUploadGaleri() { System.out.println("TODO: Upload Galeri"); }
    @FXML private void goPelanggan()    { try { SceneManager.showKelolaPelanggan(); } catch (Exception e) { e.printStackTrace(); } }
}

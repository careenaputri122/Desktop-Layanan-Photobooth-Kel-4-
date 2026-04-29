package model;

import java.util.Date;

public class Booking {
    private int    id;
    private User   user;
    private Paket  paket;
    private Date   tanggal;
    private String slotWaktu;
    private String lokasi;
    private String namaPemesan;
    private String email;
    private String phone;
    private String catatan;
    private String status;
    private String nomorPesanan;
    private double totalHarga;

    public Booking() {}

    public Booking(int id, User user, Paket paket, Date tanggal, String status) {
        this.id      = id;
        this.user    = user;
        this.paket   = paket;
        this.tanggal = tanggal;
        this.status  = status;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int    getId()            { return id; }
    public void   setId(int id)      { this.id = id; }

    public User   getUser()          { return user; }
    public void   setUser(User u)    { this.user = u; }

    public Paket  getPaket()         { return paket; }
    public void   setPaket(Paket p)  { this.paket = p; }

    public Date   getTanggal()               { return tanggal; }
    public void   setTanggal(Date tanggal)   { this.tanggal = tanggal; }

    public String getSlotWaktu()                  { return slotWaktu; }
    public void   setSlotWaktu(String slotWaktu)  { this.slotWaktu = slotWaktu; }

    public String getLokasi()               { return lokasi; }
    public void   setLokasi(String lokasi)  { this.lokasi = lokasi; }

    public String getNamaPemesan()                    { return namaPemesan; }
    public void   setNamaPemesan(String namaPemesan)  { this.namaPemesan = namaPemesan; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getCatatan()                { return catatan; }
    public void   setCatatan(String catatan)  { this.catatan = catatan; }

    public String getStatus()               { return status; }
    public void   setStatus(String status)  { this.status = status; }

    public String getNomorPesanan()                       { return nomorPesanan; }
    public void   setNomorPesanan(String nomorPesanan)    { this.nomorPesanan = nomorPesanan; }

    public double getTotalHarga()                   { return totalHarga; }
    public void   setTotalHarga(double totalHarga)  { this.totalHarga = totalHarga; }
}

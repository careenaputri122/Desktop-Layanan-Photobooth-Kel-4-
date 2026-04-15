package model;

import java.util.Date;

public class Booking {
    private int id;
    private User user;
    private Paket paket;
    private Date tanggal;
    private String status;

    public Booking() {}

    public Booking(int id, User user, Paket paket, Date tanggal, String status) {
        this.id = id;
        this.user = user;
        this.paket = paket;
        this.tanggal = tanggal;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Paket getPaket() { return paket; }
    public void setPaket(Paket paket) { this.paket = paket; }

    public Date getTanggal() { return tanggal; }
    public void setTanggal(Date tanggal) { this.tanggal = tanggal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

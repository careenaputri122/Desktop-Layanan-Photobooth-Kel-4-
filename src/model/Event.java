package model;

import java.util.Date;

public class Event {
    private int id;
    private String namaEvent;
    private Date tanggal;
    private String lokasi;

    public Event() {}

    public Event(int id, String namaEvent, Date tanggal, String lokasi) {
        this.id = id;
        this.namaEvent = namaEvent;
        this.tanggal = tanggal;
        this.lokasi = lokasi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaEvent() { return namaEvent; }
    public void setNamaEvent(String namaEvent) { this.namaEvent = namaEvent; }

    public Date getTanggal() { return tanggal; }
    public void setTanggal(Date tanggal) { this.tanggal = tanggal; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
}

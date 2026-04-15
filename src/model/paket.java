package model;

public class Paket {
    private int id;
    private String namaPaket;
    private double harga;
    private String deskripsi;

    public Paket() {}

    public Paket(int id, String namaPaket, double harga, String deskripsi) {
        this.id = id;
        this.namaPaket = namaPaket;
        this.harga = harga;
        this.deskripsi = deskripsi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaPaket() { return namaPaket; }
    public void setNamaPaket(String namaPaket) { this.namaPaket = namaPaket; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
}

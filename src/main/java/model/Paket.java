package model;

public class Paket {
    private int    id;
    private String nama;    
    private int    harga;     
    private String tipe;     

    public Paket() {}

    public Paket(int id, String nama, int harga, String tipe) {
        this.id    = id;
        this.nama  = nama;
        this.harga = harga;
        this.tipe  = tipe;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int    getId()             { return id; }
    public void   setId(int id)       { this.id = id; }

    public String getNama()             { return nama; }
    public void   setNama(String nama)  { this.nama = nama; }

    public int    getHarga()             { return harga; }
    public void   setHarga(int harga)    { this.harga = harga; }

    public String getTipe()             { return tipe; }
    public void   setTipe(String tipe)  { this.tipe = tipe; }
}

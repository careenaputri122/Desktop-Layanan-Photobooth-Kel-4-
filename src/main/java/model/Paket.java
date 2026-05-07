package model;

public class Paket {
    private int    id;
    private String nama;    
    private int    harga;     
    private String tipe;
    private String keterangan;
    private int    diskonMember;
    private int    promoUmum;

    public Paket() {}

    public Paket(int id, String nama, int harga, String tipe) {
        this.id    = id;
        this.nama  = nama;
        this.harga = harga;
        this.tipe  = tipe;
    }

    public Paket(int id, String nama, int harga, String tipe, String keterangan) {
        this(id, nama, harga, tipe);
        this.keterangan = keterangan;
    }

    public Paket(int id, String nama, int harga, String tipe, String keterangan, int diskonMember) {
        this(id, nama, harga, tipe, keterangan);
        this.diskonMember = diskonMember;
    }

    public Paket(int id, String nama, int harga, String tipe, String keterangan, int diskonMember, int promoUmum) {
        this(id, nama, harga, tipe, keterangan, diskonMember);
        this.promoUmum = promoUmum;
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

    public String getKeterangan()                  { return keterangan; }
    public void   setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public int  getDiskonMember()                 { return diskonMember; }
    public void setDiskonMember(int diskonMember) { this.diskonMember = diskonMember; }

    public int  getPromoUmum()              { return promoUmum; }
    public void setPromoUmum(int promoUmum) { this.promoUmum = promoUmum; }
}

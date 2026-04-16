package model;

public class User {
    private int id;
    private String namaDepan;
    private String namaBelakang;
    private String email;
    private String password;
    private String role;

    public User() {}

    public User(int id, String namaDepan, String namaBelakang, String email, String password, String role) {
        this.id = id;
        this.namaDepan = namaDepan;
        this.namaBelakang = namaBelakang;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaDepan() { return namaDepan; }
    public void setNamaDepan(String namaDepan) { this.namaDepan = namaDepan; }

    public String getNamaBelakang() { return namaBelakang; }
    public void setNamaBelakang(String namaBelakang) { this.namaBelakang = namaBelakang; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // optional: nama lengkap
    public String getNamaLengkap() {
        return namaDepan + " " + namaBelakang;
    }
}
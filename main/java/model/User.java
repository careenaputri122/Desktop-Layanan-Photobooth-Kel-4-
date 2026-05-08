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

    public int getId() { 
        return id; 
    }
    
    public String getNamaDepan() { 
        return namaDepan; 
    }
    
    public String getNamaBelakang() { 
        return namaBelakang; 
    }
    
    public String getNamaLengkap() { 
        return namaDepan + " " + namaBelakang; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public String getPassword() { 
        return password; 
    }

     public String getRole() { 
        return role; 
    }

    public void setId(int id) {          
        this.id = id; 
    }
    
    public void setNamaDepan(String namaDepan) { 
        this.namaDepan = namaDepan; 
    }
    
    public void setNamaBelakang(String namaBelakang) { 
        this.namaBelakang = namaBelakang; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    public void setRole(String role) { 
        this.role = role; 
    }

}
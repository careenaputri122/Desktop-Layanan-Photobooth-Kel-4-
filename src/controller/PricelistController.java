package controller;

import javafx.fxml.FXML;

public class PricelistController {


    // ── Aksi Pesan ───────────────────────────────────
    @FXML
    private void pesanPaket() {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Navigasi Navbar ──────────────────────────────
    @FXML private void goHome()      { try { SceneManager.showHome();      } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goGaleri()    { System.out.println("TODO: Galeri");  }
    @FXML private void goPemesanan() { try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goAdmin()     { System.out.println("TODO: Admin");   }
    @FXML private void goMember()    { System.out.println("TODO: Member");  }
    @FXML private void goLogin()     { try { SceneManager.showLogin();      } catch (Exception e) { e.printStackTrace(); } }
    @FXML
private void goSignin() {
    try { SceneManager.showRegister(); } catch (Exception e) { e.printStackTrace(); }
}
}

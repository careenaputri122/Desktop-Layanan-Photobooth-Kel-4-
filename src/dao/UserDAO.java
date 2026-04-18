package dao;

import model.User;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static UserDAO instance;
    private List<User> users    = new ArrayList<>();
    private User currentUser;

    private UserDAO() {}

    public static UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    // Simpan user baru 
    public boolean register(User user) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(user.getEmail())) return false;
        }
        users.add(user);
        return true;
    }

    // Cek email + password 
    public User login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)
                    && u.getPassword().equals(password)) {
                currentUser = u;
                return u;
            }
        }
        return null;
    }

    public User getCurrentUser()  { return currentUser; }
    public void logout()          { currentUser = null; }
    public List<User> getAll()    { return users; }
}

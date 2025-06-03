package valaermortis.core;

import valaermortis.model.User;
import valaermortis.service.*;

public class AppContext {
    private User currentUser;
    
    public final AuthService authService = new AuthService(this);

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public boolean isLoggedIn() { return currentUser != null; }
}
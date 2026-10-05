package warehouse.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Role { ADMIN, MANAGER, OPERATOR }

    private int id;
    private String username;
    private String password;
    private String fullName;
    private Role role;

    public User(int id, String username, String password, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() { return fullName + " (" + role + ")"; }
}

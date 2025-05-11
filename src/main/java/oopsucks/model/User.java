package oopsucks.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class User {
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userID;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "dob", nullable = false)
    private String dob;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public User() {
    }

    public User(String userName, String userID, String email, String dob, String accountName, String password, Role role) {
        this.userName = userName;
        this.userID = userID;
        this.email = email;
        this.dob = dob;
        this.accountName = accountName;
        this.password = password;
        this.role = role;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

}

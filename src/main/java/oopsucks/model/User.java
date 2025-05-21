package oopsucks.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class User {
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userID;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "dob", nullable = false)
    private String dob;

    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "gender", nullable = false)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public User() {
    }

    public User(String userName, String userID, String email, String dob, String password, Role role) {
        this.userName = userName;
        this.userID = userID;
        this.email = email;
        this.dob = dob;
        this.password = password;
        this.role = role;
        this.gender = ""; 
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }


    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

}

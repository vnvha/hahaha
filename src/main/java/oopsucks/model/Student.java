package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Student extends User {

    @Column(name = "major")
    private String major;

    @Column(name = "faculty")
    private String faculty;

    public Student() {
    }

    public Student(String userName, String userID, String email, String dob, String accountName, String password, Role role, String major, String faculty) {
        super(userName, userID, email, dob, accountName, password, role);
        this.major = major;
        this.faculty = faculty;
    }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
}
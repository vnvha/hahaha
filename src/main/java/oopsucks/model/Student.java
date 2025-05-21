package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Student extends User {

    @Column(name = "major")
    private String major;

    @Column(name = "institute")
    private String institute;

    public Student() {
    }

    public Student(String userName, String userID, String email, String dob, String password, Role role, String major, String institute) {
        super(userName, userID, email, dob, password, role);
        this.major = major;
        this.institute = institute;
    }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }
}
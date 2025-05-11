package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "year_based_student")
public class YearBasedStudent extends Student {
    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    public YearBasedStudent() {
    }

    public YearBasedStudent(String userName, String userID, String email, String dob, String accountName, String password, Role role, String major, String faculty, String academicYear) {
        super(userName, userID, email, dob, accountName, password, role, major, faculty);
        this.academicYear = academicYear;
    }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}

package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "credit_based_student")
public class CreditBasedStudent extends Student {

    @Column(name = "total_credits", nullable = false)
    private int totalCredits;

    public CreditBasedStudent() {
    }

    public CreditBasedStudent(String userName, String userID, String email, String dob, String accountName, String password, Role role, String major, String faculty, int totalCredits) {
        super(userName, userID, email, dob, accountName, password, role, major, faculty);
        this.totalCredits = totalCredits;
    }

    // Getter và Setter
    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }
}
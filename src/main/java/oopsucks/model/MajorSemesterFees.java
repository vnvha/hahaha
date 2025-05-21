package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "major_semester_fees")
public class MajorSemesterFees {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "semester_fee", nullable = false)
    private Double semesterFee;

    public MajorSemesterFees() {
    }

    public MajorSemesterFees(String major, Double semesterFee) { 
        this.major = major; 
        this.semesterFee = semesterFee; 
    }
    
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Double getSemesterFee() { return semesterFee; }
    public void setSemesterFee(Double semesterFee) { this.semesterFee = semesterFee; }
}
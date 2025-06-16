package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "annual_tuition_fee")
public class AnnualTuitionFee extends BaseTuitionFee {

    @Column(name = "semester_fee", nullable = false)
    private Double semesterFee;

    public AnnualTuitionFee() {
        super();
    }

    public AnnualTuitionFee(String studentId, Integer semester, Double semesterFee) {
        super(studentId, semester);
        this.semesterFee = semesterFee;
    }

    @Override
    public Double calculateFee() {return this.semesterFee;}

    @Override
    public Double getTotalFee() { return this.semesterFee;}


    public Double getSemesterFee() { return semesterFee; }
    public void setSemesterFee(Double semesterFee) { this.semesterFee = semesterFee; }
}
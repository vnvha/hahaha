package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "major_semester_fees")
public class MajorSemesterFees extends MajorFees {

    @Column(name = "semester_fee", nullable = false)
    private Double semesterFee;

    public MajorSemesterFees() {
    }

    public MajorSemesterFees(String major, Double semesterFee) {
        super(major);
        this.semesterFee = semesterFee;
    }

    public Double getSemesterFee() {
        return semesterFee;
    }

    public void setSemesterFee(Double semesterFee) {
        this.semesterFee = semesterFee;
    }
}
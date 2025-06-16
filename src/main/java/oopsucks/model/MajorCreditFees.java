package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "major_credit_fees")
public class MajorCreditFees extends MajorFees {

    @Column(name = "credit_fee", nullable = false)
    private Double creditFee;

    public MajorCreditFees() {
    }

    public MajorCreditFees(String major, Double creditFee) {
        super(major);
        this.creditFee = creditFee;
    }

    public Double getCreditFee() {
        return creditFee;
    }

    public void setCreditFee(Double creditFee) {
        this.creditFee = creditFee;
    }
}
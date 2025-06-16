package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tuition_fee")
public class TuitionFee extends BaseTuitionFee {

    @Column(name = "credit_fee", nullable = false)
    private Double creditFee;

    @Column(name = "total_credits", nullable = false)
    private Integer totalChargeableCredits;

    @Column(name = "tuition", nullable = false)
    private Double tuition;

    public TuitionFee() {
        super();
        this.creditFee = 0.0;
        this.totalChargeableCredits = 0;
        this.tuition = 0.0;
    }

    public TuitionFee(String studentId, Integer semester, Double creditFee, Integer totalChargeableCredits) {
        super(studentId, semester);
        this.creditFee = creditFee;
        this.totalChargeableCredits = totalChargeableCredits;
        this.tuition = creditFee * totalChargeableCredits;
    }

    @Override
    public Double calculateFee() {
        return creditFee * totalChargeableCredits;
    }

    @Override
    public Double getTotalFee() { return tuition; }
    public Double getCreditFee() { return creditFee; }
    public void setCreditFee(Double creditFee) { this.creditFee = creditFee; updateTuition(); }
    public Integer getTotalChargeableCredits() { return totalChargeableCredits; }
    public void setTotalChargeableCredits(Integer totalChargeableCredits) { this.totalChargeableCredits = totalChargeableCredits; updateTuition(); }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }
    private void updateTuition() { this.tuition = (creditFee != null && totalChargeableCredits != null) ? creditFee * totalChargeableCredits : 0.0; }

}
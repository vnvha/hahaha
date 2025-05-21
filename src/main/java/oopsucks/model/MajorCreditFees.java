package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "major_credit_fees")
public class MajorCreditFees {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
	
    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "credit_fee", nullable = false)
    private Double creditFee;

    public MajorCreditFees() {
    }

    public MajorCreditFees(String major, Double creditFee) { 
    	this.major = major; this.creditFee = creditFee; 
    }
    
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Double getCreditFee() { return creditFee; }
    public void setCreditFee(Double creditFee) { this.creditFee = creditFee; }
}
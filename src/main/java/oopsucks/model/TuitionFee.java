package oopsucks.model;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Objects;

@Entity
@Table(name = "tuition_fee")
public class TuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "credit_fee", nullable = false)
    private Double creditFee;

    @Column(name = "total_credits", nullable = false)
    private Integer totalChargeableCredits;

    @Column(name = "tuition", nullable = false)
    private Double tuition;

    @Column(name = "status", nullable = false)
    private Boolean status;


    public TuitionFee() {
        this.status = false; 
    }

  
    public TuitionFee(String studentId, Integer semester, Integer totalChargeableCredits) {
        this.studentId = studentId;
        this.semester = semester;
        this.totalChargeableCredits = totalChargeableCredits;

        // Get student's major and set credit fee
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy sinh viên với ID: " + studentId);
        }

        String major = student.getMajor();
        if (major == null || major.isEmpty()) {
            throw new IllegalArgumentException("Sinh viên " + studentId + " không có thông tin ngành học");
        }

        this.creditFee = getCreditFeeByMajor(major);
        this.tuition = this.creditFee * totalChargeableCredits;
        this.status = false; 
    }


    public TuitionFee(String studentId, Integer semester, Double creditFee, Integer totalChargeableCredits) {
        this.studentId = studentId;
        this.semester = semester;
        this.creditFee = creditFee;
        this.totalChargeableCredits = totalChargeableCredits;
        this.tuition = creditFee * totalChargeableCredits;
        this.status = false; // Default payment status is false (unpaid)
    }


    private Double getCreditFeeByMajor(String major) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT creditFee FROM MajorCreditFees WHERE major = :major", Double.class);
            query.setParameter("major", major);
            Double fee = query.uniqueResultOptional().orElse(null);
            if (fee == null) {
                throw new IllegalArgumentException("Không tìm thấy phí tín chỉ cho ngành: " + major);
            }
            return fee;
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi lấy phí tín chỉ cho ngành " + major + ": " + e.getMessage(), e);
        }
    }


    public void setCreditFeeByMajor(String studentId) {
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy sinh viên với ID: " + studentId);
        }

        String major = student.getMajor();
        if (major == null || major.isEmpty()) {
            throw new IllegalArgumentException("Sinh viên " + studentId + " không có thông tin ngành học");
        }

        this.creditFee = getCreditFeeByMajor(major);
        this.calculateTuition();
    }

    private void calculateTuition() {
        if (this.creditFee != null && this.totalChargeableCredits != null) {
            this.tuition = this.creditFee * this.totalChargeableCredits;
        } else {
            this.tuition = 0.0;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    
    public Double getCreditFee() { return creditFee; }
    public void setCreditFee(Double creditFee) { this.creditFee = creditFee; this.calculateTuition(); }
    
    public Integer getTotalChargeableCredits() { return totalChargeableCredits; }
    public void setTotalChargeableCredits(Integer totalChargeableCredits) { this.totalChargeableCredits = totalChargeableCredits; this.calculateTuition(); }
    
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }
    
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TuitionFee that = (TuitionFee) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
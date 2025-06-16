package oopsucks.model;

import jakarta.persistence.*;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseTuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    protected Long id;

    @Column(name = "student_id", nullable = false)
    protected String studentId;

    @Column(name = "semester", nullable = false)
    protected Integer semester;

    @Column(name = "status", nullable = false)
    protected Boolean status;

    public BaseTuitionFee() {
        this.status = false;
    }

    public BaseTuitionFee(String studentId, Integer semester) {
        this.studentId = studentId;
        this.semester = semester;
        this.status = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }


    // Abstract method để các class con implement tính toán phí riêng
    public abstract Double calculateFee();
    
    // Abstract method để lấy tổng phí
    public abstract Double getTotalFee();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTuitionFee that = (BaseTuitionFee) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
package oopsucks.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "annual_tuition_fee")
public class AnnualTuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "semester_fee", nullable = false)
    private Double semesterFee;

    @Column(name = "status", nullable = false)
    private Boolean status;

    public AnnualTuitionFee() {
        this.status = false;
    }

    public AnnualTuitionFee(String studentId, Integer semester, Double semesterFee) {
        this.studentId = studentId;
        this.semester = semester;
        this.semesterFee = semesterFee;
        this.status = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    
    public Double getSemesterFee() { return semesterFee; }
    public void setSemesterFee(Double semesterFee) { this.semesterFee = semesterFee; }
    
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnualTuitionFee that = (AnnualTuitionFee) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
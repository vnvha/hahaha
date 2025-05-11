package oopsucks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Objects;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id", nullable = false, unique = true)
    private Long gradeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clazz_id", nullable = false)
    private Clazz clazz;

    @Column(name = "midterm_score")
    @Min(0) @Max(10)
    private Float midtermScore;

    @Column(name = "final_score")
    @Min(0) @Max(10)
    private Float finalScore;

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "letter_grade")
    private String letterGrade;

    @Column(name = "grade_point")
    private Float gradePoint;


    public Grade() {
        this.midtermScore = null;
        this.finalScore = null;
        this.totalScore = null;
        this.letterGrade = null;
        this.gradePoint = null;
    }

    public Grade(Student student, Clazz clazz, Float midtermScore, Float finalScore) {
        this.student = student;
        this.clazz = clazz;
        setMidtermScore(midtermScore);
        setFinalScore(finalScore);
    }


    public Long getGradeID() { return gradeID; }
    public void setGradeID(Long gradeID) { this.gradeID = gradeID; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Clazz getClazz() { return clazz; }
    public void setClazz(Clazz clazz) { this.clazz = clazz; }

    public Float getMidtermScore() { return midtermScore; }
    public void setMidtermScore(Float midtermScore) {
        if (midtermScore != null && (midtermScore < 0 || midtermScore > 10)) {
            throw new IllegalArgumentException("Midterm score must be between 0 and 10");
        }
        this.midtermScore = midtermScore;
        updateTotalScoreAndGrades();
    }

    public Float getFinalScore() { return finalScore; }
    public void setFinalScore(Float finalScore) {
        if (finalScore != null && (finalScore < 0 || finalScore > 10)) {
            throw new IllegalArgumentException("Final score must be between 0 and 10");
        }
        this.finalScore = finalScore;
        updateTotalScoreAndGrades();
    }

    public Float getTotalScore() { return totalScore; }
    public void setTotalScore(Float totalScore) { this.totalScore = totalScore; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public Float getGradePoint() { return gradePoint; }
    public void setGradePoint(Float gradePoint) { this.gradePoint = gradePoint; }


    private Float calculateTotalScore() {
        if (midtermScore == null || finalScore == null || clazz == null || clazz.getCourse() == null) {
            return null;
        }
        float finalExamWeight = clazz.getCourse().getFinalExamWeight();
        return midtermScore * (1 - finalExamWeight) + finalScore * finalExamWeight;
    }

 
    private String calculateLetterGrade() {
        if (totalScore == null) {
            return null;
        }
        if (totalScore >= 9.5) return "A+";
        if (totalScore >= 8.5) return "A";
        if (totalScore >= 8.0) return "B+";
        if (totalScore >= 7.0) return "B";
        if (totalScore >= 6.5) return "C+";
        if (totalScore >= 5.5) return "C";
        if (totalScore >= 5.0) return "D+";
        if (totalScore >= 4.0) return "D";
        return "F";
    }

    // Tính điểm thang 4
    private Float calculateGradePoint() {
        if (letterGrade == null) {
            return null;
        }
        return switch (letterGrade) {
            case "A+" -> 4.0f;
            case "B+" -> 3.5f;
            case "B" -> 3.0f;
            case "C+" -> 2.5f;
            case "C" -> 2.0f;
            case "D+" -> 2.5f;
            case "D" -> 2.0f;
            case "F" -> 0.0f;
            default -> null;
        };
    }

    // Cập nhật điểm tổng, thang chữ và thang 4
    private void updateTotalScoreAndGrades() {
        this.totalScore = calculateTotalScore();
        this.letterGrade = calculateLetterGrade();
        this.gradePoint = calculateGradePoint();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return Objects.equals(gradeID, grade.gradeID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeID);
    }
}
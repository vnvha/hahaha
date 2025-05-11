package oopsucks.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "course_id", nullable = false, unique = true)
    private String courseID;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "credit_number", nullable = false)
    private Integer creditNumber;

    @Column(name = "institute", nullable = false)
    private String institute;

    @Column(name = "final_exam_weight", nullable = false)
    private Float finalExamWeight;

    @Column(name = "type_id", nullable = false)
    private String typeID;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_prerequisite",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "prereq_course_id")
    )
    private List<Course> prereqCourses = new ArrayList<>();

    // Constructor mặc định
    public Course() {
    }

    // Constructor đầy đủ
    public Course(String courseID, String courseName, int creditNumber, String institute,
                  float finalExamWeight, String typeID, boolean isMandatory, boolean isCompleted,
                  List<Course> prereqCourses) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.creditNumber = creditNumber;
        this.institute = institute;
        this.finalExamWeight = finalExamWeight;
        this.typeID = typeID;
        this.isMandatory = isMandatory;
        this.isCompleted = isCompleted;
        this.prereqCourses = prereqCourses != null ? prereqCourses : new ArrayList<>();
    }

    // Getter và Setter
    public String getCourseID() { return courseID; }
    public void setCourseID(String courseID) { this.courseID = courseID; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public int getCreditNumber() { return creditNumber; }
    public void setCreditNumber(int creditNumber) { this.creditNumber = creditNumber; }
    
    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }
    
    public float getFinalExamWeight() { return finalExamWeight; }
    public void setFinalExamWeight(float finalExamWeight) { this.finalExamWeight = finalExamWeight; }
    
    public String getTypeID() { return typeID; }
    public void setTypeID(String typeID) { this.typeID = typeID; }
    
    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { isMandatory = mandatory; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public List<Course> getPrereqCourses() { return prereqCourses; }
    public void setPrereqCourses(List<Course> prereqCourses) {
        this.prereqCourses = prereqCourses != null ? prereqCourses : new ArrayList<>();
    }
}
package oopsucks.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clazz")
public class Clazz {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clazz_id", nullable = false, unique = true)
    private Integer clazzID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "time", nullable = false)
    private String time;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "weeks", nullable = false)
    private String weeks;

    @Column(name = "room", nullable = false)
    private String room;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "registered_count", nullable = false)
    private Integer registeredCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToMany
    @JoinTable(
        name = "clazz_student",
        joinColumns = @JoinColumn(name = "clazz_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students;

    public Clazz(Course course, String credit, String time, String startTime, String endTime,
                 String dayOfWeek, String weeks, String room, int maxCapacity, int registeredCount,
                 Teacher teacher, List<Student> students) {
        this.course = course;
        this.time = time;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.weeks = weeks;
        this.room = room;
        this.maxCapacity = maxCapacity;
        this.registeredCount = registeredCount;
        this.teacher = teacher;
        this.students = students != null ? students : new ArrayList<>();
    }

    public int getClazzID() { return clazzID; }
    public Course getCourse() { return course; }
    public String getTime() { return time; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getWeeks() { return weeks; }
    public String getRoom() { return room; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getRegisteredCount() { return registeredCount; }
    public Teacher getTeacher() { return teacher; }
    public List<Student> getStudents() { return students; }

    public void setClazzID(int clazzID) { this.clazzID = clazzID; }
    public void setCourse(Course course) { this.course = course; }
    public void setTime(String time) { this.time = time; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setWeeks(String weeks) { this.weeks = weeks; }
    public void setRoom(String room) { this.room = room; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public void setStudents(List<Student> students) { this.students = students != null ? students : new ArrayList<>(); }

}
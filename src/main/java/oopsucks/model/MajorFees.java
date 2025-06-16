package oopsucks.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class MajorFees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "major", nullable = false)
    private String major;

    public MajorFees() {
    }

    public MajorFees(String major) {
        this.major = major;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}
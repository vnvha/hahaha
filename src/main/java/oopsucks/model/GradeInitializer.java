package oopsucks.model;

import org.hibernate.Hibernate;

public class GradeInitializer {
    private GradeDAO gradeDAO;

    public GradeInitializer() {
        this.gradeDAO = new GradeDAO();
    }

    public void initializeGrade(Student student, Clazz clazz) {
        try {
            Grade grade = new Grade();
            grade.setStudent(student);
            grade.setClazz(clazz);
            grade.setMidtermScore(null);
            grade.setFinalScore(null);

            Hibernate.initialize(clazz.getCourse());

            gradeDAO.saveGrade(grade);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi khởi tạo Grade: " + e.getMessage(), e);
        }
    }
}
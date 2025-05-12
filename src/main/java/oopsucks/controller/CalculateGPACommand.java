package oopsucks.controller;

import oopsucks.model.*;

import java.util.List;

public class CalculateGPACommand {
    private final Student student;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;

    public CalculateGPACommand(Student student, ClazzDAO clazzDAO, GradeDAO gradeDAO) {
        this.student = student;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
    }

    public GPAResult execute() {
        List<Clazz> studentClazzes = clazzDAO.getClazzesByStudent(student);
        if (studentClazzes == null || studentClazzes.isEmpty()) {
            return new GPAResult("GPA: N/A (Chưa đăng ký học phần)", 0.0f);
        }

        float totalWeightedPoints = 0;
        int totalCredits = 0;

        for (Clazz clazz : studentClazzes) {
            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
            if (grade != null && grade.getTotalScore() != null && grade.getGradePoint() != null && clazz.getCourse() != null) {
                int credits = clazz.getCourse().getCreditNumber();
                totalWeightedPoints += grade.getGradePoint() * credits;
                totalCredits += credits;
            }
        }

        if (totalCredits > 0) {
            float gpa = totalWeightedPoints / totalCredits;
            return new GPAResult("GPA: " + String.format("%.2f", gpa), gpa);
        } else {
            return new GPAResult("GPA: N/A (Chưa có điểm hợp lệ)", 0.0f);
        }
    }

    public static class GPAResult {
        private final String message;
        private final float gpa;

        public GPAResult(String message, float gpa) {
            this.message = message;
            this.gpa = gpa;
        }

        public String getMessage() {
            return message;
        }

        public float getGPA() {
            return gpa;
        }
    }
}


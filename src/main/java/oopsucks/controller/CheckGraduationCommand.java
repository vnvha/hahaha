package oopsucks.controller;

import oopsucks.model.*;

import java.util.ArrayList;
import java.util.List;

public class CheckGraduationCommand {
    private final Student student;
    private final List<Course> mandatoryCourses;
    private final List<Course> selectedOptionalCourses;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;

    public CheckGraduationCommand(Student student,
                                  List<Course> mandatoryCourses,
                                  List<Course> selectedOptionalCourses,
                                  ClazzDAO clazzDAO,
                                  GradeDAO gradeDAO) {
        this.student = student;
        this.mandatoryCourses = mandatoryCourses;
        this.selectedOptionalCourses = selectedOptionalCourses;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
    }

    public Result execute() {
        List<Course> coursesToCheck = new ArrayList<>(mandatoryCourses);
        coursesToCheck.addAll(selectedOptionalCourses);

        boolean allScoresValid = true;
        List<String> invalidCoursesList = new ArrayList<>();

        float totalWeightedPoints = 0;
        int totalCredits = 0;

        for (Course course : coursesToCheck) {
            Clazz registeredClazz = findRegisteredClazz(student, course);
            if (registeredClazz == null) {
                allScoresValid = false;
                invalidCoursesList.add(course.getCourseID() + " (chưa đăng ký)");
                continue;
            }

            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, registeredClazz);
            if (grade == null || grade.getTotalScore() == null || grade.getTotalScore() < 4.0f) {
                allScoresValid = false;
                String reason;
                if (grade == null) {
                    reason = "chưa có điểm";
                } else if (grade.getTotalScore() == null) {
                    reason = "đang chờ điểm";
                } else {
                    reason = "điểm " + String.format("%.2f", grade.getTotalScore());
                }
                invalidCoursesList.add(course.getCourseID() + " (" + reason + ")");
            }

            if (grade != null && grade.getGradePoint() != null) {
                int credits = course.getCreditNumber();
                totalWeightedPoints += grade.getGradePoint() * credits;
                totalCredits += credits;
            }
        }

        float gpa = totalCredits > 0 ? totalWeightedPoints / totalCredits : 0.0f;

        if (allScoresValid && totalCredits > 0) {
            return new Result(true, "Thỏa mãn điều kiện tốt nghiệp. GPA: " + String.format("%.2f", gpa), gpa);
        } else {
            StringBuilder message = new StringBuilder("Không thỏa mãn điều kiện tốt nghiệp\n");

            if (!allScoresValid) {
                message.append("Các môn không đạt (yêu cầu totalScore >= 4.0):\n");
                for (String course : invalidCoursesList) {
                    message.append("- ").append(course).append("\n");
                }
            }

            if (totalCredits == 0) {
                message.append("Không có điểm hợp lệ để tính GPA.");
            } else {
                message.append("GPA: ").append(String.format("%.2f", gpa));
            }

            return new Result(false, message.toString(), gpa);
        }
    }

    private Clazz findRegisteredClazz(Student student, Course course) {
        List<Clazz> studentClazzes = clazzDAO.getClazzesByStudent(student);
        for (Clazz clazz : studentClazzes) {
            if (clazz.getCourse() != null &&
                clazz.getCourse().getCourseID().equals(course.getCourseID())) {
                return clazz;
            }
        }
        return null;
    }

    public static class Result {
        private final boolean isQualified;
        private final String message;
        private final float gpa;

        public Result(boolean isQualified, String message, float gpa) {
            this.isQualified = isQualified;
            this.message = message;
            this.gpa = gpa;
        }

        public boolean isQualified() {
            return isQualified;
        }

        public String getMessage() {
            return message;
        }

        public float getGPA() {
            return gpa;
        }
    }
}

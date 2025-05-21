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
    private final TuitionFeeDAO tuitionFeeDAO; // Thêm TuitionFeeDAO

    public CheckGraduationCommand(Student student,
                                  List<Course> mandatoryCourses,
                                  List<Course> selectedOptionalCourses,
                                  ClazzDAO clazzDAO,
                                  GradeDAO gradeDAO,
                                  TuitionFeeDAO tuitionFeeDAO) { // Thêm tham số TuitionFeeDAO
        this.student = student;
        this.mandatoryCourses = mandatoryCourses;
        this.selectedOptionalCourses = selectedOptionalCourses;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
        this.tuitionFeeDAO = tuitionFeeDAO; // Lưu lại tham số TuitionFeeDAO
    }

    public Result execute() {
        List<Course> coursesToCheck = new ArrayList<>(mandatoryCourses);
        coursesToCheck.addAll(selectedOptionalCourses);

        boolean allScoresValid = true;
        List<String> invalidCoursesList = new ArrayList<>();

        float totalWeightedPoints = 0;
        int totalCredits = 0;
        String studentInstitute = student.getInstitute();

        for (Course course : coursesToCheck) {
            // Chỉ kiểm tra và tính điểm cho các khóa học có cùng institute với sinh viên
            if (!course.getInstitute().equals(studentInstitute)) {
                continue;
            }
            
            Clazz registeredClazz = findRegisteredClazz(student, course);
            if (registeredClazz == null) {
                allScoresValid = false;
                invalidCoursesList.add(course.getCourseID() + " (chưa đăng ký)");
                continue;
            }

            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, registeredClazz);
            if (grade == null || grade.getTotalScore() == null || grade.getGradePoint() < 2.0f) {
                allScoresValid = false;
                String reason;
                if (grade == null) {
                    reason = "chưa có điểm";
                } else if (grade.getTotalScore() == null) {
                    reason = "đang chờ điểm";
                } else {
                    reason = "điểm: " + grade.getLetterGrade();
                }
                invalidCoursesList.add(course.getCourseID() + " (" + reason + ")");
            }

            if (grade != null && grade.getGradePoint() != null) {
                int credits = course.getCreditNumber();
                totalWeightedPoints += grade.getGradePoint() * credits;
                totalCredits += credits;
            }
        }

        // Kiểm tra học phí
        List<TuitionFee> tuitionFees = tuitionFeeDAO.getTuitionFeesByStudent(student.getUserID());
        boolean hasPendingTuition = false;
        List<Integer> pendingSemesters = new ArrayList<>();
        
        for (TuitionFee fee : tuitionFees) {
            if (!fee.getStatus()) { // Nếu chưa thanh toán
                hasPendingTuition = true;
                pendingSemesters.add(fee.getSemester());
            }
        }

        float gpa = totalCredits > 0 ? totalWeightedPoints / totalCredits : 0.0f;

        if (allScoresValid && !hasPendingTuition && totalCredits > 0) {
            return new Result(true, "Thỏa mãn điều kiện tốt nghiệp. GPA: " + String.format("%.2f", gpa), gpa);
        } else {
            StringBuilder message = new StringBuilder("Không thỏa mãn điều kiện tốt nghiệp\n");

            if (!allScoresValid) {
                message.append("Các môn không đạt:\n");
                for (String course : invalidCoursesList) {
                    message.append("- ").append(course).append("\n");
                }
            }

            if (hasPendingTuition) {
                message.append("Còn nợ học phí học kỳ: ");
                for (int i = 0; i < pendingSemesters.size(); i++) {
                    message.append(pendingSemesters.get(i));
                    if (i < pendingSemesters.size() - 1) {
                        message.append(", ");
                    }
                }
                message.append("\n");
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
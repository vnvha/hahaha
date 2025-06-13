package oopsucks.controller;

import oopsucks.model.*;
import java.util.ArrayList;
import java.util.List;

public class CheckGraduationCommand extends BaseCommand<CheckGraduationCommand.Result> {
    private final Student student;
    private final List<Course> mandatoryCourses;
    private final List<Course> selectedOptionalCourses;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final TuitionFeeDAO tuitionFeeDAO;

    public CheckGraduationCommand(Student student,
                                  List<Course> mandatoryCourses,
                                  List<Course> selectedOptionalCourses,
                                  ClazzDAO clazzDAO,
                                  GradeDAO gradeDAO,
                                  TuitionFeeDAO tuitionFeeDAO) {
        this.student = student;
        this.mandatoryCourses = mandatoryCourses;
        this.selectedOptionalCourses = selectedOptionalCourses;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
        this.tuitionFeeDAO = tuitionFeeDAO;
    }

    @Override
    protected Result doExecute() throws CommandException {
        List<Course> coursesToCheck = new ArrayList<>(mandatoryCourses);
        coursesToCheck.addAll(selectedOptionalCourses);

        boolean allScoresValid = true;
        List<String> invalidCoursesList = new ArrayList<>();
        float totalWeightedPoints = 0;
        int totalCredits = 0;
        String studentInstitute = student.getInstitute();

        for (Course course : coursesToCheck) {
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

        // Tính GPA
        float gpa = totalCredits > 0 ? totalWeightedPoints / totalCredits : 0.0f;

        // Xác định điều kiện tốt nghiệp
        boolean qualified = allScoresValid && !hasPendingTuition && totalCredits > 0;

        // Xây dựng thông điệp
        StringBuilder message = new StringBuilder();
        if (qualified) {
            message.append("Đủ điều kiện tốt nghiệp! Điểm trung bình tích lũy: ")
                   .append(String.format("%.2f", gpa));
        } else {
            message.append("Chưa đủ điều kiện tốt nghiệp: ");

            if (!allScoresValid) {
                message.append("\nCác môn chưa hoàn thành: ");
                for (int i = 0; i < invalidCoursesList.size(); i++) {
                    message.append(invalidCoursesList.get(i));
                    if (i < invalidCoursesList.size() - 1) {
                        message.append(", ");
                    }
                }
            }

            if (hasPendingTuition) {
                message.append("\nCòn nợ học phí học kỳ: ");
                for (int i = 0; i < pendingSemesters.size(); i++) {
                    message.append(pendingSemesters.get(i));
                    if (i < pendingSemesters.size() - 1) {
                        message.append(", ");
                    }
                }
            }

            if (totalCredits == 0) {
                message.append("\nKhông có điểm hợp lệ để tính GPA.");
            } else {
                message.append("\nĐiểm trung bình tích lũy: ").append(String.format("%.2f", gpa));
            }
        }

        return new Result(message.toString(), qualified, gpa);
    }

    @Override
    public boolean validate() {
        return student != null &&
               mandatoryCourses != null && !mandatoryCourses.isEmpty() &&
               selectedOptionalCourses != null &&
               clazzDAO != null &&
               gradeDAO != null &&
               tuitionFeeDAO != null;
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
        private final String message;
        private final boolean qualified;
        private final float gpa;

        public Result(String message, boolean qualified, float gpa) {
            this.message = message;
            this.qualified = qualified;
            this.gpa = gpa;
        }

        public String getMessage() {
            return message;
        }

        public boolean isQualified() {
            return qualified;
        }

        public float getGPA() {
            return gpa;
        }
    }
}
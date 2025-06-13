package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;
import java.util.ArrayList;

public class CheckAnnualGraduationCommand extends BaseCommand<CheckAnnualGraduationCommand.Result> {
    private final Student student;
    private final List<Course> courses;
    private final GradeDAO gradeDAO;
    
    public CheckAnnualGraduationCommand(Student student, List<Course> courses, GradeDAO gradeDAO) {
        this.student = student;
        this.courses = courses;
        this.gradeDAO = gradeDAO;
    }
    
    @Override
    protected Result doExecute() throws CommandException {
        // Kiểm tra hoàn thành tất cả các môn (>=D)
        boolean allCoursesCompleted = true;
        List<String> failedCourses = new ArrayList<>();
        
        for (Course course : courses) {
            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(student.getUserID(), course.getCourseID());
            
            Grade bestGrade = null;
            Float highestTotalScore = null;
            
            for (Grade grade : grades) {
                Float totalScore = grade.getTotalScore();
                if (totalScore != null && (highestTotalScore == null || totalScore > highestTotalScore)) {
                    highestTotalScore = totalScore;
                    bestGrade = grade;
                }
            }
            
            if (bestGrade == null || bestGrade.getLetterGrade() == null || bestGrade.getLetterGrade().equals("F")) {
                allCoursesCompleted = false;
                failedCourses.add(course.getCourseName());
            }
        }
        
        // Tính điểm trung bình tích lũy
        float totalPoints = 0;
        int courseCount = 0;
        
        for (Course course : courses) {
            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(student.getUserID(), course.getCourseID());
            
            Grade bestGrade = null;
            Float highestTotalScore = null;
            
            for (Grade grade : grades) {
                Float totalScore = grade.getTotalScore();
                if (totalScore != null && (highestTotalScore == null || totalScore > highestTotalScore)) {
                    highestTotalScore = totalScore;
                    bestGrade = grade;
                }
            }
            
            if (bestGrade != null && bestGrade.getGradePoint() != null) {
                totalPoints += bestGrade.getGradePoint();
                courseCount++;
            }
        }
        
        float gpa = (courseCount > 0) ? (totalPoints / courseCount) : 0.0f;
        boolean gpaQualified = gpa >= 5.0; // Giả sử điểm trung bình tích lũy phải >= 5.0
        
        StringBuilder message = new StringBuilder();
        boolean qualified = allCoursesCompleted && gpaQualified;
        
        if (qualified) {
            message.append("Đủ điều kiện tốt nghiệp! Điểm trung bình tích lũy: ")
                   .append(String.format("%.2f", gpa));
        } else {
            message.append("Chưa đủ điều kiện tốt nghiệp: ");
            
            if (!allCoursesCompleted) {
                message.append("\nCác môn chưa hoàn thành: ");
                for (int i = 0; i < failedCourses.size(); i++) {
                    message.append(failedCourses.get(i));
                    if (i < failedCourses.size() - 1) {
                        message.append(", ");
                    }
                }
            }
            
            if (!gpaQualified) {
                message.append("\nĐiểm trung bình tích lũy: ").append(String.format("%.2f", gpa))
                       .append(" (yêu cầu >= 5.0)");
            }
        }
        
        return new Result(message.toString(), qualified);
    }
    
    @Override
    public boolean validate() {
        return student != null && courses != null && !courses.isEmpty() && gradeDAO != null;
    }
    
    /**
     * Result class to store the graduation check result
     */
    public static class Result {
        private final String message;
        private final boolean qualified;
        
        public Result(String message, boolean qualified) {
            this.message = message;
            this.qualified = qualified;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isQualified() {
            return qualified;
        }
    }
}
package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Command class to check graduation requirements for annual system students
 */
public class CheckAnnualGraduationCommand {
    private final Student student;
    private final List<Course> courses;
    private final GradeDAO gradeDAO;
    
    public CheckAnnualGraduationCommand(Student student, List<Course> courses, GradeDAO gradeDAO) {
        this.student = student;
        this.courses = courses;
        this.gradeDAO = gradeDAO;
    }
    
    /**
     * Executes the graduation check for annual system students
     * Annual students must:
     * 1. Complete all courses (grade >= D)
     * 2. Have a cumulative GPA >= 1.0
     */
    public Result execute() {
        if (student == null || courses == null || courses.isEmpty()) {
            return new Result("Không thể kiểm tra: Thiếu thông tin sinh viên hoặc khóa học", false);
        }
        
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
        boolean gpaQualified = gpa >= 1.0; // Giả sử điểm trung bình tích lũy phải >= 1.0
        
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
                       .append(" (yêu cầu >= 1.0)");
            }
        }
        
        return new Result(message.toString(), qualified);
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
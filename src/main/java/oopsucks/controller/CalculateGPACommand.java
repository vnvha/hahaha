package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;

public class CalculateGPACommand {
    private final Student student;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final boolean isCreditBasedSystem;

    public CalculateGPACommand(Student student, CourseDAO courseDAO, ClazzDAO clazzDAO, GradeDAO gradeDAO, boolean isCreditBasedSystem) {
        this.student = student;
        this.courseDAO = courseDAO;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
        this.isCreditBasedSystem = isCreditBasedSystem;
    }


    public GPAResult execute() {
        if (student == null) {
            return new GPAResult(isCreditBasedSystem ? "GPA: N/A" : "Điểm TB tích lũy: N/A", 0.0f);
        }

        String studentInstitute = student.getInstitute();

        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses == null || allCourses.isEmpty()) {
            return new GPAResult("GPA: N/A", 0.0f);
        }
        List<Clazz> studentClazzes = clazzDAO.getClazzesByStudent(student);
        if (studentClazzes == null || studentClazzes.isEmpty()) {
            return new GPAResult("GPA: N/A", 0.0f);
        }

        float totalWeightedPoints = 0;
        int totalCredits = 0;
        
        float totalScores = 0;
        int courseCount = 0;

        for (Course course : allCourses) {
            if (!course.getInstitute().equals(studentInstitute)) {
                continue;
            }

            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(student.getUserID(), course.getCourseID());
            Grade bestGrade = null;
            Float highestGradePoint = null;

            for (Grade grade : grades) {
                Float gradePoint = grade.getGradePoint();
                if (gradePoint != null && (highestGradePoint == null || gradePoint > highestGradePoint)) {
                    highestGradePoint = gradePoint;
                    bestGrade = grade;
                }
            }

            if (bestGrade != null && bestGrade.getGradePoint() != null) {
                int credits = course.getCreditNumber();
                totalWeightedPoints += bestGrade.getGradePoint() * credits;
                totalCredits += credits;
                
                totalScores += bestGrade.getTotalScore();
                courseCount++;
            }
        }
        if (isCreditBasedSystem) {
        	 if (totalCredits > 0) {
                 float gpa = totalWeightedPoints / totalCredits;
                 return new GPAResult("GPA: " + String.format("%.2f", gpa), gpa);
             } else {
                 return new GPAResult("GPA: N/A", 0.0f);
             }
        } else {
        	if (courseCount > 0) {
                float gpa = totalScores / courseCount;
                return new GPAResult("Điểm TB tích lũy: " + String.format("%.2f", gpa), gpa);
            } else {
                return new GPAResult("Điểm TB tích lũy: N/A", 0.0f);
            }
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




package oopsucks.controller;

import oopsucks.model.*;

import java.util.List;

public class CalculateTotalCreditsCommand extends BaseCommand<Void> {
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final GradeDAO gradeDAO;
    private final String studentID;

    public CalculateTotalCreditsCommand(UserDAO userDAO, CourseDAO courseDAO, GradeDAO gradeDAO, String studentID) {
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.gradeDAO = gradeDAO;
        this.studentID = studentID;
    }

    @Override
    protected Void doExecute() throws CommandException {
        Student student = userDAO.getStudent(studentID);
        if (!(student instanceof CreditBasedStudent)) {
            return null;
        }
        String studentInstitute = student.getInstitute();
        List<Course> allCourses = courseDAO.getAllCourses();
        int totalCreditsEarned = 0;

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
            if (bestGrade != null && bestGrade.getLetterGrade() != null && !bestGrade.getLetterGrade().equals("F")) {
                totalCreditsEarned += course.getCreditNumber();
            }
        }

        ((CreditBasedStudent) student).setTotalCredits(totalCreditsEarned);
        userDAO.updateStudent(student);
        return null;
    }

    @Override
    public boolean validate() {
        return userDAO != null && courseDAO != null && gradeDAO != null &&
               studentID != null && !studentID.trim().isEmpty();
    }
}

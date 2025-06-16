package oopsucks.controller;

import oopsucks.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class LoadCourseDataCommand extends BaseCommand<Void> {
    private final DefaultTableModel tableModel;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final boolean isCreditBasedSystem;
    private final String studentID;

    public LoadCourseDataCommand(DefaultTableModel tableModel,
                                 UserDAO userDAO, CourseDAO courseDAO, ClazzDAO clazzDAO, GradeDAO gradeDAO,
                                 boolean isCreditBasedSystem,
                                 String studentID) {
        this.tableModel = tableModel;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
        this.isCreditBasedSystem = isCreditBasedSystem;
        this.studentID = studentID;
    }

    @Override
    protected Void doExecute() throws CommandException {
        tableModel.setRowCount(0);
        Student student = userDAO.getStudent(studentID);
        if (student == null) {
            throw new CommandException("Không thể tải thông tin sinh viên");
        }

        String studentInstitute = student.getInstitute();
        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses == null || allCourses.isEmpty()) {
            throw new CommandException("Không có khóa học nào để hiển thị");
        }

        for (Course course : allCourses) {
            if (!course.getInstitute().equals(studentInstitute)) {
                continue;
            }

            Object[] rowData = isCreditBasedSystem ? new Object[10] : new Object[8];
            rowData[0] = course.getCourseID();
            rowData[1] = course.getCourseName();
            if (isCreditBasedSystem) {
                rowData[2] = course.getCreditNumber();
                rowData[3] = course.getInstitute();
            } else {
                rowData[2] = course.getInstitute();
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

            int baseIndex = isCreditBasedSystem ? 4 : 3;
            if (bestGrade != null) {
                rowData[baseIndex] = formatGrade(bestGrade.getMidtermScore());
                rowData[baseIndex + 1] = formatGrade(bestGrade.getFinalScore());
                rowData[baseIndex + 2] = formatGrade(bestGrade.getTotalScore());
                rowData[baseIndex + 3] = bestGrade.getLetterGrade();
                rowData[baseIndex + 4] = formatGrade(bestGrade.getGradePoint());
            } else {
                rowData[baseIndex] = "";
                rowData[baseIndex + 1] = "";
                rowData[baseIndex + 2] = "";
                rowData[baseIndex + 3] = "";
                rowData[baseIndex + 4] = "";
            }

            if (isCreditBasedSystem) {
                rowData[9] = course.isMandatory();
            }

            tableModel.addRow(rowData);
        }

        return null;
    }

    @Override
    public boolean validate() {
        return tableModel != null && userDAO != null && courseDAO != null &&
               studentID != null && !studentID.trim().isEmpty();
    }

    private String formatGrade(Float grade) {
        return grade != null ? String.format("%.2f", grade) : "";
    }
}
package oopsucks.controller;

import oopsucks.view.TrainingProgramPanel;
import oopsucks.model.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class LoadCourseDataCommand {
    private final TrainingProgramPanel panel;
    private final DefaultTableModel tableModel;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;

    public LoadCourseDataCommand(TrainingProgramPanel panel, DefaultTableModel tableModel,
                                UserDAO userDAO, CourseDAO courseDAO, ClazzDAO clazzDAO, GradeDAO gradeDAO) {
        this.panel = panel;
        this.tableModel = tableModel;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
    }

    public void loadCourseData() {
        tableModel.setRowCount(0);
        Student student = userDAO.getStudent(panel.getStudentAccountName());
        if (student == null) {
            panel.getResultLabel().setText("Lỗi: Không thể tải thông tin sinh viên");
            return;
        }

        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses == null || allCourses.isEmpty()) {
            panel.getResultLabel().setText("Lỗi: Không có khóa học nào để hiển thị");
            return;
        }

        for (Course course : allCourses) {
            Object[] rowData = new Object[10];

            rowData[0] = course.getCourseID();
            rowData[1] = course.getCourseName();
            rowData[2] = course.getCreditNumber();
            rowData[3] = course.getInstitute();

            Clazz registeredClazz = panel.findRegisteredClazz(student, course);
            if (registeredClazz != null) {
                Grade grade = gradeDAO.getGradeByStudentAndClazz(student, registeredClazz);
                if (grade != null) {
                    rowData[4] = panel.formatGrade(grade.getMidtermScore());
                    rowData[5] = panel.formatGrade(grade.getFinalScore());
                    rowData[6] = panel.formatGrade(grade.getTotalScore());
                    rowData[7] = grade.getLetterGrade();
                    rowData[8] = panel.formatGrade(grade.getGradePoint());
                }
            }

            rowData[9] = course.isMandatory();
            tableModel.addRow(rowData);
        }
    }
}
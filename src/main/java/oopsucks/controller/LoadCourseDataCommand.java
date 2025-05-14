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

            // Điền thông tin cơ bản của Course
            rowData[0] = course.getCourseID();
            rowData[1] = course.getCourseName();
            rowData[2] = course.getCreditNumber();
            rowData[3] = course.getInstitute();

            // Lấy tất cả các Grade cho sinh viên và khóa học
            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(student.getUserID(), course.getCourseID());
            Grade bestGrade = null;
            Float highestTotalScore = null;

            // Tìm Grade có totalScore cao nhất
            for (Grade grade : grades) {
                Float totalScore = grade.getTotalScore();
                if (totalScore != null && (highestTotalScore == null || totalScore > highestTotalScore)) {
                    highestTotalScore = totalScore;
                    bestGrade = grade;
                }
            }

            // Nếu có Grade hợp lệ, điền thông tin điểm
            if (bestGrade != null) {
                rowData[4] = panel.formatGrade(bestGrade.getMidtermScore()); // Điểm quá trình
                rowData[5] = panel.formatGrade(bestGrade.getFinalScore()); // Điểm cuối kỳ
                rowData[6] = panel.formatGrade(bestGrade.getTotalScore()); // Điểm tổng kết
                rowData[7] = bestGrade.getLetterGrade(); // Điểm chữ
                rowData[8] = panel.formatGrade(bestGrade.getGradePoint()); // Điểm hệ 4
            } else {
                rowData[4] = ""; // Điểm quá trình
                rowData[5] = ""; // Điểm cuối kỳ
                rowData[6] = ""; // Điểm tổng kết
                rowData[7] = ""; // Điểm chữ
                rowData[8] = ""; // Điểm hệ 4
            }

            rowData[9] = course.isMandatory();
            tableModel.addRow(rowData);
        }
    }
}
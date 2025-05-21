package oopsucks.controller;

import oopsucks.model.*;
import oopsucks.view.AnnualTrainingProgramPanel;
import oopsucks.view.TrainingProgramPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class LoadCourseDataCommand {
    private final JPanel panel; // Sử dụng kiểu chung JPanel để hỗ trợ cả hai panel
    private final DefaultTableModel tableModel;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final boolean isCreditBasedSystem; //xác định hệ tín chỉ hay niên chế

    public LoadCourseDataCommand(JPanel panel, DefaultTableModel tableModel,
                                 UserDAO userDAO, CourseDAO courseDAO, ClazzDAO clazzDAO, GradeDAO gradeDAO,
                                 boolean isCreditBasedSystem) {
        this.panel = panel;
        this.tableModel = tableModel;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.clazzDAO = clazzDAO;
        this.gradeDAO = gradeDAO;
        this.isCreditBasedSystem = isCreditBasedSystem;
    }

    public void loadCourseData() {
        tableModel.setRowCount(0);
        String studentID = getStudentID();
        Student student = userDAO.getStudent(studentID);
        if (student == null) {
            setResultLabelText("Lỗi: Không thể tải thông tin sinh viên");
            return;
        }

        String studentInstitute = student.getInstitute();
        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses == null || allCourses.isEmpty()) {
            setResultLabelText("Lỗi: Không có khóa học nào để hiển thị");
            return;
        }

        int totalCreditsEarned = 0;

        for (Course course : allCourses) {
            if (!course.getInstitute().equals(studentInstitute)) {
                continue; // Bỏ qua nếu institute không khớp
            }

            Object[] rowData = isCreditBasedSystem ? new Object[10] : new Object[8];

            // Điền thông tin cơ bản của Course
            rowData[0] = course.getCourseID();
            rowData[1] = course.getCourseName();
            if (isCreditBasedSystem) {
                rowData[2] = course.getCreditNumber();
                rowData[3] = course.getInstitute();
            } else {
                rowData[2] = course.getInstitute();
            }

            // Lấy tất cả các Grade cho sinh viên và khóa học
            List<Grade> grades = gradeDAO.getGradesByStudentAndCourse(student.getUserID(), course.getCourseID());
            Grade bestGrade = null;
            Float highestGradePoint = null;

            // Tìm Grade có totalScore cao nhất
            for (Grade grade : grades) {
                Float gradePoint = grade.getGradePoint();
                if (gradePoint != null && (highestGradePoint == null || gradePoint > highestGradePoint)) {
                    highestGradePoint = gradePoint;
                    bestGrade = grade;
                }
            }

            // Điền thông tin điểm
            int baseIndex = isCreditBasedSystem ? 4 : 3;
            if (bestGrade != null) {
                rowData[baseIndex] = formatGrade(bestGrade.getMidtermScore());
                rowData[baseIndex + 1] = formatGrade(bestGrade.getFinalScore());
                rowData[baseIndex + 2] = formatGrade(bestGrade.getTotalScore());
                rowData[baseIndex + 3] = bestGrade.getLetterGrade();
                rowData[baseIndex + 4] = formatGrade(bestGrade.getGradePoint());

                // Tính tổng tín chỉ cho hệ tín chỉ
                if (isCreditBasedSystem && bestGrade.getLetterGrade() != null) {
                    String letterGrade = bestGrade.getLetterGrade();
                    if (!letterGrade.equals("F")) {
                        totalCreditsEarned += course.getCreditNumber();
                    }
                }
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

        // Cập nhật tổng số tín chỉ cho hệ tín chỉ
        if (isCreditBasedSystem && student instanceof CreditBasedStudent) {
            updateTotalCredits((CreditBasedStudent) student, totalCreditsEarned);
        }
    }

    private void updateTotalCredits(CreditBasedStudent student, int totalCreditsEarned) {
        student.setTotalCredits(totalCreditsEarned);
        userDAO.updateStudent(student);
    }

    private String formatGrade(Float grade) {
        if (grade == null) {
            return "";
        }
        return String.format("%.2f", grade);
    }

    private String getStudentID() {
        if (panel instanceof AnnualTrainingProgramPanel) {
            return ((AnnualTrainingProgramPanel) panel).studentID();
        } else if (panel instanceof TrainingProgramPanel) {
            return ((TrainingProgramPanel) panel).studentID();
        }
        throw new IllegalStateException("Panel không được hỗ trợ");
    }

    private void setResultLabelText(String text) {
        if (panel instanceof AnnualTrainingProgramPanel) {
            ((AnnualTrainingProgramPanel) panel).getResultLabel().setText(text);
        } else if (panel instanceof TrainingProgramPanel) {
            ((TrainingProgramPanel) panel).getResultLabel().setText(text);
        }
    }
}
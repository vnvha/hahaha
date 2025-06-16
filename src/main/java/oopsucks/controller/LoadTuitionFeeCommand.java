package oopsucks.controller;

import oopsucks.model.*;
import oopsucks.model.ClazzDAO;
import oopsucks.model.AnnualTuitionFeeDAO;
import oopsucks.model.TuitionFeeDAO;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class LoadTuitionFeeCommand extends BaseCommand<List<Object[]>> {
    private final String studentID;
    private final Object tuitionFeeDAO;
    private final UserDAO userDAO;
    private final ClazzDAO clazzDAO;
    private final boolean isAnnualStudent;
    private String message;
    private Color messageColor;

    public LoadTuitionFeeCommand(String studentID, Object tuitionFeeDAO, UserDAO userDAO, boolean isAnnualStudent) {
        this.studentID = studentID;
        this.tuitionFeeDAO = tuitionFeeDAO;
        this.userDAO = userDAO;
        this.clazzDAO = new ClazzDAO();
        this.isAnnualStudent = isAnnualStudent;
        this.message = "";
        this.messageColor = Color.RED;
    }

    @Override
    protected List<Object[]> doExecute() throws CommandException {
        List<Object[]> tableData = new ArrayList<>();

        // Check student existence
        Student student = userDAO.getStudent(studentID);
        if (student == null) {
            message = "Không tìm thấy thông tin sinh viên!";
            return tableData;
        }

        // For annual students, verify YearBasedStudent
        if (isAnnualStudent && !(student instanceof YearBasedStudent)) {
            message = "Sinh viên không thuộc hệ niên chế!";
            return tableData;
        }

        // Get registered semesters
        List<Integer> registeredSemesters = clazzDAO.getRegisteredSemestersByStudent(studentID);
        if (registeredSemesters.isEmpty()) {
            message = "Sinh viên chưa đăng ký lớp học nào!";
            return tableData;
        }

        // Update tuition fees for registered semesters
        boolean hasData = false;
        if (isAnnualStudent) {
            AnnualTuitionFeeDAO annualDAO = (AnnualTuitionFeeDAO) tuitionFeeDAO;
            for (int semester : registeredSemesters) {
                AnnualTuitionFee fee = annualDAO.calculateAndSaveAnnualTuitionFee(studentID, semester);
                if (fee != null) {
                    hasData = true;
                }
            }
            // Load tuition fee data
            List<AnnualTuitionFee> tuitionFees = annualDAO.getAnnualTuitionFeesByStudent(studentID);
            for (AnnualTuitionFee fee : tuitionFees) {
                String status = fee.getStatus() ? "Đã thanh toán" : "Chưa thanh toán";
                tableData.add(new Object[]{
                    fee.getSemester(),
                    String.format("%.2f", fee.getSemesterFee()),
                    status
                });
            }
        } else {
            TuitionFeeDAO creditDAO = (TuitionFeeDAO) tuitionFeeDAO;
            for (int semester : registeredSemesters) {
                TuitionFee fee = creditDAO.calculateAndSaveTuitionFee(studentID, semester);
                if (fee != null) {
                    hasData = true;
                }
            }
            // Load tuition fee data
            List<TuitionFee> tuitionFees = creditDAO.getTuitionFeesByStudent(studentID);
            for (TuitionFee fee : tuitionFees) {
                String status = fee.getStatus() ? "Đã thanh toán" : "Chưa thanh toán";
                tableData.add(new Object[]{
                    fee.getSemester(),
                    fee.getTotalChargeableCredits(),
                    String.format("%.2f", fee.getCreditFee()),
                    String.format("%.2f", fee.getTuition()),
                    status
                });
            }
        }

        // Set appropriate message
        if (!hasData) {
            message = "Không có thông tin học phí cho sinh viên này!";
        } else {
            message = "Đã hiển thị thông tin học phí!";
            messageColor = new Color(0, 128, 0);
        }

        return tableData;
    }

    @Override
    public boolean validate() {
        return studentID != null && !studentID.isEmpty() && tuitionFeeDAO != null && userDAO != null;
    }

    public String getMessage() {
        return message;
    }

    public Color getMessageColor() {
        return messageColor;
    }
}
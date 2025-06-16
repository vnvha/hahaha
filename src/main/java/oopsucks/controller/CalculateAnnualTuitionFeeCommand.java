package oopsucks.controller;

import oopsucks.model.*;

public class CalculateAnnualTuitionFeeCommand extends BaseCommand<AnnualTuitionFee> {
    private final String studentId;
    private final Integer semester;
    private final AnnualTuitionFeeDAO tuitionFeeDAO;
    private final UserDAO userDAO;

    public CalculateAnnualTuitionFeeCommand(String studentId, Integer semester) {
        this.studentId = studentId;
        this.semester = semester;
        this.tuitionFeeDAO = new AnnualTuitionFeeDAO();
        this.userDAO = new UserDAO();
    }

    @Override
    protected AnnualTuitionFee doExecute() throws CommandException {
        try {
            Student student = userDAO.getStudent(studentId);
            if (student == null) {
                throw new CommandException("Không tìm thấy sinh viên với ID: " + studentId);
            }

            if (!(student instanceof YearBasedStudent)) {
                throw new CommandException("Sinh viên " + studentId + " không thuộc hệ niên chế");
            }

            String major = student.getMajor();
            if (major == null || major.isEmpty()) {
                throw new CommandException("Sinh viên " + studentId + " không có thông tin ngành học");
            }

            Double semesterFee = tuitionFeeDAO.getSemesterFeeByMajor(major);

            AnnualTuitionFee existingFee = tuitionFeeDAO.getAnnualTuitionFeeByStudentAndSemester(studentId, semester);
            AnnualTuitionFee tuitionFee;
            if (existingFee != null) {
                existingFee.setSemesterFee(semesterFee);
                tuitionFee = existingFee;
            } else {
                tuitionFee = new AnnualTuitionFee(studentId, semester, semesterFee);
            }

            if (!tuitionFeeDAO.saveAnnualTuitionFee(tuitionFee)) {
                throw new CommandException("Lỗi khi lưu học phí cho sinh viên " + studentId + " kỳ " + semester);
            }

            return tuitionFee;
        } catch (IllegalArgumentException e) {
            throw new CommandException("Lỗi khi tính học phí: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CommandException("Lỗi không xác định khi tính học phí: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validate() {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        if (semester == null || semester <= 0) {
            return false;
        }
        Student student = userDAO.getStudent(studentId);
        return student != null && student instanceof YearBasedStudent;
    }
}
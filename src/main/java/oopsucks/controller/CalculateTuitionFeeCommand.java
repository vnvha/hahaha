package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;

public class CalculateTuitionFeeCommand extends BaseCommand<TuitionFee> {
    private TuitionFeeDAO tuitionFeeDAO;
    private UserDAO userDAO;
    private ClazzDAO clazzDAO;
    private String studentId;
    private Integer semester;
    private Double customCreditFee;

    public CalculateTuitionFeeCommand(String studentId, Integer semester) {
        this.tuitionFeeDAO = new TuitionFeeDAO();
        this.userDAO = new UserDAO();
        this.clazzDAO = new ClazzDAO();
        this.studentId = studentId;
        this.semester = semester;
        this.customCreditFee = null;
    }

    public CalculateTuitionFeeCommand(String studentId, Integer semester, Double customCreditFee) {
        this.tuitionFeeDAO = new TuitionFeeDAO();
        this.userDAO = new UserDAO();
        this.clazzDAO = new ClazzDAO();
        this.studentId = studentId;
        this.semester = semester;
        this.customCreditFee = customCreditFee;
    }

    @Override
    protected TuitionFee doExecute() throws CommandException {
        try {
            // Calculate total chargeable credits
            int totalChargeableCredits = calculateSemesterCredits();
            
            if (totalChargeableCredits <= 0) {
            throw new CommandException("Không có tín chỉ nào được đăng ký cho sinh viên " + 
                                     studentId + " trong kỳ " + semester);
            }

            // Check if tuition fee already exists
            TuitionFee existingFee = tuitionFeeDAO.getTuitionFeeByStudentAndSemester(studentId, semester);
            
            TuitionFee tuitionFee;
            if (existingFee != null) {
                // Update existing tuition fee
                tuitionFee = updateExistingTuitionFee(existingFee, totalChargeableCredits);
            } else {
                // Create new tuition fee
                tuitionFee = createNewTuitionFee(totalChargeableCredits);
            }
            
            // Save the tuition fee
            if (!tuitionFeeDAO.saveTuitionFee(tuitionFee)) {
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
        if (customCreditFee != null && customCreditFee <= 0) {
            return false;
        }
        
        // Validate student exists
        Student student = userDAO.getStudent(studentId);
        if (student == null) {
            return false;
        }
        
        return true;
    }
    
    private int calculateSemesterCredits() throws CommandException {
        Student student = userDAO.getStudent(studentId);
        if (student == null) {
            throw new CommandException("Không tìm thấy sinh viên với ID: " + studentId);
        }

        List<Clazz> clazzes = clazzDAO.getClazzesByStudentAndSemester(student, semester);
        int totalChargeableCredits = 0;

        for (Clazz clazz : clazzes) {
            Course course = clazz.getCourse();
            if (course != null) {
                totalChargeableCredits += course.getChargeableCredits();
            }
        }
        
        return totalChargeableCredits;
    }
    
    private TuitionFee updateExistingTuitionFee(TuitionFee existingFee, int totalChargeableCredits) 
            throws CommandException {
        existingFee.setTotalChargeableCredits(totalChargeableCredits);
        
        if (customCreditFee != null) {
            existingFee.setCreditFee(customCreditFee);
        } else {
            Student student = userDAO.getStudent(studentId);
            if (student == null || student.getMajor() == null) {
                throw new CommandException("Không tìm thấy thông tin ngành học cho sinh viên: " + studentId);
            }
            existingFee.setCreditFee(tuitionFeeDAO.getCreditFeeByMajor(student.getMajor()));
        }
        
        return existingFee;
    }
    
    private TuitionFee createNewTuitionFee(int totalChargeableCredits) throws CommandException {
        Double creditFee;
        if (customCreditFee != null) {
            creditFee = customCreditFee;
        } else {
            Student student = userDAO.getStudent(studentId);
            if (student == null || student.getMajor() == null) {
                throw new CommandException("Không tìm thấy thông tin ngành học cho sinh viên: " + studentId);
            }
            creditFee = tuitionFeeDAO.getCreditFeeByMajor(student.getMajor());
        }
        return new TuitionFee(studentId, semester, creditFee, totalChargeableCredits);
    }
}
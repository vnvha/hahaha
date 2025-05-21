package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;
import java.util.List;

public class FinishRegistrationCommand {
    private ClazzDAO clazzDAO;
    private GradeDAO gradeDAO;
    private String studentUserID; 

    public FinishRegistrationCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.studentUserID = studentUserID;
    }

    public String execute() {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class) // Sửa truy vấn
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                return "Không tìm thấy thông tin sinh viên với userID: " + studentUserID;
            }

            List<Clazz> registeredClasses = clazzDAO.getClazzesByStudent(student);
            if (registeredClasses.isEmpty()) {
                return "Bạn chưa đăng ký lớp nào!";
            }

            GradeInitializer gradeInitializer = new GradeInitializer();
            for (Clazz clazz : registeredClasses) {
                Grade existingGrade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
                if (existingGrade == null) {
                    gradeInitializer.initializeGrade(student, clazz);
                    System.out.println("Đã tạo điểm cho sinh viên " + student.getUserID() + " trong lớp " + clazz.getClazzID());
                } else {
                    gradeInitializer.initializeGrade(student, clazz);
                    System.out.println("Đã cập nhật điểm cho sinh viên " + student.getUserID() + " trong lớp " + clazz.getClazzID());
                }
            }
         // Sau phần lưu điểm
/*            
         // Tính và lưu học phí cho học kỳ hiện tại
            Integer currentSemester = null;
            if (!registeredClasses.isEmpty()) {
                currentSemester = registeredClasses.get(0).getSemester();
            }
         TuitionFeeDAO tuitionFeeDAO = new TuitionFeeDAO();
         TuitionFee tuitionFee = tuitionFeeDAO.calculateAndSaveTuitionFee(student.getUserID(), currentSemester);
         if (tuitionFee != null) {
             System.out.println("Đã tính học phí cho sinh viên " + student.getUserID() + 
                                " học kỳ " + currentSemester + ": " + tuitionFee.getTuition());
         } else {
             System.out.println("Không thể tính học phí cho sinh viên " + student.getUserID() + 
                                " học kỳ " + currentSemester);
         }
*/
            transaction.commit();
            return "Đăng ký học phần thành công!";
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Lỗi khi hoàn thành đăng ký: " + e.getMessage();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
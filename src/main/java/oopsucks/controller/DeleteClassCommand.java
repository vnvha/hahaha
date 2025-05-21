package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;

public class DeleteClassCommand {
    private ClazzDAO clazzDAO;
    private GradeDAO gradeDAO;
//    private TuitionFeeDAO tuitionFeeDAO; // Thêm TuitionFeeDAO
    private String studentUserID;

    public DeleteClassCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
//        this.tuitionFeeDAO = new TuitionFeeDAO(); // Khởi tạo TuitionFeeDAO
        this.studentUserID = studentUserID;
    }

    public String execute(Integer clazzID) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Clazz clazz = clazzDAO.getClazzWithStudents(clazzID);
            if (clazz == null) {
                return "Lớp ID: " + clazzID + " không tồn tại!";
            }

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class)
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                return "Không tìm thấy thông tin sinh viên!";
            }

            boolean removed = clazz.getStudents().removeIf(s -> s.getUserID().equals(student.getUserID()));
            if (!removed) {
                return "Lớp ID: " + clazzID + " không nằm trong danh sách đã đăng ký!";
            }

            clazz.setRegisteredCount(clazz.getRegisteredCount() - 1);
            session.merge(clazz);

            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
            if (grade != null) {
                session.remove(grade);
                System.out.println("Đã xóa điểm cho sinh viên " + student.getUserID() + " trong lớp " + clazz.getClazzID());
            }
/*
            // Tính toán lại học phí sau khi xóa lớp
            Integer semester = clazz.getSemester();
            TuitionFee tuitionFee = tuitionFeeDAO.calculateAndSaveTuitionFee(student.getUserID(), semester);
            if (tuitionFee != null) {
                System.out.println("Đã cập nhật học phí cho sinh viên " + student.getUserID() + 
                                   " học kỳ " + semester + ": " + tuitionFee.getTuition());
            } else {
                System.out.println("Không còn lớp nào trong kỳ " + semester + ", không cần tính học phí.");
                // Xóa bản ghi học phí nếu tồn tại
                TuitionFee existingFee = tuitionFeeDAO.getTuitionFeeByStudentAndSemester(student.getUserID(), semester);
                if (existingFee != null) {
                    session.remove(existingFee);
                    System.out.println("Đã xóa bản ghi học phí cho sinh viên " + student.getUserID() + " học kỳ " + semester);
                }
            }
*/
            transaction.commit();
            return "Xóa lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Lỗi khi xóa lớp: " + e.getMessage();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;

public class DeleteClassCommand {
    private ClazzDAO clazzDAO;
    private GradeDAO gradeDAO;
    private String studentAccountName;

    public DeleteClassCommand(String studentAccountName) {
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.studentAccountName = studentAccountName;
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
                "FROM Student WHERE accountName = :account", Student.class)
                .setParameter("account", studentAccountName)
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

            // Xóa bản ghi điểm
            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
            if (grade != null) {
                session.remove(grade);
                System.out.println("Đã xóa điểm cho sinh viên " + student.getUserID() + " trong lớp " + clazz.getClazzID());
            }

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
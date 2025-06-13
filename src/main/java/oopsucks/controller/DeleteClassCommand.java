package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;

public class DeleteClassCommand extends BaseCommand<String> {
    private ClazzDAO clazzDAO;
    private GradeDAO gradeDAO;
    private String studentUserID;
    private Integer clazzID;

    public DeleteClassCommand(String studentUserID, Integer clazzID) {
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.studentUserID = studentUserID;
        this.clazzID = clazzID;
    }

    @Override
    protected String doExecute() throws CommandException {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Clazz clazz = clazzDAO.getClazzWithStudents(clazzID);
            if (clazz == null) {
                throw new CommandException("Lớp ID: " + clazzID + " không tồn tại!");
            }

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class)
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                throw new CommandException("Không tìm thấy thông tin sinh viên!");
            }

            boolean removed = clazz.getStudents().removeIf(s -> s.getUserID().equals(student.getUserID()));
            if (!removed) {
                throw new CommandException("Lớp ID: " + clazzID + " không nằm trong danh sách đã đăng ký!");
            }

            clazz.setRegisteredCount(clazz.getRegisteredCount() - 1);
            session.merge(clazz);

            Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
            if (grade != null) {
                session.remove(grade);
                System.out.println("Đã xóa điểm cho sinh viên " + student.getUserID() + " trong lớp " + clazz.getClazzID());
            }

            transaction.commit();
            return "Xóa lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new CommandException("Lỗi khi xóa lớp: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean validate() {
        return studentUserID != null && !studentUserID.trim().isEmpty() && clazzID != null && clazzID > 0;
    }
}
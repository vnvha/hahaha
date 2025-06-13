package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;
import java.util.List;

public class FinishRegistrationCommand extends BaseCommand<String> {
    private ClazzDAO clazzDAO;
    private GradeDAO gradeDAO;
    private String studentUserID;

    public FinishRegistrationCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.studentUserID = studentUserID;
    }

    @Override
    protected String doExecute() throws CommandException {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class)
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                throw new CommandException("Không tìm thấy thông tin sinh viên với userID: " + studentUserID);
            }

            List<Clazz> registeredClasses = clazzDAO.getClazzesByStudent(student);
            if (registeredClasses.isEmpty()) {
                throw new CommandException("Bạn chưa đăng ký lớp nào!");
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

            transaction.commit();
            return "Đăng ký học phần thành công!";
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new CommandException("Lỗi khi hoàn thành đăng ký: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean validate() {
        return studentUserID != null && !studentUserID.trim().isEmpty();
    }
}
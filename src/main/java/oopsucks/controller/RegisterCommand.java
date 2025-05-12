package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;
import oopsucks.model.Student;
import oopsucks.model.UserDAO;
import oopsucks.model.HibernateUtil;
import oopsucks.model.GradeInitializer;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private List<Clazz> registeredClasses;
    private String studentAccountName;

    public RegisterCommand(String studentAccountName) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.registeredClasses = new ArrayList<>();
        this.studentAccountName = studentAccountName;
    }

    public RegisterCommand() {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.registeredClasses = new ArrayList<>();
    }

    public void setStudentAccountName(String accountName) {
        this.studentAccountName = accountName;
    }

    public String execute(Integer clazzID) {
        try {
            Clazz clazz = clazzDAO.getClazzById(clazzID);
            if (clazz == null) {
                return "Class ID " + clazzID + " not found!";
            }

            if (registeredClasses.stream().anyMatch(c -> c.getClazzID() == clazzID)) {
                return "Lớp ID: " + clazzID + " đã được đăng ký trước đó!";
            }

            for (Clazz registered : registeredClasses) {
                if (registered.getCourse() != null && clazz.getCourse() != null &&
                    registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                    return "Không thể đăng ký: Khóa học " + clazz.getCourse().getCourseID() + " đã được đăng ký trước đó!";
                }
            }

            if (clazz.getRegisteredCount() >= clazz.getMaxCapacity()) {
                return "Không thể đăng ký: Lớp " + clazzID + " đã đạt số lượng sinh viên tối đa!";
            }

            registeredClasses.add(clazz);
            return "Đăng ký lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            return "Error registering class: " + e.getMessage();
        }
    }

    public String delete(Integer clazzID) {
        try {
            Clazz clazzToRemove = null;
            for (Clazz clazz : registeredClasses) {
                if (clazz.getClazzID() == clazzID) {
                    clazzToRemove = clazz;
                    break;
                }
            }

            if (clazzToRemove == null) {
                return "Lớp ID: " + clazzID + " không nằm trong danh sách đã đăng ký!";
            }

            registeredClasses.remove(clazzToRemove);
            return "Xóa lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            return "Error deleting class: " + e.getMessage();
        }
    }

    public List<Clazz> getRegisteredClasses() {
        return registeredClasses;
    }

    public String finishRegistration() {
        Transaction transaction = null;
        Session session = null;

        try {
            if (registeredClasses.isEmpty()) {
                return "Bạn chưa đăng ký lớp nào!";
            }

            if (studentAccountName == null || studentAccountName.isEmpty()) {
                return "Không tìm thấy thông tin sinh viên!";
            }

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Student student = session.createQuery(
                "FROM Student WHERE accountName = :account", Student.class)
                .setParameter("account", studentAccountName)
                .uniqueResult();

            if (student == null) {
                return "Không tìm thấy thông tin sinh viên với tài khoản: " + studentAccountName;
            }

            GradeInitializer gradeInitializer = new GradeInitializer();
            for (Clazz tempClazz : registeredClasses) {
                Clazz clazz = session.get(Clazz.class, tempClazz.getClazzID());

                List<Student> students = clazz.getStudents();
                boolean studentExists = false;
                for (Student s : students) {
                    if (s.getUserID().equals(student.getUserID())) {
                        studentExists = true;
                        break;
                    }
                }

                if (!studentExists) {
                    students.add(student);
                    clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
                    session.merge(clazz);
                    // Tạo bản ghi Grade cho sinh viên và lớp học
                    gradeInitializer.initializeGrade(student, clazz);
                    System.out.println("Đã thêm sinh viên " + student.getUserID() + " vào lớp " + clazz.getClazzID() + " và tạo Grade");
                } else {
                    System.out.println("Sinh viên " + student.getUserID() + " đã có trong lớp " + clazz.getClazzID());
                }
            }

            transaction.commit();
            registeredClasses.clear();
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
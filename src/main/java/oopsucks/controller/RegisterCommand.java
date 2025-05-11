package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;
import oopsucks.model.Student;
import oopsucks.model.UserDAO;
import oopsucks.model.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private List<Clazz> registeredClasses; // Lưu danh sách lớp đã đăng ký (giả lập)
    private String studentAccountName; // Thêm biến lưu tên tài khoản của sinh viên

    public RegisterCommand(String studentAccountName) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.registeredClasses = new ArrayList<>();
        this.studentAccountName = studentAccountName;
    }

    // Constructor không tham số cho khả năng tương thích ngược
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
            // Lấy thông tin lớp từ database
            Clazz clazz = clazzDAO.getClazzById(clazzID);
            if (clazz == null) {
                return "Class ID " + clazzID + " not found!";
            }

            // Kiểm tra trùng clazzID
            if (registeredClasses.stream().anyMatch(c -> c.getClazzID() == clazzID)) {
                return "Lớp ID: " + clazzID + " đã được đăng ký trước đó!";
            }

            // Kiểm tra trùng courseID (bất kể time)
            for (Clazz registered : registeredClasses) {
                if (registered.getCourse() != null && clazz.getCourse() != null &&
                    registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                    return "Không thể đăng ký: Khóa học " + clazz.getCourse().getCourseID() + " đã được đăng ký trước đó!";
                }
            }

            // Kiểm tra sĩ số lớp
            if (clazz.getRegisteredCount() >= clazz.getMaxCapacity()) {
                return "Không thể đăng ký: Lớp " + clazzID + " đã đạt số lượng sinh viên tối đa!";
            }

            // Đăng ký thành công
            registeredClasses.add(clazz);
            return "Đăng ký lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            return "Error registering class: " + e.getMessage();
        }
    }

    public String delete(Integer clazzID) {
        try {
            // Tìm lớp trong danh sách đã đăng ký
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
    
    // Phương thức finishRegistration đã cập nhật để xử lý lazy loading
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
            
            // Tạo session mới
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Lấy thông tin sinh viên từ database trong session hiện tại
            Student student = session.createQuery(
                "FROM Student WHERE accountName = :account", Student.class)
                .setParameter("account", studentAccountName)
                .uniqueResult();
                
            if (student == null) {
                return "Không tìm thấy thông tin sinh viên với tài khoản: " + studentAccountName;
            }
            
            // Thêm sinh viên vào tất cả các lớp đã đăng ký và cập nhật database
            for (Clazz tempClazz : registeredClasses) {
                // Lấy lại đối tượng Clazz trong session hiện tại
                Clazz clazz = session.get(Clazz.class, tempClazz.getClazzID());
                
                // Lấy danh sách sinh viên trong cùng session và kiểm tra xem sinh viên đã tồn tại chưa
                List<Student> students = clazz.getStudents(); // An khi lazy loading trong cùng session
                
                boolean studentExists = false;
                for (Student s : students) {
                    if (s.getUserID() == student.getUserID()) {
                        studentExists = true;
                        break;
                    }
                }
                
                if (!studentExists) {
                    // Thêm sinh viên vào danh sách sinh viên của lớp
                    students.add(student);
                    // Tăng số lượng đã đăng ký
                    clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
                    // Cập nhật trong session
                    session.merge(clazz);
                }
            }
            
            // Commit transaction
            transaction.commit();
            
            // Xóa danh sách đăng ký tạm thời sau khi đã lưu vào database
            registeredClasses.clear();
            
            return "Đăng ký học phần thành công!";
        } catch (Exception e) {
            // Rollback nếu có lỗi
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Lỗi khi hoàn thành đăng ký: " + e.getMessage();
        } finally {
            // Đóng session
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
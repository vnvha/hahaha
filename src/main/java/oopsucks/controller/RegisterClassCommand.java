package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;
import java.util.List;

public class RegisterClassCommand {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private GradeDAO gradeDAO;
    private CourseDAO courseDAO;
    private TuitionFeeDAO tuitionFeeDAO; // Thêm TuitionFeeDAO
    private String studentUserID;

    public RegisterClassCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.gradeDAO = new GradeDAO();
        this.courseDAO = new CourseDAO();
        this.tuitionFeeDAO = new TuitionFeeDAO(); // Khởi tạo TuitionFeeDAO
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
                return "Lớp ID " + clazzID + " không tồn tại!";
            }

            Integer selectedSemester = RegistrationManager.getSelectedSemester();
            if (selectedSemester == null || clazz.getSemester() != selectedSemester) {
                return "Không thể đăng ký: Lớp ID " + clazzID + " không thuộc kỳ đang đăng ký!";
            }

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class)
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                return "Không tìm thấy thông tin sinh viên!";
            }

            boolean studentExists = clazz.getStudents().stream()
                    .anyMatch(s -> s.getUserID().equals(student.getUserID()));
            if (studentExists) {
                return "Lớp ID: " + clazzID + " đã được đăng ký trước đó!";
            }

            // Kiểm tra trùng lặp trong cùng một kỳ học
            List<Clazz> registeredClasses = clazzDAO.getClazzesByStudent(student);
            for (Clazz registered : registeredClasses) {
                if (registered.getSemester() == clazz.getSemester()) {
                    if (registered.getCourse() != null && clazz.getCourse() != null &&
                        registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                        return "Không thể đăng ký: Khóa học " + clazz.getCourse().getCourseID() + 
                               " đã được đăng ký trước đó trong cùng kỳ học!";
                    }
                    if (registered.getDayOfWeek().equals(clazz.getDayOfWeek())) {
                        int newStartTimeMinutes = convertToMinutes(clazz.getStartTime());
                        int newEndTimeMinutes = convertToMinutes(clazz.getEndTime());
                        int regStartTimeMinutes = convertToMinutes(registered.getStartTime());
                        int regEndTimeMinutes = convertToMinutes(registered.getEndTime());
                        if (!(newEndTimeMinutes <= regStartTimeMinutes || newStartTimeMinutes >= regEndTimeMinutes)) {
                            return "Không thể đăng ký: Lớp ID " + clazzID + 
                                   " trùng thời gian với lớp ID " + registered.getClazzID();
                        }
                    }
                }
            }

            // Kiểm tra môn học điều kiện
            if (clazz.getCourse() != null) {
                String prerequisiteCheckResult = checkPrerequisites(student, clazz.getCourse());
                if (prerequisiteCheckResult != null) {
                    return prerequisiteCheckResult;
                }
            }

            // Kiểm tra sĩ số lớp học
            if (clazz.getRegisteredCount() >= clazz.getMaxCapacity()) {
                return "Không thể đăng ký: Lớp " + clazzID + " đã đạt số lượng sinh viên tối đa!";
            }

            // Thực hiện đăng ký
            clazz.getStudents().add(student);
            clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
            session.merge(clazz);

            // Khởi tạo hoặc cập nhật điểm
            GradeInitializer gradeInitializer = new GradeInitializer();
            gradeInitializer.initializeGrade(student, clazz);
            System.out.println("Processed Grade for student " + student.getUserID() + " in clazz " + clazz.getClazzID());

            // Tính toán lại học phí cho kỳ học
            TuitionFee tuitionFee = tuitionFeeDAO.calculateAndSaveTuitionFee(student.getUserID(), clazz.getSemester());
            if (tuitionFee != null) {
                System.out.println("Đã tính học phí cho sinh viên " + student.getUserID() + 
                                   " học kỳ " + clazz.getSemester() + ": " + tuitionFee.getTuition());
            } else {
                System.out.println("Không thể tính học phí cho sinh viên " + student.getUserID() + 
                                   " học kỳ " + clazz.getSemester());
            }

            transaction.commit();
            return "Đăng ký lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Lỗi khi đăng ký lớp: " + e.getMessage();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    private String checkPrerequisites(Student student, Course course) {
        List<Course> prerequisites = courseDAO.getPrerequisites(course);
        System.out.println("Prerequisites for course " + course.getCourseID() + ": " + prerequisites);

        if (prerequisites == null || prerequisites.isEmpty()) {
            System.out.println("No prerequisites found.");
            return null;
        }

        StringBuilder missingGrades = new StringBuilder();

        for (Course prereq : prerequisites) {
            boolean hasGrade = hasAnyGrade(student, prereq);

            if (!hasGrade) {
                if (missingGrades.length() > 0) {
                    missingGrades.append(", ");
                }
                missingGrades.append(prereq.getCourseID());
            }
        }

        if (missingGrades.length() > 0) {
            return "Không thể đăng ký: Bạn chưa học các học phần " + missingGrades;
        }

        return null; // Có thể đăng ký
    }


    private boolean hasAnyGrade(Student student, Course prerequisiteCourse) {
        List<Clazz> allStudentClasses = clazzDAO.getClazzesByStudent(student);

        for (Clazz clazz : allStudentClasses) {
            if (clazz.getCourse() != null && 
                clazz.getCourse().getCourseID().equals(prerequisiteCourse.getCourseID())) {
                
                Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
                if (grade != null && grade.getTotalScore() != null) {
                    return true; // Có điểm -> đủ điều kiện
                }
            }
        }
        return false; // Không tìm thấy điểm nào
    }


    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
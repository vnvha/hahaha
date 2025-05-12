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
    private String studentAccountName;

    public RegisterClassCommand(String studentAccountName) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.gradeDAO = new GradeDAO();
        this.courseDAO = new CourseDAO();
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
                return "Lớp ID " + clazzID + " không tồn tại!";
            }

            Student student = session.createQuery(
                "FROM Student WHERE accountName = :account", Student.class)
                .setParameter("account", studentAccountName)
                .uniqueResult();
            if (student == null) {
                return "Không tìm thấy thông tin sinh viên!";
            }

            boolean studentExists = clazz.getStudents().stream()
                .anyMatch(s -> s.getUserID().equals(student.getUserID()));
            if (studentExists) {
                return "Lớp ID: " + clazzID + " đã được đăng ký trước đó!";
            }

            // Kiểm tra điều kiện tiên quyết
            if (clazz.getCourse() != null) {
                String prerequisiteCheckResult = checkPrerequisites(student, clazz.getCourse());
                if (prerequisiteCheckResult != null) {
                    return prerequisiteCheckResult;
                }
            }

            // Kiểm tra xung đột thời gian và lớp đã đăng ký
            List<Clazz> registeredClasses = clazzDAO.getClazzesByStudent(student);
            for (Clazz registered : registeredClasses) {
                if (registered.getCourse() != null && clazz.getCourse() != null &&
                    registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                    return "Không thể đăng ký: Khóa học " + clazz.getCourse().getCourseID() + " đã được đăng ký trước đó!";
                }
                if (registered.getDayOfWeek().equals(clazz.getDayOfWeek())) {
                    int newStartTimeMinutes = convertToMinutes(clazz.getStartTime());
                    int newEndTimeMinutes = convertToMinutes(clazz.getEndTime());
                    int regStartTimeMinutes = convertToMinutes(registered.getStartTime());
                    int regEndTimeMinutes = convertToMinutes(registered.getEndTime());
                    if (!(newEndTimeMinutes <= regStartTimeMinutes || newStartTimeMinutes >= regEndTimeMinutes)) {
                        return "Không thể đăng ký: Lớp ID " + clazzID + " trùng thời gian với lớp ID " + registered.getClazzID() + "!";
                    }
                }
            }

            if (clazz.getRegisteredCount() >= clazz.getMaxCapacity()) {
                return "Không thể đăng ký: Lớp " + clazzID + " đã đạt số lượng sinh viên tối đa!";
            }

            clazz.getStudents().add(student);
            clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
            session.merge(clazz);

            // Khởi tạo hoặc cập nhật điểm
            GradeInitializer gradeInitializer = new GradeInitializer();
            gradeInitializer.initializeGrade(student, clazz);
            System.out.println("Processed Grade for student " + student.getUserID() + " in clazz " + clazz.getClazzID());

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

        StringBuilder failedPrereqs = new StringBuilder();
        StringBuilder pendingPrereqs = new StringBuilder();

        for (Course prereq : prerequisites) {
            Grade bestGrade = findBestGradeForCourse(student, prereq);
            if (bestGrade == null) {
                if (failedPrereqs.length() > 0) {
                    failedPrereqs.append(", ");
                }
                failedPrereqs.append(prereq.getCourseID());
            } else if (bestGrade.getTotalScore() == null) {
                if (pendingPrereqs.length() > 0) {
                    pendingPrereqs.append(", ");
                }
                pendingPrereqs.append(prereq.getCourseID());
            } else if (bestGrade.getTotalScore() < 4.0f) {
                if (failedPrereqs.length() > 0) {
                    failedPrereqs.append(", ");
                }
                failedPrereqs.append(prereq.getCourseID());
            }
        }

        StringBuilder errorMessage = new StringBuilder();
        if (failedPrereqs.length() > 0) {
            errorMessage.append("Bạn chưa hoàn thành học phần: ").append(failedPrereqs);
        }
        if (pendingPrereqs.length() > 0) {
            if (errorMessage.length() > 0) {
                errorMessage.append("; ");
            }
            errorMessage.append("Các học phần đang chờ điểm: ").append(pendingPrereqs);
        }

        return errorMessage.length() > 0 ? "Không thể đăng ký: " + errorMessage.toString() : null;
    }

    private Grade findBestGradeForCourse(Student student, Course prerequisiteCourse) {
        List<Clazz> allStudentClasses = clazzDAO.getClazzesByStudent(student);
        Grade bestGrade = null;

        for (Clazz clazz : allStudentClasses) {
            if (clazz.getCourse() != null &&
                clazz.getCourse().getCourseID().equals(prerequisiteCourse.getCourseID())) {
                Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
                if (grade != null && grade.getTotalScore() != null) {
                    if (bestGrade == null ||
                        bestGrade.getTotalScore() == null ||
                        grade.getTotalScore() > bestGrade.getTotalScore()) {
                        bestGrade = grade;
                    }
                }
            }
        }
        return bestGrade;
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
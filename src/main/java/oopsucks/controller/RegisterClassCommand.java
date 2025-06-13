package oopsucks.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import oopsucks.model.*;
import java.util.List;

public class RegisterClassCommand extends BaseCommand<String> {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private GradeDAO gradeDAO;
    private CourseDAO courseDAO;
    private TuitionFeeDAO tuitionFeeDAO;
    private String studentUserID;
    private Integer clazzID;

    public RegisterClassCommand(String studentUserID, Integer clazzID) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.gradeDAO = new GradeDAO();
        this.courseDAO = new CourseDAO();
        this.tuitionFeeDAO = new TuitionFeeDAO();
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
                throw new CommandException("Lớp ID " + clazzID + " không tồn tại!");
            }

            Integer selectedSemester = RegistrationManager.getSelectedSemester();
            if (selectedSemester == null || clazz.getSemester() != selectedSemester) {
                throw new CommandException("Lớp ID " + clazzID + " không thuộc kỳ đang đăng ký!");
            }

            Student student = session.createQuery(
                "FROM Student WHERE userID = :userID", Student.class)
                .setParameter("userID", studentUserID)
                .uniqueResult();
            if (student == null) {
                throw new CommandException("Không tìm thấy thông tin sinh viên!");
            }

            boolean studentExists = clazz.getStudents().stream()
                    .anyMatch(s -> s.getUserID().equals(student.getUserID()));
            if (studentExists) {
                throw new CommandException("Lớp ID: " + clazzID + " đã được đăng ký trước đó!");
            }

            List<Clazz> registeredClasses = clazzDAO.getClazzesByStudent(student);
            for (Clazz registered : registeredClasses) {
                if (registered.getSemester() == clazz.getSemester()) {
                    if (registered.getCourse() != null && clazz.getCourse() != null &&
                        registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                        throw new CommandException("Khóa học " + clazz.getCourse().getCourseID() + 
                                                  " đã được đăng ký trước đó trong cùng kỳ học!");
                    }
                    if (registered.getDayOfWeek().equals(clazz.getDayOfWeek())) {
                        int newStartTimeMinutes = convertToMinutes(clazz.getStartTime());
                        int newEndTimeMinutes = convertToMinutes(clazz.getEndTime());
                        int regStartTimeMinutes = convertToMinutes(registered.getStartTime());
                        int regEndTimeMinutes = convertToMinutes(registered.getEndTime());
                        if (!(newEndTimeMinutes <= regStartTimeMinutes || newStartTimeMinutes >= regEndTimeMinutes)) {
                            throw new CommandException("Lớp ID " + clazzID + 
                                                       " trùng thời gian với lớp ID " + registered.getClazzID());
                        }
                    }
                }
            }

            if (clazz.getCourse() != null) {
                String prerequisiteCheckResult = checkPrerequisites(student, clazz.getCourse());
                if (prerequisiteCheckResult != null) {
                    throw new CommandException(prerequisiteCheckResult);
                }
            }

            if (clazz.getRegisteredCount() >= clazz.getMaxCapacity()) {
                throw new CommandException("Lớp " + clazzID + " đã đạt số lượng sinh viên tối đa!");
            }

            clazz.getStudents().add(student);
            clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
            session.merge(clazz);

            GradeInitializer gradeInitializer = new GradeInitializer();
            gradeInitializer.initializeGrade(student, clazz);
            System.out.println("Processed Grade for student " + student.getUserID() + " in clazz " + clazz.getClazzID());

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
            throw new CommandException("Lỗi khi đăng ký lớp: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    @Override
    public boolean validate() {
        return studentUserID != null && !studentUserID.trim().isEmpty() && 
               clazzID != null && clazzID > 0;
    }

    private String checkPrerequisites(Student student, Course course) {
        List<Course> prerequisites = courseDAO.getPrerequisites(course);
        if (prerequisites == null || prerequisites.isEmpty()) {
            return null;
        }

        StringBuilder missingGrades = new StringBuilder();
        for (Course prereq : prerequisites) {
            if (!hasAnyGrade(student, prereq)) {
                if (missingGrades.length() > 0) missingGrades.append(", ");
                missingGrades.append(prereq.getCourseID());
            }
        }

        return missingGrades.length() > 0 ? "Bạn chưa học các học phần " + missingGrades : null;
    }

    private boolean hasAnyGrade(Student student, Course prerequisiteCourse) {
        List<Clazz> allStudentClasses = clazzDAO.getClazzesByStudent(student);
        for (Clazz clazz : allStudentClasses) {
            if (clazz.getCourse() != null && 
                clazz.getCourse().getCourseID().equals(prerequisiteCourse.getCourseID())) {
                Grade grade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
                if (grade != null && grade.getTotalScore() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
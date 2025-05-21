package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class AnnualTuitionFeeDAO {

    /**
     * Lấy học phí theo studentId và semester
     */
    public AnnualTuitionFee getAnnualTuitionFeeByStudentAndSemester(String studentId, Integer semester) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AnnualTuitionFee WHERE studentId = :studentId AND semester = :semester";
            Query<AnnualTuitionFee> query = session.createQuery(hql, AnnualTuitionFee.class);
            query.setParameter("studentId", studentId);
            query.setParameter("semester", semester);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy danh sách học phí của sinh viên theo studentId
     */
    public List<AnnualTuitionFee> getAnnualTuitionFeesByStudent(String studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AnnualTuitionFee WHERE studentId = :studentId ORDER BY semester";
            Query<AnnualTuitionFee> query = session.createQuery(hql, AnnualTuitionFee.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lưu hoặc cập nhật thông tin học phí
     */
    public boolean saveAnnualTuitionFee(AnnualTuitionFee tuitionFee) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(tuitionFee);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật trạng thái thanh toán học phí
     */
    public boolean updatePaymentStatus(Long id, boolean status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            AnnualTuitionFee tuitionFee = session.get(AnnualTuitionFee.class, id);
            if (tuitionFee != null) {
                tuitionFee.setStatus(status);
                session.merge(tuitionFee);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tính và lưu học phí cho sinh viên niên chế
     *
     * @param studentId ID của sinh viên
     * @param semester Học kỳ cần tính học phí
     * @return Đối tượng AnnualTuitionFee hoặc null nếu có lỗi
     */
    public AnnualTuitionFee calculateAndSaveAnnualTuitionFee(String studentId, Integer semester) {
        try {
            // Lấy thông tin sinh viên
            UserDAO userDAO = new UserDAO();
            Student student = userDAO.getStudent(studentId);
            if (student == null) {
                throw new IllegalArgumentException("Không tìm thấy sinh viên với ID: " + studentId);
            }

            // Kiểm tra xem sinh viên có phải là YearBasedStudent
            if (!(student instanceof YearBasedStudent)) {
                throw new IllegalArgumentException("Sinh viên " + studentId + " không thuộc hệ niên chế");
            }

            String major = student.getMajor();
            if (major == null || major.isEmpty()) {
                throw new IllegalArgumentException("Sinh viên " + studentId + " không có thông tin ngành học");
            }

            // Lấy phí học kỳ theo ngành
            Double semesterFee = getSemesterFeeByMajor(major);
            if (semesterFee == null) {
                throw new IllegalArgumentException("Không tìm thấy phí học kỳ cho ngành: " + major);
            }

            // Kiểm tra xem đã có học phí cho kỳ này chưa
            AnnualTuitionFee existingFee = getAnnualTuitionFeeByStudentAndSemester(studentId, semester);
            if (existingFee != null) {
                // Cập nhật học phí hiện có
                existingFee.setSemesterFee(semesterFee);
                if (saveAnnualTuitionFee(existingFee)) {
                    return existingFee;
                } else {
                    System.err.println("Lỗi khi lưu học phí cho sinh viên " + studentId + " kỳ " + semester);
                    return null;
                }
            } else {
                // Tạo mới học phí
                AnnualTuitionFee tuitionFee = new AnnualTuitionFee(studentId, semester, semesterFee);
                if (saveAnnualTuitionFee(tuitionFee)) {
                    return tuitionFee;
                } else {
                    System.err.println("Lỗi khi lưu học phí cho sinh viên " + studentId + " kỳ " + semester);
                    return null;
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi khi tính học phí: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy phí học kỳ theo ngành từ bảng major_semester_fees
     */
    private Double getSemesterFeeByMajor(String major) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT semesterFee FROM MajorSemesterFees WHERE major = :major", Double.class);
            query.setParameter("major", major);
            return query.uniqueResultOptional().orElse(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi lấy phí học kỳ cho ngành " + major + ": " + e.getMessage(), e);
        }
    }
}
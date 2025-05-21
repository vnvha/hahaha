package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import oopsucks.controller.*;
import java.util.ArrayList;
import java.util.List;

public class TuitionFeeDAO {

    public TuitionFee getTuitionFeeById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TuitionFee.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TuitionFee getTuitionFeeByStudentAndSemester(String studentId, Integer semester) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TuitionFee WHERE studentId = :studentId AND semester = :semester";
            Query<TuitionFee> query = session.createQuery(hql, TuitionFee.class);
            query.setParameter("studentId", studentId);
            query.setParameter("semester", semester);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<TuitionFee> getTuitionFeesByStudent(String studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TuitionFee WHERE studentId = :studentId ORDER BY semester";
            Query<TuitionFee> query = session.createQuery(hql, TuitionFee.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean saveTuitionFee(TuitionFee tuitionFee) {
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

    public boolean updatePaymentStatus(Long id, boolean status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            TuitionFee tuitionFee = session.get(TuitionFee.class, id);
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

    public int calculateSemesterCredits(String studentId, Integer semester) {
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Không tìm thấy sinh viên với ID: " + studentId);
        }

        ClazzDAO clazzDAO = new ClazzDAO();
        List<Clazz> clazzes = clazzDAO.getClazzesByStudentAndSemester(student, semester);
        int totalChargeableCredits = 0;

        for (Clazz clazz : clazzes) {
            Course course = clazz.getCourse();
            if (course != null) {
            	totalChargeableCredits += course.getChargeableCredits();
            }
        }
        return totalChargeableCredits;
    }

    public TuitionFee calculateAndSaveTuitionFee(String studentId, Integer semester) {
        try {
            int totalChargeableCredits = calculateSemesterCredits(studentId, semester);
            if (totalChargeableCredits <= 0) {
                System.out.println("Không có tín chỉ nào được đăng ký cho sinh viên " + studentId + " trong kỳ " + semester);
                return null;
            }

            TuitionFee existingFee = getTuitionFeeByStudentAndSemester(studentId, semester);
            if (existingFee != null) {
                existingFee.setTotalChargeableCredits(totalChargeableCredits);
                existingFee.setCreditFeeByMajor(studentId);
                if (saveTuitionFee(existingFee)) {
                    return existingFee;
                } else {
                    System.err.println("Lỗi khi lưu học phí cho sinh viên " + studentId + " kỳ " + semester);
                    return null;
                }
            } else {
                TuitionFee tuitionFee = new TuitionFee(studentId, semester, totalChargeableCredits);
                if (saveTuitionFee(tuitionFee)) {
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

    public TuitionFee calculateAndSaveTuitionFee(String studentId, Integer semester, Double creditFee) {
        int totalChargeableCredits = calculateSemesterCredits(studentId, semester);

        if (totalChargeableCredits > 0) {
            TuitionFee existingFee = getTuitionFeeByStudentAndSemester(studentId, semester);

            if (existingFee != null) {
                existingFee.setCreditFee(creditFee);
                existingFee.setTotalChargeableCredits(totalChargeableCredits);
                saveTuitionFee(existingFee);
                return existingFee;
            } else {
                TuitionFee tuitionFee = new TuitionFee(studentId, semester, creditFee, totalChargeableCredits);
                saveTuitionFee(tuitionFee);
                return tuitionFee;
            }
        }

        System.out.println("Không có tín chỉ nào được đăng ký cho sinh viên " + studentId + " trong kỳ " + semester);
        return null;
    }
}
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

    public Double getCreditFeeByMajor(String major) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT creditFee FROM MajorCreditFees WHERE major = :major", Double.class);
            query.setParameter("major", major);
            return query.uniqueResultOptional().orElseThrow(() -> 
                new IllegalArgumentException("Không tìm thấy phí tín chỉ cho ngành: " + major));
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi lấy phí tín chỉ cho ngành " + major + ": " + e.getMessage(), e);
        }
    }

    public TuitionFee calculateAndSaveTuitionFee(String studentId, Integer semester) {
        try {
            CalculateTuitionFeeCommand command = new CalculateTuitionFeeCommand(studentId, semester);
            return command.execute();
        } catch (CommandException e) {
            System.err.println("Lỗi khi tính học phí: " + e.getMessage());
            return null;
        }
    }

    public TuitionFee calculateAndSaveTuitionFee(String studentId, Integer semester, Double creditFee) {
        try {
            CalculateTuitionFeeCommand command = new CalculateTuitionFeeCommand(studentId, semester, creditFee);
            return command.execute();
        } catch (CommandException e) {
            System.err.println("Lỗi khi tính học phí: " + e.getMessage());
            return null;
        }
    }

}
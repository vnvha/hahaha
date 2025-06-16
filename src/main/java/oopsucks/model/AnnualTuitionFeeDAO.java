package oopsucks.model;

import oopsucks.controller.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import oopsucks.controller.CalculateAnnualTuitionFeeCommand;

import java.util.ArrayList;
import java.util.List;

public class AnnualTuitionFeeDAO {

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

    public AnnualTuitionFee calculateAndSaveAnnualTuitionFee(String studentId, Integer semester) {
        try {
            CalculateAnnualTuitionFeeCommand command = new CalculateAnnualTuitionFeeCommand(studentId, semester);
            return command.execute();
        } catch (CommandException e) {
            System.err.println("Lỗi khi tính học phí niên chế: " + e.getMessage());
            return null;
        }
    }

    public Double getSemesterFeeByMajor(String major) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT semesterFee FROM MajorSemesterFees WHERE major = :major", Double.class);
            query.setParameter("major", major);
            return query.uniqueResultOptional().orElseThrow(() ->
                new IllegalArgumentException("Không tìm thấy phí học kỳ cho ngành: " + major));
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi lấy phí học kỳ cho ngành " + major + ": " + e.getMessage(), e);
        }
    }
}
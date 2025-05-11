package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class GradeDAO {
    public Grade getGradeByStudentAndClazz(Student student, Clazz clazz) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Grade WHERE student.id = :studentId AND clazz.clazzID = :clazzId",
                    Grade.class)
                    .setParameter("studentId", student.getUserID())
                    .setParameter("clazzId", clazz.getClazzID())
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Grade> getGradesByClazz(Clazz clazz) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Grade WHERE clazz.clazzID = :clazzId",
                    Grade.class)
                    .setParameter("clazzId", clazz.getClazzID())
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveGrade(Grade grade) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(grade);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
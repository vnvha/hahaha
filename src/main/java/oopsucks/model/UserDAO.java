package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDAO {

    public void saveTeacher(Teacher teacher) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void saveStudent(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Teacher getTeacher(String accountName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                .createQuery("FROM Teacher WHERE account_name = :account", Teacher.class)
                .setParameter("account", accountName)
                .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Student getStudent(String accountName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                .createQuery("FROM Student WHERE account_name = :account", Student.class)
                .setParameter("account", accountName)
                .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

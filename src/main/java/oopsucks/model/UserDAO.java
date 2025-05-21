package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public void saveTeacher(Teacher teacher) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(teacher);
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
            session.merge(student); 
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void updateStudent(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(student); 
            transaction.commit();
            LOGGER.info("Successfully updated student: userID=" + student.getUserID() + ", type=" + student.getClass().getSimpleName() + ", totalCredits=" + (student instanceof CreditBasedStudent ? ((CreditBasedStudent) student).getTotalCredits() : "N/A"));
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            LOGGER.severe("Failed to update student: userID=" + student.getUserID() + ", type=" + student.getClass().getSimpleName() + ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }

    public Teacher getTeacher(String userID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                .createQuery("FROM Teacher WHERE userID = :userID", Teacher.class)
                .setParameter("userID", userID)
                .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Student getStudent(String userID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CreditBasedStudent creditStudent = session
                .createQuery("FROM CreditBasedStudent WHERE userID = :userID", CreditBasedStudent.class)
                .setParameter("userID", userID)
                .uniqueResult();
            
            if (creditStudent != null) {
                return creditStudent;
            }
            
            YearBasedStudent yearStudent = session
                .createQuery("FROM YearBasedStudent WHERE userID = :userID", YearBasedStudent.class)
                .setParameter("userID", userID)
                .uniqueResult();
            
            return yearStudent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean updatePasswordByEmail(String email, String newPassword) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            CreditBasedStudent creditStudent = session
                .createQuery("FROM CreditBasedStudent WHERE email = :email", CreditBasedStudent.class)
                .setParameter("email", email)
                .uniqueResult();
            
            if (creditStudent != null) {
                creditStudent.setPassword(newPassword);
                session.merge(creditStudent);
                transaction.commit();
                return true;
            }
            
            YearBasedStudent yearStudent = session
                .createQuery("FROM YearBasedStudent WHERE email = :email", YearBasedStudent.class)
                .setParameter("email", email)
                .uniqueResult();
            
            if (yearStudent != null) {
                yearStudent.setPassword(newPassword);
                session.merge(yearStudent);
                transaction.commit();
                return true;
            }

            Teacher teacher = session
                .createQuery("FROM Teacher WHERE email = :email", Teacher.class)
                .setParameter("email", email)
                .uniqueResult();
            
            if (teacher != null) {
                teacher.setPassword(newPassword);
                session.merge(teacher);
                transaction.commit();
                return true;
            }

            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
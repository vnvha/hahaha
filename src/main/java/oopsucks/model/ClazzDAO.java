package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

public class ClazzDAO {
    
    public List<Clazz> getAllClazzes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Clazz", Clazz.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Clazz getClazzById(Integer clazzId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Clazz.class, clazzId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Phương thức mới để lấy Clazz với danh sách students đã được tải
    public Clazz getClazzWithStudents(Integer clazzId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Sử dụng JOIN FETCH để tải students cùng lúc với clazz
            return session.createQuery(
                    "SELECT c FROM Clazz c LEFT JOIN FETCH c.students WHERE c.clazzID = :id",
                    Clazz.class)
                .setParameter("id", clazzId)
                .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void saveClazz(Clazz clazz) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(clazz); // Sử dụng merge thay vì saveOrUpdate
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    // Phương thức mới để thêm sinh viên vào lớp học và cập nhật số lượng đăng ký
    public boolean addStudentToClazz(Integer clazzId, Student student) {
        Transaction transaction = null;
        Session session = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Lấy thông tin Clazz với students đã được tải
            Clazz clazz = session.createQuery(
                "SELECT c FROM Clazz c LEFT JOIN FETCH c.students WHERE c.clazzID = :id", 
                Clazz.class)
            .setParameter("id", clazzId)
            .uniqueResult();
            
            if (clazz == null) {
                return false;
            }
            
            // Kiểm tra sinh viên đã tồn tại trong danh sách chưa
            boolean studentExists = false;
            for (Student s : clazz.getStudents()) {
                if (s.getUserID() == student.getUserID()) {
                    studentExists = true;
                    break;
                }
            }
            
            if (!studentExists) {
                // Lấy student từ session hiện tại
                Student managedStudent = session.get(Student.class, student.getUserID());
                // Thêm sinh viên vào danh sách
                clazz.getStudents().add(managedStudent);
                // Tăng số lượng đăng ký
                clazz.setRegisteredCount(clazz.getRegisteredCount() + 1);
                // Cập nhật vào database
                session.merge(clazz);
            }
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    public List<Clazz> getClazzesByStudent(Student student) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Clazz c JOIN c.students s LEFT JOIN FETCH c.teacher WHERE s.id = :studentId",
                    Clazz.class)
                    .setParameter("studentId", student.getUserID())
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Clazz> getClazzesByTeacher(Teacher teacher) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Clazz WHERE teacher.id = :teacherId", 
                    Clazz.class)
                .setParameter("teacherId", teacher.getUserID())
                .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    
    public List<Clazz> getClazzesByStudentAndSemester(Student student, Integer semester) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Clazz c JOIN c.students s LEFT JOIN FETCH c.course WHERE s.id = :studentId AND c.semester = :semester",
                    Clazz.class)
                .setParameter("studentId", student.getUserID())
                .setParameter("semester", semester)
                .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Integer> getRegisteredSemestersByStudent(String studentID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT c.semester FROM Clazz c JOIN c.students s WHERE s.id = :studentId ORDER BY c.semester",
                Integer.class)
                .setParameter("studentId", studentID)
                .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    
}
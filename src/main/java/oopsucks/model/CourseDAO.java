package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Course> query = session.createQuery("FROM Course", Course.class);
            return query.list();
        }
    }

    public Course getCourseById(String courseID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Sử dụng JOIN FETCH để tải prereqCourses cùng lúc
            String jpql = "FROM Course c LEFT JOIN FETCH c.prereqCourses WHERE c.courseID = :courseID";
            Query<Course> query = session.createQuery(jpql, Course.class);
            query.setParameter("courseID", courseID);
            return query.uniqueResult();
        }
    }
    public List<Course> getPrerequisites(Course course) {
        if (course == null || course.getCourseID() == null) {
            return new ArrayList<>();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Lấy Course với prereqCourses đã được tải
            String hql = "FROM Course c LEFT JOIN FETCH c.prereqCourses WHERE c.courseID = :courseId";
            Query<Course> query = session.createQuery(hql, Course.class);
            query.setParameter("courseId", course.getCourseID());
            Course result = query.uniqueResult();
            return result != null ? result.getPrereqCourses() : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
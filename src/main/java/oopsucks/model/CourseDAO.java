package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.query.Query;

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
}
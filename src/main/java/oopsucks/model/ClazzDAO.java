package oopsucks.model;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ClazzDAO {

    public List<Clazz> getAllClazzes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Clazz> query = session.createQuery("FROM Clazz", Clazz.class);
            return query.list();
        }
    }

    public Clazz getClazzById(Integer clazzID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String jpql = "FROM Clazz c LEFT JOIN FETCH c.course LEFT JOIN FETCH c.teacher WHERE c.clazzID = :clazzID";
            Query<Clazz> query = session.createQuery(jpql, Clazz.class);
            query.setParameter("clazzID", clazzID);
            return query.uniqueResult();
        }
    }
}
package oopsucks.controller;

import oopsucks.model.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Command class to calculate the total credits for a student in a specific semester
 */
public class CalculateSemesterCreditsCommand {
    
    /**
     * Calculates the total credits for a student in a specific semester
     *
     * @param studentId ID of the student
     * @param semester Semester number
     * @return Total number of credits for the semester
     */
    public int calculateSemesterCredits(String studentId, int semester) {
        int totalCredits = 0;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get all classes the student is registered for in the specified semester
            String hql = "SELECT c FROM Clazz c " +
                         "JOIN c.students s " +
                         "JOIN FETCH c.course " +
                         "WHERE s.userID = :studentId " +
                         "AND c.semester = :semester";
                         
            Query<Clazz> query = session.createQuery(hql, Clazz.class);
            query.setParameter("studentId", studentId);
            query.setParameter("semester", semester);
            
            List<Clazz> clazzes = query.list();
            
            // Sum up the credits from all courses
            for (Clazz clazz : clazzes) {
                Course course = clazz.getCourse();
                if (course != null) {
                    totalCredits += course.getCreditNumber();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error calculating semester credits: " + e.getMessage());
        }
        
        
        return totalCredits;
    }
    
    /**
     * Gets a list of semesters that the student is enrolled in
     * 
     * @param studentId ID of the student
     * @return List of semester numbers
     */
    public List<Integer> getStudentSemesters(String studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT c.semester FROM Clazz c " +
                         "JOIN c.students s " +
                         "WHERE s.userID = :studentId " +
                         "ORDER BY c.semester";
                         
            Query<Integer> query = session.createQuery(hql, Integer.class);
            query.setParameter("studentId", studentId);
            
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
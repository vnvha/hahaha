package oopsucks.model;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Entity
@Table(name = "registration_manager")
public class RegistrationManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "register", nullable = false)
    private boolean register;

    @Column(name = "semester")
    private Integer semester;

    public RegistrationManager() {
        this.register = false; 
        this.semester = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    // Lấy trạng thái register từ cơ sở dữ liệu
    public static boolean getRegisterStatus() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RegistrationManager canRegister = session.get(RegistrationManager.class, 1L); // Giả sử id = 1
            return canRegister != null && canRegister.isRegister();
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }

    // Lấy kỳ học đã chọn từ cơ sở dữ liệu
    public static Integer getSelectedSemester() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RegistrationManager canRegister = session.get(RegistrationManager.class, 1L);
            return canRegister != null ? canRegister.getSemester() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Cập nhật trạng thái register và kỳ học trong cơ sở dữ liệu
    public static void updateRegisterStatus(boolean register, Integer semester) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            RegistrationManager canRegister = session.get(RegistrationManager.class, 1L); // Giả sử id = 1
            if (canRegister == null) {
                canRegister = new RegistrationManager();
                canRegister.setId(1L);
            }
            canRegister.setRegister(register);
            canRegister.setSemester(semester);
            session.merge(canRegister);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
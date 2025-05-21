package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher")
public class Teacher extends User {
    public Teacher() {
    }

    public Teacher(String userName, String userID, String email, String dob, String password, Role role) {
        super(userName, userID, email, dob, password, role);
    }
}
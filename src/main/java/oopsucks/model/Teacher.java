package oopsucks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher")
public class Teacher extends User {
    public Teacher() {
    }

    public Teacher(String userName, String userID, String email, String dob, String accountName, String password, Role role) {
        super(userName, userID, email, dob, accountName, password, role);
    }
}
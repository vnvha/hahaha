package oopsucks.controller;
import oopsucks.model.*;

public class LoginCommand implements Command {
    private UserDAO userDAO;
    private String username;
    private String password;
    private Role role;

    public LoginCommand(String username, String password, Role role) {
        this.userDAO = new UserDAO();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public boolean execute() {
        if (role == null) {
            return false;
        }

        User user = null;
        if (role == Role.TEACHER) {
            user = userDAO.getTeacher(username);
        } else if (role == Role.CREDITBASEDSTUDENT || role == Role.YEARBASEDSTUDENT) {
            user = userDAO.getStudent(username);
            if (user != null && user.getRole() != role) {
                user = null; 
            }
        }

        return user != null && user.getPassword().equals(password);
    }
}
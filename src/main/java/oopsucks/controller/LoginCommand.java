package oopsucks.controller;

import oopsucks.model.*;
public class LoginCommand implements Command {
    private UserDAO userDAO;
    private String account_name;
    private String password;
    private Role role;

    public LoginCommand(String account_name, String password, Role role) {
        this.userDAO = new UserDAO();
        this.account_name = account_name;
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
            user = userDAO.getTeacher(account_name);
        } else if (role == Role.CREDITBASEDSTUDENT || role == Role.YEARBASEDSTUDENT) {
            user = userDAO.getStudent(account_name);
            if (user != null && user.getRole() != role) {
                user = null; // Đảm bảo vai trò khớp
            }
        }

        return user != null && user.getPassword().equals(password);
    }
    
    
}
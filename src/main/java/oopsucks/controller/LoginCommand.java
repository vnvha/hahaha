package oopsucks.controller;
import oopsucks.model.*;

public class LoginCommand implements Command {
    private UserDAO userDAO;
    private String userID; 
    private String password;
    private Role role;

    public LoginCommand(String userID, String password, Role role) {
        this.userDAO = new UserDAO();
        this.userID = userID; 
        this.password = password;
        this.role = role;
    }

    @Override
    public boolean execute() {
        if (role == null) {
            System.out.println("Vai trò không được chọn");
            return false;
        }

        User user = null;
        if (role == Role.TEACHER) {
            user = userDAO.getTeacher(userID);
        } else if (role == Role.CREDITBASEDSTUDENT || role == Role.YEARBASEDSTUDENT) {
            user = userDAO.getStudent(userID);
            if (user != null && !user.getRole().equals(role)) {
                System.out.println("Vai trò không khớp");
                return false;
            }
        }

        boolean result = user != null && user.getPassword().equals(password);
        if (!result && user != null) {
            System.out.println("Mật khẩu không khớp");
        }
        
        return result;
    }
}

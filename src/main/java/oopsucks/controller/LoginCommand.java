package oopsucks.controller;
import oopsucks.model.*;

public class LoginCommand extends BaseCommand<Boolean> {
    private final UserDAO userDAO;
    private final String userID;
    private final String password;
    private final Role role;

    public LoginCommand(String userID, String password, Role role) {
        this.userDAO = new UserDAO();
        this.userID = userID;
        this.password = password;
        this.role = role;
    }

    @Override
    public boolean validate() {
        return userID != null && !userID.trim().isEmpty() 
            && password != null && !password.trim().isEmpty() 
            && role != null;
    }

    @Override
    protected Boolean doExecute() throws CommandException {
        if (!validate()) {
            throw new CommandException("Dữ liệu đầu vào không hợp lệ");
        }

        User user = null;
        if (role == Role.TEACHER) {
            user = userDAO.getTeacher(userID);
        } else if (role == Role.CREDITBASEDSTUDENT || role == Role.YEARBASEDSTUDENT) {
            user = userDAO.getStudent(userID);
            if (user != null && !user.getRole().equals(role)) {
                throw new CommandException("Vai trò không khớp");
            }
        } else {
            throw new CommandException("Vai trò không được hỗ trợ: " + role);
        }

        if (user == null) {
            throw new CommandException("Không tìm thấy người dùng: " + userID);
        }

        if (!user.getPassword().equals(password)) {
            throw new CommandException("Mật khẩu không khớp");
        }

        return true;
    }
}
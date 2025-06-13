package oopsucks.controller;

import oopsucks.model.*;
import java.util.ArrayList;
import java.util.List;

public class GetRegisteredClassesCommand extends BaseCommand<List<Clazz>> {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private String studentUserID;

    public GetRegisteredClassesCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.studentUserID = studentUserID;
    }

    @Override
    protected List<Clazz> doExecute() throws CommandException {
        try {
            Student student = userDAO.getStudent(studentUserID);
            if (student == null) {
                throw new CommandException("Không tìm thấy thông tin sinh viên với userID: " + studentUserID);
            }
            return clazzDAO.getClazzesByStudent(student);
        } catch (Exception e) {
            throw new CommandException("Lỗi khi lấy danh sách lớp đã đăng ký: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validate() {
        return studentUserID != null && !studentUserID.trim().isEmpty();
    }
}
package oopsucks.controller;

import oopsucks.model.*;
import java.util.ArrayList;
import java.util.List;

public class GetRegisteredClassesCommand {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private String studentUserID;

    public GetRegisteredClassesCommand(String studentUserID) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.studentUserID = studentUserID;
    }

    public List<Clazz> execute() {
        try {
            Student student = userDAO.getStudent(studentUserID); // Sử dụng phương thức mới
            if (student == null) {
                return new ArrayList<>();
            }
            return clazzDAO.getClazzesByStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
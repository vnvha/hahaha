package oopsucks.controller;

import oopsucks.model.*;
import java.util.ArrayList;
import java.util.List;

public class GetRegisteredClassesCommand {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private String studentAccountName;

    public GetRegisteredClassesCommand(String studentAccountName) {
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        this.studentAccountName = studentAccountName;
    }

    public List<Clazz> execute() {
        try {
            Student student = userDAO.getStudent(studentAccountName);
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
package oopsucks.controller;

import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand {
    private ClazzDAO clazzDAO;
    private List<Clazz> registeredClasses; // Lưu danh sách lớp đã đăng ký (giả lập)

    public RegisterCommand() {
        this.clazzDAO = new ClazzDAO();
        this.registeredClasses = new ArrayList<>();
    }

    public String execute(Integer clazzID) {
        try {
            // Lấy thông tin lớp từ database
            Clazz clazz = clazzDAO.getClazzById(clazzID);
            if (clazz == null) {
                return "Class ID " + clazzID + " not found!";
            }

            // Kiểm tra trùng clazzID
            if (registeredClasses.stream().anyMatch(c -> c.getClazzID() == clazzID)) {
                return "Lớp ID: " + clazzID + " đã được đăng ký trước đó!";
            }

            // Kiểm tra trùng courseID (bất kể time)
            for (Clazz registered : registeredClasses) {
                if (registered.getCourse() != null && clazz.getCourse() != null &&
                    registered.getCourse().getCourseID().equals(clazz.getCourse().getCourseID())) {
                    return "Không thể đăng ký: Khóa học " + clazz.getCourse().getCourseID() + " đã được đăng ký trước đó!";
                }
            }

            // Đăng ký thành công
            registeredClasses.add(clazz);
            return "Đăng ký lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            return "Error registering class: " + e.getMessage();
        }
    }

    public String delete(Integer clazzID) {
        try {
            // Tìm lớp trong danh sách đã đăng ký
            Clazz clazzToRemove = null;
            for (Clazz clazz : registeredClasses) {
                if (clazz.getClazzID() == clazzID) {
                    clazzToRemove = clazz;
                    break;
                }
            }

            if (clazzToRemove == null) {
                return "Lớp ID: " + clazzID + " không nằm trong danh sách đã đăng ký!";
            }

            registeredClasses.remove(clazzToRemove);
            return "Xóa lớp ID: " + clazzID + " thành công!";
        } catch (Exception e) {
            return "Error deleting class: " + e.getMessage();
        }
    }

    public List<Clazz> getRegisteredClasses() {
        return registeredClasses;
    }
}
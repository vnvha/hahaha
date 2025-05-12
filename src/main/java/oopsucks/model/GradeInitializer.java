package oopsucks.model;

public class GradeInitializer {
    private GradeDAO gradeDAO;

    public GradeInitializer() {
        this.gradeDAO = new GradeDAO();
    }

    public void initializeGrade(Student student, Clazz clazz) {
        Grade existingGrade = gradeDAO.getGradeByStudentAndClazz(student, clazz);
        if (existingGrade == null) {
            Grade grade = new Grade(student, clazz, null, null); // Tạo mới với điểm mặc định null
            gradeDAO.saveGrade(grade);
            System.out.println("Created new Grade for student " + student.getUserID() + " in clazz " + clazz.getClazzID());
        } else {
            // Không làm gì nếu Grade đã tồn tại, giữ nguyên dữ liệu điểm hiện có
            System.out.println("Grade already exists for student " + student.getUserID() + " in clazz " + clazz.getClazzID() + ", no changes made.");
        }
    }
}
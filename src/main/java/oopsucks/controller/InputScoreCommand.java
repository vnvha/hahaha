package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;

public class InputScoreCommand extends BaseCommand<Boolean> {
    private GradeDAO gradeDAO;
    private ClazzDAO clazzDAO;
    private List<Grade> gradesToSave;

    public InputScoreCommand(List<Grade> gradesToSave) {
        this.gradeDAO = new GradeDAO();
        this.clazzDAO = new ClazzDAO();
        this.gradesToSave = gradesToSave;
    }

    @Override
    protected Boolean doExecute() throws CommandException {
        try {
            for (Grade grade : gradesToSave) {
                Float midterm = grade.getMidtermScore();
                Float finalScore = grade.getFinalScore();

                // Kiểm tra điểm hợp lệ
                if ((midterm != null && (midterm < 0 || midterm > 10)) ||
                    (finalScore != null && (finalScore < 0 || finalScore > 10))) {
                    throw new CommandException("Điểm phải nằm trong khoảng từ 0 đến 10");
                }

                // Lấy Clazz từ clazz_id để đảm bảo Course được tải
                Clazz clazz = clazzDAO.getClazzById(grade.getClazz().getClazzID());
                if (clazz == null || clazz.getCourse() == null) {
                    throw new CommandException("Không tìm thấy lớp hoặc khóa học cho grade: " + grade.getClazz().getClazzID());
                }
                grade.setClazz(clazz); // Cập nhật Clazz để đảm bảo Course được liên kết

                // Lưu điểm trực tiếp
                grade.setMidtermScore(midterm);
                grade.setFinalScore(finalScore);
                gradeDAO.saveGrade(grade);
                System.out.println("Saved Grade: studentId=" + grade.getStudent().getUserID() + ", clazzId=" + grade.getClazz().getClazzID() +
                                 ", midtermScore=" + midterm + ", finalScore=" + finalScore + ", totalScore=" + grade.getTotalScore());
            }
            return true;
        } catch (Exception e) {
            throw new CommandException("Lỗi khi lưu điểm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validate() {
        if (gradesToSave == null || gradesToSave.isEmpty()) {
            return false;
        }
        return true;
    }

    // Phương thức hỗ trợ để lấy danh sách điểm theo lớp
    public List<Grade> getGradesByClazz(Clazz clazz) {
        return gradeDAO.getGradesByClazz(clazz);
    }

    // Phương thức hỗ trợ để lấy điểm của một học sinh trong một lớp
    public Grade getGradeByStudentAndClazz(String studentId, int clazzId) {
        return gradeDAO.getGradeByStudentAndClazz2(studentId, clazzId);
    }
}
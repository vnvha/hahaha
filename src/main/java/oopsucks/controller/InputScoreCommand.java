package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;

public class InputScoreCommand {
    private GradeDAO gradeDAO;
    private ClazzDAO clazzDAO;

    public InputScoreCommand() {
        this.gradeDAO = new GradeDAO();
        this.clazzDAO = new ClazzDAO();
    }

    public List<Grade> getGradesByClazz(Clazz clazz) {
        return gradeDAO.getGradesByClazz(clazz);
    }

    public boolean saveScores(List<Grade> gradesToSave) throws Exception {
        for (Grade grade : gradesToSave) {
            Float midterm = grade.getMidtermScore();
            Float finalScore = grade.getFinalScore();

            // Kiểm tra điểm hợp lệ
            if ((midterm != null && (midterm < 0 || midterm > 10)) ||
                (finalScore != null && (finalScore < 0 || finalScore > 10))) {
                throw new IllegalArgumentException("Điểm phải nằm trong khoảng từ 0 đến 10");
            }

            // Lấy Clazz từ clazz_id để đảm bảo Course được tải
            Clazz clazz = clazzDAO.getClazzById(grade.getClazz().getClazzID());
            if (clazz == null || clazz.getCourse() == null) {
                throw new IllegalStateException("Không tìm thấy lớp hoặc khóa học cho grade: " + grade.getClazz().getClazzID());
            }
            grade.setClazz(clazz); // Cập nhật Clazz để đảm bảo Course được liên kết

            // Cập nhật điểm (totalScore, letterGrade, gradePoint sẽ được tính trong Grade)
            grade.setMidtermScore(midterm);
            grade.setFinalScore(finalScore);

            // Lưu điểm
            gradeDAO.saveGrade(grade);
            System.out.println("Saved Grade: studentId=" + grade.getStudent().getUserID() + ", clazzId=" + grade.getClazz().getClazzID() +
                              ", midtermScore=" + midterm + ", finalScore=" + finalScore + ", totalScore=" + grade.getTotalScore());
        }
        return true;
    }

    public Grade getGradeByStudentAndClazz(String studentId, int clazzId) {
        return gradeDAO.getGradeByStudentAndClazz2(studentId, clazzId);
    }
}
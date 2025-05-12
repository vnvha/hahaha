package oopsucks.controller;

import oopsucks.model.*;
import java.util.List;

public class InputScoreCommand {
    private GradeDAO gradeDAO;

    public InputScoreCommand() {
        this.gradeDAO = new GradeDAO();
    }

    public List<Grade> getGradesByClazz(Clazz clazz) {
        return gradeDAO.getGradesByClazz(clazz);
    }

    public boolean saveScores(List<Grade> gradesToSave) throws Exception {
        for (Grade grade : gradesToSave) {
            Float midterm = grade.getMidtermScore();
            Float finalScore = grade.getFinalScore();

            if ((midterm != null && (midterm < 0 || midterm > 10)) ||
                (finalScore != null && (finalScore < 0 || finalScore > 10))) {
                throw new IllegalArgumentException("Điểm phải nằm trong khoảng từ 0 đến 10");
            }

            gradeDAO.saveGrade(grade);
        }
        return true;
    }

    public Grade getGradeByStudentAndClazz(String studentId, int clazzId) {
        return gradeDAO.getGradeByStudentAndClazz2(studentId, clazzId);
    }
}

package oopsucks.view;

import oopsucks.controller.CalculateGPACommand;
import oopsucks.controller.CheckGraduationCommand;
import oopsucks.controller.LoadCourseDataCommand;
import oopsucks.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingProgramPanel extends JPanel {
    private final String studentAccountName;
    private final JTable courseTable;
    private final DefaultTableModel tableModel;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final UserDAO userDAO;
    private JLabel gpaLabel;
    private JTextArea resultLabel;

    public TrainingProgramPanel(String accountName, JPanel cardPanel, CardLayout cardLayout) {
        this.studentAccountName = accountName;
        this.courseDAO = new CourseDAO();
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Chương Trình Đào Tạo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "CreditBasedStudent"));
        headerPanel.add(backButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table Model
        String[] columnNames = {
            "Mã HP", "Tên học phần", "Số tín chỉ", "Viện", "Điểm quá trình",
            "Điểm cuối kỳ", "Điểm tổng kết", "Điểm chữ", "Điểm hệ 4", "Bắt buộc"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 9) {
                    Course course = courseDAO.getCourseById((String) getValueAt(row, 0));
                    return course != null && !course.isMandatory();
                }
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 9) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setFont(new Font("Arial", Font.PLAIN, 14));
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        courseTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(scrollPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Add result label/text area to the left side of footer
        resultLabel = new JTextArea(3, 40);
        resultLabel.setEditable(false);
        resultLabel.setLineWrap(true);
        resultLabel.setWrapStyleWord(true);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        resultLabel.setBackground(footerPanel.getBackground());
        JScrollPane resultScrollPane = new JScrollPane(resultLabel);
        resultScrollPane.setBorder(BorderFactory.createEmptyBorder());
        footerPanel.add(resultScrollPane, BorderLayout.WEST);

        // Right: GPA Label and Check Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gpaLabel = new JLabel("GPA: ");
        gpaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rightPanel.add(gpaLabel);

        JButton checkGraduationButton = new JButton("Kiểm tra điều kiện tốt nghiệp");
        checkGraduationButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkGraduationButton.addActionListener(e -> checkGraduation());
        rightPanel.add(checkGraduationButton);

        footerPanel.add(rightPanel, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);

        // Load data and calculate GPA
        loadCourseData();
        calculateAndDisplayGPA();
    }

    private void loadCourseData() {
        LoadCourseDataCommand loader = new LoadCourseDataCommand(this, tableModel, userDAO, courseDAO, clazzDAO, gradeDAO);
        loader.loadCourseData();
    }

    private void calculateAndDisplayGPA() {
        Student student = userDAO.getStudent(studentAccountName);
        if (student == null) {
            gpaLabel.setText("GPA: N/A");
            return;
        }
        CalculateGPACommand gpaCommand = new CalculateGPACommand(student, clazzDAO, gradeDAO);
        CalculateGPACommand.GPAResult result = gpaCommand.execute();
        gpaLabel.setText(result.getMessage());
    }

    private void checkGraduation() {
        Student student = userDAO.getStudent(studentAccountName);
        if (student == null) {
            resultLabel.setText("Lỗi: Không thể tải thông tin sinh viên");
            resultLabel.setForeground(new Color(200, 0, 0));
            return;
        }

        List<Course> mandatoryCourses = new ArrayList<>();
        List<Course> selectedOptionalCourses = new ArrayList<>();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String courseId = (String) tableModel.getValueAt(row, 0);
            Boolean isSelected = (Boolean) tableModel.getValueAt(row, 9);
            Course course = courseDAO.getCourseById(courseId);
            if (course == null) continue;

            if (course.isMandatory()) {
                mandatoryCourses.add(course);
            } else if (isSelected != null && isSelected) {
                selectedOptionalCourses.add(course);
            }
        }

        if (selectedOptionalCourses.size() != 2) {
            resultLabel.setText("Vui lòng chọn đúng 2 môn tự chọn");
            resultLabel.setForeground(new Color(200, 0, 0));
            return;
        }

        CheckGraduationCommand graduationCommand = new CheckGraduationCommand(
            student, mandatoryCourses, selectedOptionalCourses, clazzDAO, gradeDAO
        );
        CheckGraduationCommand.Result result = graduationCommand.execute();

        resultLabel.setText(result.getMessage());
        resultLabel.setForeground(result.isQualified() ? new Color(0, 128, 0) : new Color(200, 0, 0));
    }

    // Getter methods for helper classes
    public String getStudentAccountName() {
        return studentAccountName;
    }

    public JLabel getGpaLabel() {
        return gpaLabel;
    }

    public JTextArea getResultLabel() {
        return resultLabel;
    }

    // Utility methods
    public Clazz findRegisteredClazz(Student student, Course course) {
        List<Clazz> studentClazzes = clazzDAO.getClazzesByStudent(student);
        for (Clazz clazz : studentClazzes) {
            if (clazz.getCourse() != null &&
                clazz.getCourse().getCourseID().equals(course.getCourseID())) {
                return clazz;
            }
        }
        return null;
    }

    public String formatGrade(Float grade) {
        if (grade == null) {
            return "";
        }
        return String.format("%.2f", grade);
    }
}
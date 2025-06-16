package oopsucks.view;

import oopsucks.controller.*;
import oopsucks.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TrainingProgramPanel extends JPanel {
    private final String studentID;
    private final JTable courseTable;
    private final DefaultTableModel tableModel;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final UserDAO userDAO;
    private final TuitionFeeDAO tuitionFeeDAO;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private JLabel gpaLabel;
    private JTextArea resultLabel;

    public TrainingProgramPanel(String userID, JPanel cardPanel, CardLayout cardLayout) {
        this.studentID = userID;
        this.courseDAO = new CourseDAO();
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.userDAO = new UserDAO();
        this.tuitionFeeDAO = new TuitionFeeDAO();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;

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
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "StudentPanel"));
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

        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = courseTable.getSelectedRow();
                    int column = courseTable.columnAtPoint(e.getPoint());
                    if (row >= 0 && column == 0) {
                        String courseId = (String) tableModel.getValueAt(row, 0);
                        CourseDetailPanel detailPanel = new CourseDetailPanel(courseId, courseDAO, cardPanel, cardLayout);
                        cardPanel.add(detailPanel, "CourseDetail");
                        cardLayout.show(cardPanel, "CourseDetail");
                    }
                }
            }
        });

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

        // Right: GPA Label, Check Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gpaLabel = new JLabel("GPA: ");
        gpaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rightPanel.add(gpaLabel);

        JButton checkGraduationButton = new JButton("Kiểm tra điều kiện tốt nghiệp");
        checkGraduationButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkGraduationButton.addActionListener(e -> checkGraduation());
        rightPanel.add(checkGraduationButton);

        JButton checkTuitionButton = new JButton("Kiểm tra công nợ");
        checkTuitionButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkTuitionButton.addActionListener(e -> checkTuition());
        rightPanel.add(checkTuitionButton);

        footerPanel.add(rightPanel, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);

        // Load data and calculate GPA
        loadCourseData();
        calculateAndDisplayGPA();
    }

    private void loadCourseData() {
        try {
            new LoadCourseDataCommand(
                tableModel,
                userDAO, courseDAO, clazzDAO, gradeDAO,
                true,
                studentID
            ).execute();
        } catch (Exception e) {
            getResultLabel().setText("Lỗi: " + e.getMessage());
        }
    }
    
    private void calculateAndDisplayGPA() {
        try {
            Student student = userDAO.getStudent(studentID);
            CalculateGPACommand gpaCommand = new CalculateGPACommand(student, courseDAO, clazzDAO, gradeDAO, true);
            if (!gpaCommand.validate()) {
                gpaLabel.setText("Điểm TBTL: N/A");
                return;
            }

            CalculateGPACommand.GPAResult result = gpaCommand.execute();
            gpaLabel.setText(result.getMessage());
        } catch (CommandException e) {
            gpaLabel.setText("Lỗi: Không thể tính GPA");
            System.err.println("Error calculating GPA: " + e.getMessage());
        }
    }

    private void checkGraduation() {
        Student student = userDAO.getStudent(studentID);
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

        // Sử dụng constructor mới với tham số TuitionFeeDAO
        CheckGraduationCommand graduationCommand = new CheckGraduationCommand(
            student, mandatoryCourses, selectedOptionalCourses, clazzDAO, gradeDAO, tuitionFeeDAO
        );
        
        try {
        	CheckGraduationCommand.Result result = graduationCommand.execute();

        	resultLabel.setText(result.getMessage());
        	resultLabel.setForeground(result.isQualified() ? new Color(0, 128, 0) : new Color(200, 0, 0));
        } catch (CommandException e) {

    		System.err.println("Error checking graduation status: " + e.getMessage());
    	}
    }

    private void checkTuition() {
        // Cập nhật học phí cho tất cả kỳ học
        Student student = userDAO.getStudent(studentID);
        if (student == null) {
            resultLabel.setText("Lỗi: Không thể tải thông tin sinh viên");
            resultLabel.setForeground(new Color(200, 0, 0));
            return;
        }

        // Tạo và thêm TuitionFeePanel
        StudentTuitionFeePanel studentTuitionPanel = new StudentTuitionFeePanel(studentID, cardPanel, cardLayout, new TuitionFeeDAO(), userDAO, false);
        cardPanel.add(studentTuitionPanel, "studentCreditTuitionPanel");
        cardLayout.show(cardPanel, "studentCreditTuitionPanel");
    }

    // Getter methods for helper classes
    public String studentID() {
        return studentID;
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
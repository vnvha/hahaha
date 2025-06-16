package oopsucks.view;

import oopsucks.controller.*;
import oopsucks.model.*;
import oopsucks.view.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Panel to display the training program for annual system students
 */
public class AnnualTrainingProgramPanel extends JPanel {
    private final String studentID;
    private final JTable courseTable;
    private final DefaultTableModel tableModel;
    private final CourseDAO courseDAO;
    private final ClazzDAO clazzDAO;
    private final GradeDAO gradeDAO;
    private final UserDAO userDAO;
    private JLabel gpaLabel;
    private JTextArea resultLabel;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final AnnualTuitionFeeDAO annualTuitionFeeDAO;

    public AnnualTrainingProgramPanel(String userID, JPanel cardPanel, CardLayout cardLayout) {
        this.studentID = userID;
        this.courseDAO = new CourseDAO();
        this.clazzDAO = new ClazzDAO();
        this.gradeDAO = new GradeDAO();
        this.userDAO = new UserDAO();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.annualTuitionFeeDAO = new AnnualTuitionFeeDAO();

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
            "Mã HP", "Tên học phần", "Viện", "Điểm quá trình",
            "Điểm cuối kỳ", "Điểm tổng kết", "Điểm chữ", "Điểm hệ 4"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa
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

        // Right: GPA Label, Check Graduation Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gpaLabel = new JLabel("Điểm TB tích lũy: ");
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
                false,
                studentID 
            ).execute();
        } catch (Exception e) {
            getResultLabel().setText("Lỗi: " + e.getMessage());
        }
    }

    private void calculateAndDisplayGPA() {
        try {
            Student student = userDAO.getStudent(studentID);
            CalculateGPACommand gpaCommand = new CalculateGPACommand(student, courseDAO, clazzDAO, gradeDAO, false);
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

        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses == null || allCourses.isEmpty()) {
            resultLabel.setText("Lỗi: Không có khóa học nào để kiểm tra");
            resultLabel.setForeground(new Color(200, 0, 0));
            return;
        }

        // Lọc các khóa học có institute khớp với sinh viên
        List<Course> studentCourses = new ArrayList<>();
        String studentInstitute = student.getInstitute();
        for (Course course : allCourses) {
            if (course.getInstitute().equals(studentInstitute)) {
                studentCourses.add(course);
            }
        }

        try {
            CheckAnnualGraduationCommand gradCommand = new CheckAnnualGraduationCommand(student, studentCourses, gradeDAO);
            CheckAnnualGraduationCommand.Result result = gradCommand.execute();
  
            resultLabel.setText(result.getMessage());
            resultLabel.setForeground(result.isQualified() ? new Color(0, 128, 0) : new Color(200, 0, 0));
            
        } catch (CommandException e) {
            // Handle the exception (e.g., log error or display error message)
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
        StudentTuitionFeePanel studentTuitionPanel = new StudentTuitionFeePanel(studentID, cardPanel, cardLayout, new TuitionFeeDAO(), userDAO, true);
        cardPanel.add(studentTuitionPanel, "studentCreditTuitionPanel");
        cardLayout.show(cardPanel, "studentCreditTuitionPanel");
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

    // Getter for resultLabel to be used by LoadAnnualCourseDataCommand
    public JTextArea getResultLabel() {
        return resultLabel;
    }
    
    public String studentID() {
        return studentID;
    }
}
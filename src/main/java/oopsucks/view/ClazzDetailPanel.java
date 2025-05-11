package oopsucks.view;

import oopsucks.model.Clazz;
import oopsucks.model.Student;
import oopsucks.model.Grade;
import oopsucks.model.GradeDAO;
import oopsucks.model.ClazzDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClazzDetailPanel extends JPanel {
    private String accountName;
    private Clazz clazz;
    private Student currentStudent;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public ClazzDetailPanel(String accountName, Clazz clazz, Student currentStudent, JPanel cardPanel, CardLayout cardLayout) {
        this.accountName = accountName;
        this.clazz = clazz;
        this.currentStudent = currentStudent;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Đảm bảo danh sách sinh viên được tải
        loadStudentsList();
        
        initializeUI();
    }
    
    private void loadStudentsList() {
        // Sử dụng ClazzDAO để lấy đối tượng clazz với danh sách sinh viên đã được tải
        ClazzDAO clazzDAO = new ClazzDAO();
        Clazz loadedClazz = clazzDAO.getClazzWithStudents(clazz.getClazzID());
        if (loadedClazz != null) {
            this.clazz = loadedClazz; // Cập nhật đối tượng clazz với danh sách sinh viên đã được tải
        }
    }

    private void initializeUI() {
        // Tiêu đề
        JLabel titleLabel = new JLabel("Chi tiết lớp học", SwingConstants.LEFT); // căn trái
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Tạo panel chứa cả thông tin chi tiết và danh sách sinh viên
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Panel chứa thông tin chi tiết, đặt ở góc trên bên trái
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.setBackground(Color.WHITE);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;

        int row = 0;
        gbc.gridy = row++;
        addDetailLabel(detailsPanel, gbc, "ID lớp:", String.valueOf(clazz.getClazzID()), 0);
        addDetailLabel(detailsPanel, gbc, "Môn học:", clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A", 0);
        addDetailLabel(detailsPanel, gbc, "Bắt đầu:", clazz.getStartTime(), 0);
        addDetailLabel(detailsPanel, gbc, "Kết thúc:", clazz.getEndTime(), 0);
        addDetailLabel(detailsPanel, gbc, "Thứ:", clazz.getDayOfWeek(), 0);
        addDetailLabel(detailsPanel, gbc, "Phòng:", clazz.getRoom(), 0);
        addDetailLabel(detailsPanel, gbc, "Giảng viên:", clazz.getTeacher() != null ? clazz.getTeacher().getUserName() : "N/A", 0);

        GradeDAO gradeDAO = new GradeDAO();
        Grade grade = gradeDAO.getGradeByStudentAndClazz(currentStudent, clazz);

        String midtermScore = grade != null && grade.getMidtermScore() != null ? String.valueOf(grade.getMidtermScore()) : "Chưa có";
        String finalScore = grade != null && grade.getFinalScore() != null ? String.valueOf(grade.getFinalScore()) : "Chưa có";
        String totalScore = grade != null && grade.getTotalScore() != null ? String.valueOf(grade.getTotalScore()) : "Chưa có";
        String letterGrade = grade != null && grade.getLetterGrade() != null ? grade.getLetterGrade() : "Chưa có";

        gbc.gridy = 0;
        addDetailLabel(detailsPanel, gbc, "Điểm giữa kỳ:", midtermScore, 2);
        addDetailLabel(detailsPanel, gbc, "Điểm cuối kỳ:", finalScore, 2);
        addDetailLabel(detailsPanel, gbc, "Tổng điểm:", totalScore, 2);
        addDetailLabel(detailsPanel, gbc, "Điểm chữ:", letterGrade, 2);

        wrapperPanel.add(detailsPanel);
        contentPanel.add(wrapperPanel, BorderLayout.NORTH);
        
        // Thêm danh sách sinh viên đã đăng ký
        JPanel studentsPanel = new JPanel(new BorderLayout());
        studentsPanel.setBackground(Color.WHITE);
        studentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel studentsLabel = new JLabel("Danh sách sinh viên đã đăng ký", SwingConstants.LEFT);
        studentsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        studentsPanel.add(studentsLabel, BorderLayout.NORTH);
        
        // Tạo bảng sinh viên
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("MSSV");
        tableModel.addColumn("Họ tên");
        tableModel.addColumn("Email");
        tableModel.addColumn("Ngành");
        tableModel.addColumn("Khoa");
        
        // Thêm dữ liệu sinh viên vào bảng
        List<Student> students = clazz.getStudents();
        if (students != null && !students.isEmpty()) {
            for (Student student : students) {
                tableModel.addRow(new Object[]{
                    student.getUserID(),
                    student.getUserName(),
                    student.getEmail(),
                    student.getMajor(),
                    student.getFaculty()
                });
            }
        }
        
        JTable studentsTable = new JTable(tableModel);
        studentsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        studentsPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(studentsPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Schedule"));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setBackground(Color.WHITE);
        backPanel.add(backButton);
        add(backPanel, BorderLayout.SOUTH);
    }

    private void addDetailLabel(JPanel panel, GridBagConstraints gbc, String labelText, String value, int columnOffset) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = columnOffset;
        panel.add(label, gbc);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = columnOffset + 1;
        panel.add(valueLabel, gbc);

        gbc.gridy++;
    }
}
package oopsucks.view;

import oopsucks.model.AnnualTuitionFee;
import oopsucks.model.AnnualTuitionFeeDAO;
import oopsucks.model.Student;
import oopsucks.model.UserDAO;
import oopsucks.model.ClazzDAO;
import oopsucks.model.YearBasedStudent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AnnualStudentTuitionFeePanel extends JPanel {
    private final String studentID;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final AnnualTuitionFeeDAO tuitionFeeDAO;
    private final UserDAO userDAO;
    private final ClazzDAO clazzDAO;
    private final JTable tuitionTable;
    private final DefaultTableModel tableModel;
    private final JLabel messageLabel;

    public AnnualStudentTuitionFeePanel(String studentID, JPanel cardPanel, CardLayout cardLayout, AnnualTuitionFeeDAO tuitionFeeDAO, UserDAO userDAO) {
        this.studentID = studentID;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.tuitionFeeDAO = tuitionFeeDAO;
        this.userDAO = userDAO;
        this.clazzDAO = new ClazzDAO();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);

        tuitionTable = new JTable();
        tableModel = new DefaultTableModel();
        initializeUI();
    }

    private void initializeUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Thông Tin Công Nợ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "annualTrainingProgramPanel"));
        headerPanel.add(backButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(messageLabel, BorderLayout.SOUTH);

        // Table Model for Tuition Fees
        String[] columnNames = {
            "Kỳ học", "Phí học kỳ", "Trạng thái thanh toán"
        };
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        tuitionTable.setModel(tableModel);
        tuitionTable.setFont(new Font("Arial", Font.PLAIN, 14));
        tuitionTable.setRowHeight(25);
        tuitionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tuitionTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(tuitionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Load tuition fee data
        loadTuitionFeeData();
    }

    private void loadTuitionFeeData() {
        // Kiểm tra sinh viên
        Student student = userDAO.getStudent(studentID);
        if (student == null) {
            showMessage("Không tìm thấy thông tin sinh viên!", Color.RED);
            return;
        }

        // Kiểm tra xem sinh viên có phải là YearBasedStudent
        if (!(student instanceof YearBasedStudent)) {
            showMessage("Sinh viên không thuộc hệ niên chế!", Color.RED);
            return;
        }

        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        // Lấy danh sách các kỳ học từ clazz_student
        List<Integer> registeredSemesters = clazzDAO.getRegisteredSemestersByStudent(studentID);
        if (registeredSemesters.isEmpty()) {
            showMessage("Sinh viên chưa đăng ký lớp học nào!", Color.RED);
            return;
        }

        // Cập nhật học phí cho các kỳ đã đăng ký
        boolean hasData = false;
        for (int semester : registeredSemesters) {
            AnnualTuitionFee fee = tuitionFeeDAO.calculateAndSaveAnnualTuitionFee(studentID, semester);
            if (fee != null) {
                hasData = true;
            }
        }

        // Lấy danh sách học phí
        List<AnnualTuitionFee> tuitionFees = tuitionFeeDAO.getAnnualTuitionFeesByStudent(studentID);
        for (AnnualTuitionFee fee : tuitionFees) {
            String status = fee.getStatus() ? "Đã thanh toán" : "Chưa thanh toán";
            tableModel.addRow(new Object[]{
                fee.getSemester(),
                String.format("%.2f", fee.getSemesterFee()),
                status
            });
        }

        if (!hasData) {
            showMessage("Không có thông tin học phí cho sinh viên này!", Color.RED);
        } else {
            showMessage("Đã hiển thị thông tin học phí!", new Color(0, 128, 0));
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}
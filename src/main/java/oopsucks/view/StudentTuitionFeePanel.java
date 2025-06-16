package oopsucks.view;

import oopsucks.model.*;
import oopsucks.controller.*;
import oopsucks.model.ClazzDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentTuitionFeePanel extends JPanel {
    private final String studentID;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final Object tuitionFeeDAO; // Can be AnnualTuitionFeeDAO or TuitionFeeDAO
    private final UserDAO userDAO;
    private final ClazzDAO clazzDAO;
    private final JTable tuitionTable;
    private final DefaultTableModel tableModel;
    private final JLabel messageLabel;
    private final boolean isAnnualStudent; // Flag to determine student type
    private final String backButtonTarget; // Navigation target for back button

    public StudentTuitionFeePanel(String studentID, JPanel cardPanel, CardLayout cardLayout, Object tuitionFeeDAO, UserDAO userDAO, boolean isAnnualStudent) {
        this.studentID = studentID;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.tuitionFeeDAO = tuitionFeeDAO;
        this.userDAO = userDAO;
        this.clazzDAO = new ClazzDAO();
        this.isAnnualStudent = isAnnualStudent;
        this.backButtonTarget = isAnnualStudent ? "annualTrainingProgramPanel" : "trainingProgram";

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
        backButton.addActionListener(e -> cardLayout.show(cardPanel, backButtonTarget));
        headerPanel.add(backButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(messageLabel, BorderLayout.SOUTH);

        // Table Model for Tuition Fees
        String[] columnNames = isAnnualStudent
            ? new String[]{"Kỳ học", "Phí học kỳ", "Trạng thái thanh toán"}
            : new String[]{"Kỳ học", "Số tín chỉ học phí", "Phí tín chỉ", "Tổng học phí", "Trạng thái thanh toán"};
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

        loadTuitionFeeData();
    }

    private void loadTuitionFeeData() {
        try {
            LoadTuitionFeeCommand command = new LoadTuitionFeeCommand(studentID, tuitionFeeDAO, userDAO, isAnnualStudent);
            List<Object[]> tableData = command.execute();

            tableModel.setRowCount(0);


            for (Object[] row : tableData) {
                tableModel.addRow(row);
            }

            // Show message
            showMessage(command.getMessage(), command.getMessageColor());
        } catch (CommandException e) {
            showMessage("Lỗi khi tải thông tin học phí: " + e.getMessage(), Color.RED);
        }
    }
    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}
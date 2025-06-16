package oopsucks.view;

import oopsucks.model.*;
import oopsucks.controller.InputScoreCommand;
import oopsucks.controller.CommandException;
import oopsucks.controller.CalculateTotalCreditsCommand;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InputScorePanel extends JPanel {
    private Clazz clazz;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JLabel notificationLabel;

    public InputScorePanel(Clazz clazz, JPanel cardPanel, CardLayout cardLayout) {
        this.clazz = clazz;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 700));

        // Tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(230, 240, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel(
            String.format("Nhập điểm cho lớp: %s - %s", clazz.getCourse().getCourseID(), clazz.getCourse().getCourseName()),
            SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // Bảng điểm
        String[] columnNames = {"Mã SV", "Tên SV", "Điểm giữa kỳ", "Điểm cuối kỳ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3;
            }
        };

        scoreTable = new JTable(tableModel);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 16));
        scoreTable.setRowHeight(30);
        scoreTable.setGridColor(new Color(200, 200, 200));
        scoreTable.setShowGrid(true);
        scoreTable.setIntercellSpacing(new Dimension(1, 1));

        // Căn giữa
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        scoreTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        scoreTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        scoreTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Độ rộng cột
        scoreTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        scoreTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        scoreTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        scoreTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        add(scrollPane, BorderLayout.CENTER);

        // Dữ liệu bảng
        refreshTable();

        // Nút và thông báo
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 18));
        confirmButton.setBackground(new Color(34, 139, 34));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setPreferredSize(new Dimension(150, 40));
        confirmButton.addActionListener(e -> confirmAndSaveScores());

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Teacher"));

        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);
        southPanel.add(buttonPanel, BorderLayout.NORTH);

        notificationLabel = new JLabel("", SwingConstants.CENTER);
        notificationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        notificationLabel.setForeground(new Color(34, 139, 34));
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southPanel.add(notificationLabel, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void confirmAndSaveScores() {
        List<Grade> gradesToSave = new ArrayList<>();
        InputScoreCommand command = new InputScoreCommand(gradesToSave); // Khởi tạo command với gradesToSave

        // Collect unique student IDs for credit update
        List<String> updatedStudentIds = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String studentId = (String) tableModel.getValueAt(i, 0);
            String midtermStr = tableModel.getValueAt(i, 2).toString().trim();
            String finalStr = tableModel.getValueAt(i, 3).toString().trim();

            Float midterm = null;
            Float finalScore = null;

            try {
                if (!midtermStr.isEmpty()) {
                    midterm = Float.parseFloat(midtermStr);
                    if (midterm < 0 || midterm > 10) throw new NumberFormatException();
                }
                if (!finalStr.isEmpty()) {
                    finalScore = Float.parseFloat(finalStr);
                    if (finalScore < 0 || finalScore > 10) throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showNotification("Điểm của SV " + studentId + " không hợp lệ. Vui lòng nhập số từ 0 đến 10.", 4000);
                return;
            }

            Grade grade = command.getGradeByStudentAndClazz(studentId, clazz.getClazzID());
            if (grade == null) {
                showNotification("Không tìm thấy dữ liệu điểm của SV " + studentId, 4000);
                return;
            }

            grade.setMidtermScore(midterm);
            grade.setFinalScore(finalScore);
            gradesToSave.add(grade);

            if (!updatedStudentIds.contains(studentId)) {
                updatedStudentIds.add(studentId);
            }
        }

        try {
            command.execute(); // Gọi execute thay vì saveScores

            // Call CalculateTotalCreditsCommand for each updated student
            // You may want to get DAO instances from a central place (e.g., AppContext), here assumed as static getters
            UserDAO userDAO = new UserDAO();
            CourseDAO courseDAO = new CourseDAO();
            GradeDAO gradeDAO = new GradeDAO();
            for (String studentId : updatedStudentIds) {
                CalculateTotalCreditsCommand calcCmd = new CalculateTotalCreditsCommand(userDAO, courseDAO, gradeDAO, studentId);
                try {
                    calcCmd.execute();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }

            showNotification("Đã lưu tất cả điểm thành công!", 3000);
            refreshTable();
        } catch (CommandException ex) {
            showNotification("Lỗi khi lưu điểm: " + ex.getMessage(), 4000);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        InputScoreCommand command = new InputScoreCommand(new ArrayList<>()); // Tạo command tạm thời để lấy dữ liệu
        List<Grade> grades = command.getGradesByClazz(clazz);
        for (Grade grade : grades) {
            Student student = grade.getStudent();
            tableModel.addRow(new Object[]{
                student.getUserID(),
                student.getUserName(),
                grade.getMidtermScore() != null ? grade.getMidtermScore().toString() : "",
                grade.getFinalScore() != null ? grade.getFinalScore().toString() : ""
            });
        }
        tableModel.fireTableDataChanged();
        scoreTable.revalidate();
        scoreTable.repaint();
    }

    private void showNotification(String message, int durationMs) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                notificationLabel.setText("");
                notificationLabel.setVisible(false);
            }
        }, durationMs);
    }
}
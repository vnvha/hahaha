package oopsucks.view;

import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;
import oopsucks.model.Student;
import oopsucks.model.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class SchedulePanel extends JPanel {
    private JTable scheduleTable;
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private String accountName;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JLabel messageLabel;

    public SchedulePanel(String accountName, JPanel cardPanel, CardLayout cardLayout) {
        this.accountName = accountName;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        add(messageLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("Lịch học của bạn", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Môn học", "Thời gian", "Thứ", "Phòng", "Giảng viên", "Chi tiết"};
        DefaultTableModel scheduleModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cột "Chi tiết" (nút) có thể tương tác
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? JButton.class : String.class;
            }
        };
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 14));
        scheduleTable.setRowHeight(30);

        // Renderer và Editor cho cột nút
        scheduleTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        scheduleTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), this));

        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "CreditBasedStudent"));
        JPanel backPanel = new JPanel();
        backPanel.setBackground(Color.WHITE);
        backPanel.add(backButton);
        add(backPanel, BorderLayout.SOUTH);

        populateSchedule();
    }

    private void populateSchedule() {
        DefaultTableModel model = (DefaultTableModel) scheduleTable.getModel();
        model.setRowCount(0);

        try {
            Student student = userDAO.getStudent(accountName);
            if (student == null) {
                showMessage("Không tìm thấy thông tin sinh viên!", Color.RED);
                return;
            }

            List<Clazz> classes = clazzDAO.getClazzesByStudent(student);
            if (classes.isEmpty()) {
                showMessage("Bạn chưa đăng ký lớp học nào!", Color.RED);
                return;
            }

            for (Clazz clazz : classes) {
                String teacherName = clazz.getTeacher() != null ? clazz.getTeacher().getUserName() : "N/A";
                model.addRow(new Object[]{
                    clazz.getClazzID(),
                    clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A",
                    clazz.getTime(),
                    clazz.getDayOfWeek(),
                    clazz.getRoom(),
                    teacherName,
                    "Xem chi tiết"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Lỗi khi tải lịch học: " + e.getMessage(), Color.RED);
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    // Chuyển sang ClazzDetailPanel
    public void showClassDetails(Clazz clazz, Student currentStudent) {
        ClazzDetailPanel detailPanel = new ClazzDetailPanel(accountName, clazz, currentStudent, cardPanel, cardLayout);
        cardPanel.add(detailPanel, "ClazzDetailPanel");
        cardLayout.show(cardPanel, "ClazzDetailPanel");
    }

    // Renderer cho cột nút
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Arial", Font.BOLD, 12));
            setBackground(new Color(70, 130, 180));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Editor cho cột nút
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private SchedulePanel panel;
        private int row;

        public ButtonEditor(JCheckBox checkBox, SchedulePanel panel) {
            super(checkBox);
            this.panel = panel;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                try {
                    int clazzID = Integer.parseInt(scheduleTable.getValueAt(row, 0).toString());
                    Clazz clazz = clazzDAO.getClazzWithStudents(clazzID); // Lấy Clazz với danh sách sinh viên
                    Student student = userDAO.getStudent(accountName);
                    if (clazz != null && student != null) {
                        panel.showClassDetails(clazz, student);
                    } else {
                        panel.showMessage("Không tìm thấy thông tin lớp học hoặc sinh viên!", Color.RED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    panel.showMessage("Lỗi khi hiển thị chi tiết: " + e.getMessage(), Color.RED);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
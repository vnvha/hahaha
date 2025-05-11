package oopsucks.view;

import oopsucks.controller.RegisterCommand;
import oopsucks.model.Clazz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegisteredPanel extends JPanel {
    private JTable registeredTable;
    private DefaultTableModel tableModel;
    private RegisterCommand registerCommand;
    private MainPanel mainPanel;

    public RegisteredPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        initializeUI();
    }

    public void setRegisterCommand(RegisterCommand command) {
        this.registerCommand = command;
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Danh sách lớp học đã đăng ký:"), BorderLayout.NORTH);

        String[] columns = {"Chọn", "Class ID", "Course ID", "Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        registeredTable = new JTable(tableModel);
        add(new JScrollPane(registeredTable), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Xóa các lớp đã chọn");
        JButton backButton = new JButton("Quay Lại");

        controlPanel.add(deleteButton);
        controlPanel.add(backButton);
        add(controlPanel, BorderLayout.SOUTH);

        deleteButton.addActionListener(e -> deleteSelectedClasses());
        backButton.addActionListener(e -> mainPanel.showClassListPanel());
    }

    public void loadRegisteredClasses() {
        tableModel.setRowCount(0);
        if (registerCommand == null) return;

        for (Clazz clazz : registerCommand.getRegisteredClasses()) {
            Object[] row = {
                    false,
                    clazz.getClazzID(),
                    clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A",
                    clazz.getTime()
            };
            tableModel.addRow(row);
        }
    }

    private void deleteSelectedClasses() {
        if (registerCommand == null) return;

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Boolean selected = (Boolean) tableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(selected)) {
                Integer clazzID = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                registerCommand.delete(clazzID);
                tableModel.removeRow(i);
            }
        }
    }
}

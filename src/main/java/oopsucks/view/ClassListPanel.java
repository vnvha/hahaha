package oopsucks.view;

import oopsucks.controller.RegisterCommand;
import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClassListPanel extends JPanel {
    private ClazzDAO clazzDAO = new ClazzDAO();
    private RegisterCommand registerCommand = new RegisterCommand();

    private JTable clazzTable;
    private JTextField clazzIdField;
    private JLabel statusLabel;
    private MainPanel mainPanel;
    private RegisteredPanel registeredPanel;

    public ClassListPanel(MainPanel mainPanel, RegisteredPanel registeredPanel) {
        this.mainPanel = mainPanel;
        this.registeredPanel = registeredPanel;
        initializeUI();
        loadClazzData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Course ID", "Time", "Start Time", "End Time", "Day", "Weeks", "Room", "Max", "Registered", "Teacher"};
        clazzTable = new JTable(new DefaultTableModel(columns, 0));
        add(new JScrollPane(clazzTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clazzIdField = new JTextField(10);
        JButton registerButton = new JButton("Register");
        JButton viewRegisteredButton = new JButton("Xem lớp đã đăng ký");

        inputPanel.add(new JLabel("Enter Class ID to Register:"));
        inputPanel.add(clazzIdField);
        inputPanel.add(registerButton);
        inputPanel.add(viewRegisteredButton);
        add(inputPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> {
            try {
                int clazzID = Integer.parseInt(clazzIdField.getText().trim());
                String result = registerCommand.execute(clazzID);
                statusLabel.setText(result);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid Class ID!");
            }
        });

        viewRegisteredButton.addActionListener(e -> {
            registeredPanel.setRegisterCommand(registerCommand);
            mainPanel.showRegisteredPanel();
        });
    }

    private void loadClazzData() {
        try {
            List<Clazz> clazzes = clazzDAO.getAllClazzes();
            DefaultTableModel model = (DefaultTableModel) clazzTable.getModel();
            model.setRowCount(0);

            for (Clazz clazz : clazzes) {
                Object[] row = {
                        clazz.getClazzID(),
                        clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A",
                        clazz.getTime(),
                        clazz.getStartTime(),
                        clazz.getEndTime(),
                        clazz.getDayOfWeek(),
                        clazz.getWeeks(),
                        clazz.getRoom(),
                        clazz.getMaxCapacity(),
                        clazz.getRegisteredCount(),
                        clazz.getTeacher() != null ? clazz.getTeacher().getUserID() : "N/A"
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading class data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

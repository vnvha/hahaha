package oopsucks.view;

import oopsucks.model.Clazz;
import oopsucks.model.ClazzDAO;
import oopsucks.controller.RegisterCommand;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClassRegistrationPanel extends JPanel {
    private JTable availableClassesTable;
    private JTable registeredClassesTable;
    private RegisterCommand registerCommand;
    private ClazzDAO clazzDAO;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String accountName;
    private JTextField clazzIdField;
    private JLabel messageLabel;

    public ClassRegistrationPanel(String accountName, JPanel cardPanel, CardLayout cardLayout) {
        this.accountName = accountName;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.clazzDAO = new ClazzDAO();
        this.registerCommand = new RegisterCommand(accountName);
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel topLabel = new JLabel("Danh sách lớp học có sẵn", SwingConstants.CENTER);
        topLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(topLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Môn học", "Thời gian", "Thứ", "Phòng", "Sĩ số tối đa", "Đã đăng ký"};
        DefaultTableModel availableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableClassesTable = new JTable(availableModel);
        availableClassesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        availableClassesTable.setRowHeight(30);
        populateAvailableClasses();
        topPanel.add(new JScrollPane(availableClassesTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.WHITE);
        JLabel clazzIdLabel = new JLabel("Nhập Clazz ID:");
        clazzIdLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        clazzIdField = new JTextField(10);
        clazzIdField.setFont(new Font("Arial", Font.PLAIN, 16));
        JButton registerButton = new JButton("Đăng ký");
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.WHITE);
        registerButton.addActionListener(e -> registerClass());
        inputPanel.add(clazzIdLabel);
        inputPanel.add(clazzIdField);
        inputPanel.add(registerButton);
        topPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        JLabel bottomLabel = new JLabel("Danh sách lớp đã đăng ký", SwingConstants.CENTER);
        bottomLabel.setFont(new Font("Arial", Font.BOLD, 20));
        bottomPanel.add(bottomLabel, BorderLayout.NORTH);

        String[] regColumns = {"Chọn", "ID", "Môn học", "Thời gian", "Thứ", "Phòng"};
        DefaultTableModel registeredModel = new DefaultTableModel(regColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        registeredClassesTable = new JTable(registeredModel);
        registeredClassesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        registeredClassesTable.setRowHeight(30);
        bottomPanel.add(new JScrollPane(registeredClassesTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton deleteButton = new JButton("Xóa các lớp đã chọn");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteButton.setBackground(new Color(70, 130, 180));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelectedClasses());
        buttonPanel.add(deleteButton);

        JButton finishButton = new JButton("Hoàn thành đăng ký");
        finishButton.setFont(new Font("Arial", Font.BOLD, 16));
        finishButton.setBackground(new Color(70, 130, 180));
        finishButton.setForeground(Color.WHITE);
        finishButton.addActionListener(e -> finishRegistration());
        buttonPanel.add(finishButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "CreditBasedStudent"));
        JPanel backPanel = new JPanel();
        backPanel.setBackground(Color.WHITE);
        backPanel.add(backButton);
        add(backPanel, BorderLayout.SOUTH);

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        add(splitPane, BorderLayout.CENTER);

        populateRegisteredClasses();
    }

    private void populateAvailableClasses() {
        DefaultTableModel model = (DefaultTableModel) availableClassesTable.getModel();
        model.setRowCount(0);
        List<Clazz> classes = clazzDAO.getAllClazzes();
        for (Clazz clazz : classes) {
            model.addRow(new Object[]{
                clazz.getClazzID(),
                clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A",
                clazz.getTime(),
                clazz.getDayOfWeek(),
                clazz.getRoom(),
                clazz.getMaxCapacity(),
                clazz.getRegisteredCount()
            });
        }
    }

    private void populateRegisteredClasses() {
        DefaultTableModel model = (DefaultTableModel) registeredClassesTable.getModel();
        model.setRowCount(0);
        List<Clazz> registeredClasses = registerCommand.getRegisteredClasses();
        for (Clazz clazz : registeredClasses) {
            model.addRow(new Object[]{
                false,
                clazz.getClazzID(),
                clazz.getCourse() != null ? clazz.getCourse().getCourseID() : "N/A",
                clazz.getTime(),
                clazz.getDayOfWeek(),
                clazz.getRoom()
            });
        }
    }

    private void registerClass() {
        try {
            String input = clazzIdField.getText().trim();
            if (input.isEmpty()) {
                showMessage("Vui lòng nhập mã lớp!", Color.RED);
                return;
            }
            Integer clazzID = Integer.parseInt(input);
            String result = registerCommand.execute(clazzID);
            if (result.contains("thành công")) {
                showMessage(result, new Color(0, 128, 0));
                populateRegisteredClasses();
                clazzIdField.setText("");
            } else {
                showMessage(result, Color.RED);
            }
        } catch (NumberFormatException e) {
            showMessage("Clazz ID phải là số nguyên!", Color.RED);
        }
    }

    private void deleteSelectedClasses() {
        DefaultTableModel model = (DefaultTableModel) registeredClassesTable.getModel();
        boolean found = false;
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Boolean isSelected = (Boolean) model.getValueAt(i, 0);
            if (Boolean.TRUE.equals(isSelected)) {
                Integer clazzID = Integer.parseInt(model.getValueAt(i, 1).toString());
                String result = registerCommand.delete(clazzID);
                showMessage(result, Color.RED);
                found = true;
            }
        }
        if (!found) {
            showMessage("Vui lòng chọn lớp để xóa.", Color.RED);
        }
        populateRegisteredClasses();
    }

    private void finishRegistration() {

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return registerCommand.finishRegistration();
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.contains("thành công")) {
                        showMessage(result, new Color(0, 128, 0));
                        populateAvailableClasses();
                        populateRegisteredClasses();
                        cardLayout.show(cardPanel, "CreditBasedStudent");
                    } else {
                        showMessage(result, Color.RED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Có lỗi xảy ra: " + e.getMessage(), Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}

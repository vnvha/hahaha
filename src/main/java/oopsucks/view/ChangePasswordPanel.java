package oopsucks.view;

import oopsucks.model.UserDAO;
import javax.swing.*;
import java.awt.*;

public class ChangePasswordPanel extends JPanel {
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;
    private String userID;

    public ChangePasswordPanel(String userID, JPanel cardPanel, CardLayout cardLayout) {
        this.userID = userID;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Thay đổi mật khẩu");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(400, 300)); // Giới hạn chiều rộng

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User ID display
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userIDLabel = new JLabel("Mã người dùng:");
        userIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(userIDLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JLabel userIDValueLabel = new JLabel(userID);
        userIDValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(userIDValueLabel, gbc);

        // New Password Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel newPasswordLabel = new JLabel("Mật khẩu mới:");
        newPasswordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(newPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(newPasswordField, gbc);

        // Confirm Password Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel confirmPasswordLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(confirmPasswordField, gbc);

        // Status Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);

        // Button Panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
        confirmButton.setBackground(new Color(70, 130, 180));
        confirmButton.setForeground(Color.WHITE);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.setBackground(new Color(180, 70, 70));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel, gbc);

        // Bọc formPanel để căn giữa
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(formPanel);
        add(wrapperPanel, BorderLayout.CENTER);

        // ActionListeners
        confirmButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validate input
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showStatus("Vui lòng điền đầy đủ thông tin", Color.RED);
                return;
            }

            // Check if passwords match
            if (!newPassword.equals(confirmPassword)) {
                showStatus("Mật khẩu xác nhận không khớp", Color.RED);
                return;
            }

            // Update password
            boolean success = updatePasswordByUserID(userID, newPassword);

            if (success) {
                showStatus("Đổi mật khẩu thành công! Đang quay lại...", new Color(0, 150, 0));

                // Return to login screen after 2 seconds
                Timer timer = new Timer(2000, event -> {
                    cardLayout.show(cardPanel, "Login");
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                showStatus("Không thể cập nhật mật khẩu. Vui lòng thử lại sau.", Color.RED);
            }
        });

        cancelButton.addActionListener(e -> {
            // Go back to previous screen
            cardLayout.previous(cardPanel);
        });
    }

    private boolean updatePasswordByUserID(String userID, String newPassword) {
        try {
            UserDAO userDAO = new UserDAO();
            var student = userDAO.getStudent(userID);
            if (student != null) {
                student.setPassword(newPassword);
                userDAO.updateStudent(student);
                return true;
            }

            var teacher = userDAO.getTeacher(userID);
            if (teacher != null) {
                teacher.setPassword(newPassword);
                userDAO.saveTeacher(teacher);
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}

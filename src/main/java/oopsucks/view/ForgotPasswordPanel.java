package oopsucks.view;

import oopsucks.model.*;
import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;

public class ForgotPasswordPanel extends JPanel {
    private JTextField emailField;
    private JLabel messageLabel;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public ForgotPasswordPanel(JPanel cardPanel, CardLayout cardLayout) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("KHÔI PHỤC MẬT KHẨU");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel emailLabel = new JLabel("Nhập email khôi phục:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(emailField, gbc);

        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(messageLabel, gbc);

        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 18));
        confirmButton.setBackground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(confirmButton, gbc);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(backButton, gbc);

        confirmButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                messageLabel.setText("Vui lòng nhập email!");
                return;
            }

            UserDAO userDAO = new UserDAO();
            String newPassword = generateRandomPassword();
            boolean updated = userDAO.updatePasswordByEmail(email, newPassword);

            if (updated) {
                messageLabel.setText("Mật khẩu mới đã gửi đến email của bạn!");
                emailField.setText("");
            } else {
                messageLabel.setText("Email không tồn tại trong hệ thống!");
            }
        });

        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Login");
        });
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }
}
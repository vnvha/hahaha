package oopsucks.view;

import javax.swing.*;
import java.awt.*;

public class TeacherPanel extends JPanel {
    public TeacherPanel(String userId, JPanel cardPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel messageLabel = new JLabel("Chào mừng " + userId + " đến với hệ thống dành cho Giảng viên!");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(messageLabel, BorderLayout.CENTER);

        // Logout button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        logoutButton.setBackground(new Color(255, 99, 71));
        logoutButton.setForeground(Color.WHITE);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Login");
        });
    }
}
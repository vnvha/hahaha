package oopsucks.view;

import oopsucks.model.*;
import oopsucks.controller.*;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton creditStudentRadio, yearStudentRadio, teacherRadio;
    private ButtonGroup roleGroup;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JLabel errorLabel;

    public LoginPanel(JPanel cardPanel, CardLayout cardLayout) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel roleLabel = new JLabel("Vui lòng chọn vai trò đăng nhập:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(roleLabel, gbc);

        roleGroup = new ButtonGroup();
        creditStudentRadio = new JRadioButton("Sinh viên tín chỉ");
        yearStudentRadio = new JRadioButton("Sinh viên niên chế");
        teacherRadio = new JRadioButton("Giảng viên");
        creditStudentRadio.setFont(new Font("Arial", Font.PLAIN, 18));
        yearStudentRadio.setFont(new Font("Arial", Font.PLAIN, 18));
        teacherRadio.setFont(new Font("Arial", Font.PLAIN, 18));

        roleGroup.add(creditStudentRadio);
        roleGroup.add(yearStudentRadio);
        roleGroup.add(teacherRadio);

        JPanel radioPanel = new JPanel(new GridLayout(3, 1));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.add(creditStudentRadio);
        radioPanel.add(yearStudentRadio);
        radioPanel.add(teacherRadio);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(radioPanel, gbc);

        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 5;
        add(passwordField, gbc);

        JButton forgotPasswordButton = new JButton("Quên mật khẩu");
        forgotPasswordButton.setFont(new Font("Arial", Font.BOLD,14)); 
        forgotPasswordButton.setBackground(Color.WHITE); 
        forgotPasswordButton.setForeground(	new Color(100, 149, 237)); 
        forgotPasswordButton.setBorderPainted(false); 
        forgotPasswordButton.setContentAreaFilled(false);
        gbc.gridx = 1;
        gbc.gridy = 6; 
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END; // Căn phải
        add(forgotPasswordButton, gbc);

        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(errorLabel, gbc);

        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(new Color(70, 130, 180));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            errorLabel.setText("");
            String userID = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            Role role = getSelectedRole();

            if (role == null) {
                errorLabel.setText("Vui lòng chọn vai trò!");
                return;
            }

            Command loginCommand = new LoginCommand(userID, password, role);
            if (loginCommand.execute()) {
                switch (role) {
                    case CREDITBASEDSTUDENT:
                        cardPanel.add(new StudentPanel(userID, cardPanel, cardLayout), "StudentPanel");
                        cardLayout.show(cardPanel, "StudentPanel");
                        break;
                    case YEARBASEDSTUDENT:
                    	cardPanel.add(new StudentPanel(userID, cardPanel, cardLayout), "StudentPanel");
                        cardLayout.show(cardPanel, "StudentPanel");
                        break;
                    case TEACHER:
                        cardPanel.add(new TeacherPanel(userID, cardPanel, cardLayout), "Teacher");
                        cardLayout.show(cardPanel, "Teacher");
                        break;
                }
                usernameField.setText("");
                passwordField.setText("");
                roleGroup.clearSelection();
            } else {
                errorLabel.setText("Tài khoản không có hoặc nhập sai mật khẩu!");
            }
        });

        forgotPasswordButton.addActionListener(e -> {
            cardPanel.add(new ForgotPasswordPanel(cardPanel, cardLayout), "ForgotPassword");
            cardLayout.show(cardPanel, "ForgotPassword");
        });
    }

    private Role getSelectedRole() {
        if (creditStudentRadio.isSelected()) return Role.CREDITBASEDSTUDENT;
        if (yearStudentRadio.isSelected()) return Role.YEARBASEDSTUDENT;
        if (teacherRadio.isSelected()) return Role.TEACHER;
        return null;
    }
}
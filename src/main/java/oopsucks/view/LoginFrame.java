package oopsucks.view;
import oopsucks.controller.*;

import oopsucks.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton creditStudentRadio, yearStudentRadio, teacherRadio;
    private ButtonGroup roleGroup;

    public LoginFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        // Tạo panel chính
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.setBackground(Color.WHITE);

        // Tiêu đề
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Lựa chọn vai trò
        JLabel roleLabel = new JLabel("Vui lòng chọn vai trò đăng nhập:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(roleLabel, gbc);

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
        mainPanel.add(radioPanel, gbc);

        // Tài khoản
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(usernameField, gbc);

        // Mật khẩu
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 5;
        mainPanel.add(passwordField, gbc);

        // Nút đăng nhập
        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(new Color(144, 238, 144)); // Màu xanh lá nhạt
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                Role role = getSelectedRole();

                Command loginCommand = new LoginCommand(username, password, role);
                if (loginCommand.execute()) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Đăng nhập thành công!");
                    dispose();
                    new NextFrame().setVisible(true); // Thay NextFrame bằng class thực tế
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, role == null ? "Vui lòng chọn vai trò!" : "Tài khoản không có hoặc nhập sai mật khẩu!");
                }
            }
        });

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private Role getSelectedRole() {
        if (creditStudentRadio.isSelected()) return Role.CREDITBASEDSTUDENT;
        if (yearStudentRadio.isSelected()) return Role.YEARBASEDSTUDENT;
        if (teacherRadio.isSelected()) return Role.TEACHER;
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

class NextFrame extends JFrame {
    public NextFrame() {
        setTitle("Trang Tiếp Theo");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(new JLabel("Chào mừng đến trang tiếp theo!"));
        setLocationRelativeTo(null);
    }
}
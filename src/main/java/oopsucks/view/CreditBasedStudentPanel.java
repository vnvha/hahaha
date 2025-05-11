package oopsucks.view;

import oopsucks.model.CreditBasedStudent;
import oopsucks.model.Student;
import oopsucks.model.UserDAO;

import javax.swing.*;
import java.awt.*;

public class CreditBasedStudentPanel extends JPanel {
    public CreditBasedStudentPanel(String accountName, JPanel cardPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton registerClassButton = new JButton("Đăng ký lớp học");
        JButton trainingProgramButton = new JButton("Chương trình đào tạo");
        JButton timetableButton = new JButton("Thời khóa biểu");

        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        registerClassButton.setFont(buttonFont);
        trainingProgramButton.setFont(buttonFont);
        timetableButton.setFont(buttonFont);
        registerClassButton.setBackground(new Color(70, 130, 180));
        trainingProgramButton.setBackground(new Color(70, 130, 180));
        timetableButton.setBackground(new Color(70, 130, 180));
        registerClassButton.setForeground(Color.WHITE);
        trainingProgramButton.setForeground(Color.WHITE);
        timetableButton.setForeground(Color.WHITE);
        registerClassButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        trainingProgramButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        timetableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerClassButton.setMaximumSize(new Dimension(250, 40));
        trainingProgramButton.setMaximumSize(new Dimension(250, 40));
        timetableButton.setMaximumSize(new Dimension(250, 40));

        // Add action listeners (placeholder functionality)
        registerClassButton.addActionListener(e -> {
            cardPanel.add(new ClassRegistrationPanel(accountName, cardPanel, cardLayout), "ClassRegistration");
            cardLayout.show(cardPanel, "ClassRegistration");
        });
        timetableButton.addActionListener(e -> {
            cardPanel.add(new SchedulePanel( accountName,  cardPanel,  cardLayout), "Schedule");
            cardLayout.show(cardPanel, "Schedule");
        });
        trainingProgramButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Chức năng Thời khóa biểu đang được phát triển!");
        });
        
        buttonPanel.add(registerClassButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(trainingProgramButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(timetableButton);

        add(buttonPanel, BorderLayout.WEST);

        // Center panel for student information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fetch student data from database
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(accountName);

        // Add student information labels
        JLabel titleLabel = new JLabel("Thông tin sinh viên");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        infoPanel.add(titleLabel, gbc);

        if (student instanceof CreditBasedStudent creditStudent) {
            JLabel idLabel = new JLabel("Mã sinh viên: " + creditStudent.getUserID());
            idLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            infoPanel.add(idLabel, gbc);

            JLabel nameLabel = new JLabel("Họ và tên: " + creditStudent.getUserName());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 2;
            infoPanel.add(nameLabel, gbc);

            JLabel dobLabel = new JLabel("Ngày sinh: " + creditStudent.getDob());
            dobLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 3;
            infoPanel.add(dobLabel, gbc);

            JLabel emailLabel = new JLabel("Email: " + creditStudent.getEmail());
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 4;
            infoPanel.add(emailLabel, gbc);

            JLabel majorLabel = new JLabel("Ngành: " + creditStudent.getMajor());
            majorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 5;
            infoPanel.add(majorLabel, gbc);

        } else {
            JLabel errorLabel = new JLabel("Không tìm thấy thông tin sinh viên!");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            errorLabel.setForeground(Color.RED);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            infoPanel.add(errorLabel, gbc);
        }

        // Add info panel to the CENTER
        add(infoPanel, BorderLayout.CENTER);

        // Logout button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        logoutButton.setBackground(new Color(70, 130, 180));
        logoutButton.setForeground(Color.WHITE);
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Login");
        });
    }
}
package oopsucks.view;

import oopsucks.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class StudentPanel extends JPanel {
    private JLabel notificationLabel;
    private JPanel infoPanel;
    private String userID;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public StudentPanel(String userID, JPanel cardPanel, CardLayout cardLayout) {
        this.userID = userID;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Fetch student data to determine type
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(userID);

        // Common button for all students
        JButton timetableButton = new JButton("Thời khóa biểu");
        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        timetableButton.setFont(buttonFont);
        timetableButton.setBackground(new Color(70, 130, 180));
        timetableButton.setForeground(Color.WHITE);
        timetableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        timetableButton.setMaximumSize(new Dimension(250, 40));

        timetableButton.addActionListener(e -> {
            cardPanel.add(new SchedulePanel(userID, cardPanel, cardLayout), "Schedule");
            cardLayout.show(cardPanel, "Schedule");
        });

        // Add buttons conditionally for CreditBasedStudent
        if (student instanceof CreditBasedStudent) {
            JButton registerClassButton = new JButton("Đăng ký lớp học");
            JButton trainingProgramButton = new JButton("Chương trình đào tạo");

            registerClassButton.setFont(buttonFont);
            trainingProgramButton.setFont(buttonFont);
            registerClassButton.setBackground(new Color(70, 130, 180));
            trainingProgramButton.setBackground(new Color(70, 130, 180));
            registerClassButton.setForeground(Color.WHITE);
            trainingProgramButton.setForeground(Color.WHITE);
            registerClassButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            trainingProgramButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            registerClassButton.setMaximumSize(new Dimension(250, 40));
            trainingProgramButton.setMaximumSize(new Dimension(250, 40));

            registerClassButton.addActionListener(e -> {
                if (RegistrationManager.getRegisterStatus()) {
                    ClassRegistrationPanel classRegistrationPanel = new ClassRegistrationPanel(userID, cardPanel, cardLayout);
                    cardPanel.add(classRegistrationPanel, "ClassRegistration");
                    classRegistrationPanel.populateAvailableClasses();
                    classRegistrationPanel.populateRegisteredClasses();
                    cardLayout.show(cardPanel, "ClassRegistration");
                } else {
                    showNotification("Hiện không phải thời gian đăng ký lớp học");
                }
            });

            trainingProgramButton.addActionListener(e -> {
                cardPanel.add(new TrainingProgramPanel(userID, cardPanel, cardLayout), "trainingProgram");
                cardLayout.show(cardPanel, "trainingProgram");
            });

            // Add buttons to panel for CreditBasedStudent
            buttonPanel.add(registerClassButton);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(trainingProgramButton);
            buttonPanel.add(Box.createVerticalStrut(10));
        }
        
        if (student instanceof YearBasedStudent) {

        	
        	JButton trainingProgramButton1 = new JButton("Chương trình đào tạo");
            trainingProgramButton1.setFont(buttonFont);
            trainingProgramButton1.setBackground(new Color(70, 130, 180));
            trainingProgramButton1.setForeground(Color.WHITE);
            trainingProgramButton1.setAlignmentX(Component.CENTER_ALIGNMENT);
            trainingProgramButton1.setMaximumSize(new Dimension(250, 40));
            trainingProgramButton1.addActionListener(e -> {
               cardPanel.add(new AnnualTrainingProgramPanel(userID, cardPanel, cardLayout), "annualTrainingProgramPanel");
               cardLayout.show(cardPanel, "annualTrainingProgramPanel");
            });
            buttonPanel.add(trainingProgramButton1);
            buttonPanel.add(Box.createVerticalStrut(10));
        }

        // Add timetable button (common for all students)
        buttonPanel.add(timetableButton);

        add(buttonPanel, BorderLayout.WEST);

        // Center panel for student information
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);

        // Initial data load
        refreshData();

        add(infoPanel, BorderLayout.CENTER);

        // Bottom panel with notification and logout
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        logoutButton.setBackground(new Color(70, 130, 180));
        logoutButton.setForeground(Color.WHITE);

        notificationLabel = new JLabel("");
        notificationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        notificationLabel.setForeground(Color.RED);
        notificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.setLayout(new BoxLayout(logoutPanel, BoxLayout.Y_AXIS));
        logoutPanel.add(notificationLabel);
        logoutPanel.add(Box.createVerticalStrut(5));
        logoutPanel.add(logoutButton);

        add(logoutPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Login");
        });

        // Listen for panel display events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
    }

    private void refreshData() {
        // Clear previous content
        infoPanel.removeAll();

        // GridBagConstraints setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fetch student data from database
        UserDAO userDAO = new UserDAO();
        Student student = userDAO.getStudent(userID);

        // Add title
        JLabel titleLabel = new JLabel("Thông tin sinh viên");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        infoPanel.add(titleLabel, gbc);

        if (student != null) {
            JLabel idLabel = new JLabel("Mã sinh viên: " + student.getUserID());
            idLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            infoPanel.add(idLabel, gbc);

            JLabel nameLabel = new JLabel("Họ và tên: " + student.getUserName());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 2;
            infoPanel.add(nameLabel, gbc);

            JLabel dobLabel = new JLabel("Ngày sinh: " + student.getDob());
            dobLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 3;
            infoPanel.add(dobLabel, gbc);

            JLabel emailLabel = new JLabel("Email: " + student.getEmail());
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 4;
            infoPanel.add(emailLabel, gbc);

            JLabel majorLabel = new JLabel("Ngành: " + student.getMajor());
            majorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            gbc.gridx = 0;
            gbc.gridy = 5;
            infoPanel.add(majorLabel, gbc);

            // Display totalCredits for CreditBasedStudent or academicYear for YearBasedStudent
            if (student instanceof CreditBasedStudent creditStudent) {
                JLabel creditLabel = new JLabel("Số tín chỉ tích lũy: " + creditStudent.getTotalCredits());
                creditLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                gbc.gridx = 0;
                gbc.gridy = 6;
                infoPanel.add(creditLabel, gbc);
            } else if (student instanceof YearBasedStudent yearStudent) {
                JLabel yearLabel = new JLabel("Niên khóa: " + yearStudent.getAcademicYear());
                yearLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                gbc.gridx = 0;
                gbc.gridy = 6;
                infoPanel.add(yearLabel, gbc);
            }

            // Add Change Password button
            JButton changePasswordButton = new JButton("Thay đổi mật khẩu");
            changePasswordButton.setFont(new Font("Arial", Font.BOLD, 16));
            changePasswordButton.setBackground(Color.WHITE);
            changePasswordButton.setForeground(new Color(70, 130, 180));
            changePasswordButton.setBorderPainted(false);
            changePasswordButton.setFocusPainted(false);
            changePasswordButton.setContentAreaFilled(false);
            changePasswordButton.setOpaque(true);

            changePasswordButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    changePasswordButton.setBackground(new Color(230, 240, 250));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    changePasswordButton.setBackground(Color.WHITE);
                }
            });

            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.insets = new Insets(15, 5, 5, 5);
            infoPanel.add(changePasswordButton, gbc);

            changePasswordButton.addActionListener(e -> {
                cardPanel.add(new ChangePasswordPanel(userID, cardPanel, cardLayout), "ChangePassword");
                cardLayout.show(cardPanel, "ChangePassword");
            });
        } else {
            JLabel errorLabel = new JLabel("Không tìm thấy thông tin sinh viên!");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            errorLabel.setForeground(Color.RED);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            infoPanel.add(errorLabel, gbc);
        }

        // Refresh UI
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        Timer timer = new Timer(3000, e -> notificationLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
}
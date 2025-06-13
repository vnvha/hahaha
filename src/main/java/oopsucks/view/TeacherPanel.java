package oopsucks.view;

import oopsucks.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TeacherPanel extends JPanel {
    private ClazzDAO clazzDAO;
    private UserDAO userDAO;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String userID;
    private static final String ADMIN_ID = "T001"; // user_id của admin
    private JLabel notificationLabel;
    private JTextField semesterField;
    private JPanel clazzPanel;
    private JPanel buttonPanel; 

    public TeacherPanel(String userID, JPanel cardPanel, CardLayout cardLayout) {
        this.userID = userID;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Panel chứa các nút admin 
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topControlPanel.setBackground(Color.WHITE);

        // Panel chứa nút cập nhật thông tin sinh viên, đóng/mở đăng ký (cho admin)
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        // Kiểm tra xem giảng viên có phải là admin không
        Teacher teacher = userDAO.getTeacher(userID);
        if (teacher == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isAuthorized = teacher.getUserID().equals(ADMIN_ID);
        if (isAuthorized) {
            // Nút cập nhật thông tin sinh viên
            JButton updateStudentButton = new JButton("Cập nhật thông tin sinh viên");
            updateStudentButton.setFont(new Font("Arial", Font.BOLD, 16));
            updateStudentButton.setBackground(new Color(144, 238, 144));
            updateStudentButton.setForeground(Color.WHITE);
            updateStudentButton.addActionListener(e -> {
                cardPanel.add(new UpdateStudentPanel(userID, cardPanel, cardLayout), "updateStudentPanel");
                cardLayout.show(cardPanel, "updateStudentPanel");
            });
            
            updateStudentButton.addActionListener(e -> {
                cardPanel.add(new UpdateStudentPanel(userID, cardPanel, cardLayout), "updateStudentPanel");
                cardLayout.show(cardPanel, "updateStudentPanel");
            });
            buttonPanel.add(updateStudentButton);

            JButton closeRegistrationButton = new JButton("Đóng đăng ký");
            closeRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            closeRegistrationButton.setBackground(new Color(144, 238, 144));
            closeRegistrationButton.setForeground(Color.WHITE);
            closeRegistrationButton.addActionListener(e -> {
                RegistrationManager.updateRegisterStatus(false, null); // Đóng đăng ký và xóa kỳ học
                showNotification("Đã đóng đăng ký lớp học!");
            });
            buttonPanel.add(closeRegistrationButton);

            JButton openRegistrationButton = new JButton("Mở đăng ký");
            openRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            openRegistrationButton.setBackground(new Color(144, 238, 144));
            openRegistrationButton.setForeground(Color.WHITE);
            openRegistrationButton.addActionListener(e -> {
                // Xóa ô nhập trước đó nếu có
                for (Component comp : buttonPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("tempInputPanel")) {
                        buttonPanel.remove(comp);
                    }
                }

                // Tạo panel tạm thời chứa ô nhập và nút xác nhận
                JPanel tempInputPanel = new JPanel();
                tempInputPanel.setName("tempInputPanel");
                tempInputPanel.setBackground(Color.WHITE);
                JLabel label = new JLabel("Kỳ học:");
                label.setFont(new Font("Arial", Font.PLAIN, 16));
                JTextField semesterInput = new JTextField(5);
                semesterInput.setFont(new Font("Arial", Font.PLAIN, 16));
                JButton confirmButton = new JButton("Xác nhận");
                confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
                confirmButton.setBackground(new Color(34, 139, 34));
                confirmButton.setForeground(Color.WHITE);

                confirmButton.addActionListener(confirmEvent -> {
                    String input = semesterInput.getText().trim();
                    try {
                        int semester = Integer.parseInt(input);
                        RegistrationManager.updateRegisterStatus(true, semester); // Lưu trạng thái và kỳ học
                        showNotification("Đã mở đăng ký cho kỳ " + semester + "!");
                        buttonPanel.remove(tempInputPanel); // Xóa panel sau khi xác nhận
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                    } catch (NumberFormatException ex) {
                        showNotification("Kỳ học phải là số nguyên!");
                        notificationLabel.setForeground(Color.RED);
                    }
                });

                tempInputPanel.add(label);
                tempInputPanel.add(semesterInput);
                tempInputPanel.add(confirmButton);
                buttonPanel.add(tempInputPanel);

                // Cập nhật giao diện
                buttonPanel.revalidate();
                buttonPanel.repaint();

                // Tạo timer để xóa ô nhập sau 10 giây
                Timer timer = new Timer(10000, timerEvent -> {
                    if (buttonPanel.isAncestorOf(tempInputPanel)) {
                        buttonPanel.remove(tempInputPanel);
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            });
            buttonPanel.add(openRegistrationButton);
        }

        topControlPanel.add(buttonPanel);

        // Panel chứa thông tin giáo viên và ô nhập kỳ học
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Panel chứa thông tin giáo viên
        JPanel teacherInfoPanel = new JPanel();
        teacherInfoPanel.setLayout(new BoxLayout(teacherInfoPanel, BoxLayout.Y_AXIS));
        teacherInfoPanel.setBackground(new Color(240, 248, 255)); // Light blue background
        teacherInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (teacher != null) {
            JLabel nameLabel = new JLabel("Tên giáo viên: " + teacher.getUserName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel idLabel = new JLabel("ID: " + teacher.getUserID());
            idLabel.setFont(new Font("Arial", Font.BOLD, 16));
            idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel dobLabel = new JLabel("Ngày sinh: " + teacher.getDob());
            dobLabel.setFont(new Font("Arial", Font.BOLD, 16));
            dobLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            teacherInfoPanel.add(nameLabel);
            teacherInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            teacherInfoPanel.add(idLabel);
            teacherInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            teacherInfoPanel.add(dobLabel);
            
            JButton changePasswordButton = new JButton("Thay đổi mật khẩu");
            changePasswordButton.setFont(new Font("Arial", Font.BOLD, 14));
            changePasswordButton.setForeground(new Color(0, 102, 204)); 
            changePasswordButton.setBackground(Color.WHITE);
            changePasswordButton.setBorderPainted(false);
            changePasswordButton.setFocusPainted(false);
            changePasswordButton.setContentAreaFilled(false);
            changePasswordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            changePasswordButton.addActionListener(e -> {
                cardPanel.add(new ChangePasswordPanel(userID, cardPanel, cardLayout), "ChangePassword");
                cardLayout.show(cardPanel, "ChangePassword");
            });

            teacherInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            teacherInfoPanel.add(changePasswordButton);
        } else {
            JLabel errorLabel = new JLabel("Không tìm thấy thông tin giáo viên!");
            errorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            teacherInfoPanel.add(errorLabel);
        }

        topPanel.add(teacherInfoPanel, BorderLayout.CENTER);

        // Panel chứa tiêu đề và ô nhập kỳ học
        JPanel semesterPanel = new JPanel();
        semesterPanel.setBackground(Color.WHITE);
        semesterPanel.setLayout(new BoxLayout(semesterPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Danh sách lớp học", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.WHITE);
        JLabel semesterLabel = new JLabel("Nhập kỳ học:");
        semesterLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        semesterField = new JTextField(5);
        semesterField.setFont(new Font("Arial", Font.PLAIN, 16));
        JButton showClassesButton = new JButton("Hiển thị lớp");
        showClassesButton.setFont(new Font("Arial", Font.BOLD, 16));
        showClassesButton.setBackground(new Color(70, 130, 180));
        showClassesButton.setForeground(Color.WHITE);
        showClassesButton.addActionListener(e -> populateClasses());

        inputPanel.add(semesterLabel);
        inputPanel.add(semesterField);
        inputPanel.add(showClassesButton);

        semesterPanel.add(titleLabel);
        semesterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        semesterPanel.add(inputPanel);

        topPanel.add(semesterPanel, BorderLayout.SOUTH);

        // Thêm topControlPanel và topPanel vào panel chính
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(Color.WHITE);
        northPanel.add(topControlPanel, BorderLayout.NORTH);
        northPanel.add(topPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // Panel chứa danh sách lớp học
        clazzPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        clazzPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(clazzPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Panel chứa nút đăng xuất và thông báo
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        // Nút đăng xuất (góc trái dưới cùng)
        JButton backButton = new JButton("Đăng xuất");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Login"));
        bottomPanel.add(backButton, BorderLayout.WEST);

        // Panel chứa thông báo
        JPanel notificationPanel = new JPanel();
        notificationPanel.setBackground(Color.WHITE);
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

        notificationLabel = new JLabel("");
        notificationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        notificationLabel.setForeground(new Color(0, 128, 0));
        notificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        notificationPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        notificationPanel.add(notificationLabel);

        bottomPanel.add(notificationPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void populateClasses() {
        clazzPanel.removeAll();
        String semesterInput = semesterField.getText().trim();
        if (semesterInput.isEmpty()) {
            showNotification("Vui lòng nhập kỳ học!");
            notificationLabel.setForeground(Color.RED);
            clazzPanel.revalidate();
            clazzPanel.repaint();
            return;
        }

        try {
            int semester = Integer.parseInt(semesterInput);
            Teacher teacher = userDAO.getTeacher(userID);
            if (teacher == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Clazz> clazzes = clazzDAO.getClazzesByTeacher(teacher);
            boolean hasClasses = false;
            for (Clazz clazz : clazzes) {
                if (clazz.getSemester() == semester) {
                    JButton clazzButton = new JButton(String.format("%d - %s - %s (%s - %s)", clazz.getClazzID(),
                        clazz.getCourse().getCourseID(), clazz.getCourse().getCourseName(), clazz.getRoom(), clazz.getDayOfWeek()));
                    clazzButton.setFont(new Font("Arial", Font.PLAIN, 16));
                    clazzButton.setBackground(new Color(70, 130, 180));
                    clazzButton.setForeground(Color.WHITE);
                    clazzButton.addActionListener(e -> {
                        cardPanel.add(new InputScorePanel(clazz, cardPanel, cardLayout), "InputScore");
                        cardLayout.show(cardPanel, "InputScore");
                    });
                    clazzPanel.add(clazzButton);
                    hasClasses = true;
                }
            }

            if (!hasClasses) {
                showNotification("Không tìm thấy lớp học nào cho kỳ " + semester + "!");
                notificationLabel.setForeground(Color.RED);
            } else {
                showNotification("Đã hiển thị danh sách lớp học cho kỳ " + semester + "!");
                notificationLabel.setForeground(new Color(0, 128, 0));
            }
        } catch (NumberFormatException e) {
            showNotification("Kỳ học phải là số nguyên!");
            notificationLabel.setForeground(Color.RED);
        }

        clazzPanel.revalidate();
        clazzPanel.repaint();
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        Timer timer = new Timer(3000, e -> notificationLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
}
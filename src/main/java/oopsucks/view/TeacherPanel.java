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
    private String username;
    private static final String ADMIN_ID = "T001"; // user_id của admin
    private JLabel notificationLabel;
    private JTextField semesterField;
    private JPanel clazzPanel;
    private JPanel buttonPanel; // Khai báo để truy cập trong các phương thức

    public TeacherPanel(String username, JPanel cardPanel, CardLayout cardLayout) {
        this.username = username;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.clazzDAO = new ClazzDAO();
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel titleLabel = new JLabel("Danh sách lớp học", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);

        // Ô nhập kỳ học và nút hiển thị
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel semesterPanel = new JPanel();
        semesterPanel.setBackground(Color.WHITE);
        JLabel semesterLabel = new JLabel("Nhập kỳ học:");
        semesterLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        semesterField = new JTextField(5);
        semesterField.setFont(new Font("Arial", Font.PLAIN, 16));
        JButton showClassesButton = new JButton("Hiển thị lớp");
        showClassesButton.setFont(new Font("Arial", Font.BOLD, 16));
        showClassesButton.setBackground(new Color(70, 130, 180));
        showClassesButton.setForeground(Color.WHITE);
        showClassesButton.addActionListener(e -> populateClasses());

        semesterPanel.add(semesterLabel);
        semesterPanel.add(semesterField);
        semesterPanel.add(showClassesButton);
        topPanel.add(semesterPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Panel chứa danh sách lớp học
        clazzPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        clazzPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(clazzPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Nút quay lại và nút mở/đóng đăng ký
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        // Kiểm tra xem giảng viên có phải là admin không
        Teacher teacher = userDAO.getTeacher(username);
        if (teacher == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isAuthorized = teacher.getUserID().equals(ADMIN_ID);
        if (isAuthorized) {
            JButton closeRegistrationButton = new JButton("Đóng đăng ký");
            closeRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            closeRegistrationButton.setBackground(new Color(34, 139, 34));
            closeRegistrationButton.setForeground(Color.WHITE);
            closeRegistrationButton.addActionListener(e -> {
                RegistrationManager.updateRegisterStatus(false, null); // Đóng đăng ký và xóa kỳ học
                showNotification("Đã đóng đăng ký lớp học!");
            });
            buttonPanel.add(closeRegistrationButton);

            JButton openRegistrationButton = new JButton("Mở đăng ký");
            openRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            openRegistrationButton.setBackground(new Color(34, 139, 34));
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

                // Tạo timer để xóa ô nhập sau 4 giây
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

        JButton backButton = new JButton("Đăng xuất");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Login"));
        buttonPanel.add(backButton);

        notificationLabel = new JLabel("");
        notificationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        notificationLabel.setForeground(new Color(0, 128, 0));
        notificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(buttonPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(notificationLabel);

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
            Teacher teacher = userDAO.getTeacher(username);
            if (teacher == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Clazz> clazzes = clazzDAO.getClazzesByTeacher(teacher);
            boolean hasClasses = false;
            for (Clazz clazz : clazzes) {
                if (clazz.getSemester() == semester) {
                    JButton clazzButton = new JButton(String.format("%s - %s (%s)",
                        clazz.getCourse().getCourseID(), clazz.getCourse().getCourseName(), clazz.getRoom()));
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
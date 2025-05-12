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
        add(titleLabel, BorderLayout.NORTH);

        // Lấy danh sách lớp học
        Teacher teacher = userDAO.getTeacher(username);
        if (teacher == null) {
            // Nếu không tìm thấy giáo viên, vẫn dùng JOptionPane
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Clazz> clazzes = clazzDAO.getClazzesByTeacher(teacher);
        JPanel clazzPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        clazzPanel.setBackground(Color.WHITE);

        // Hiển thị lớp học
        for (Clazz clazz : clazzes) {
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
        }

        JScrollPane scrollPane = new JScrollPane(clazzPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Nút quay lại và nút mở đăng ký 
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        // Kiểm tra xem giảng viên phải là admin không
        boolean isAuthorized = teacher.getUserID().equals(ADMIN_ID);
        if (isAuthorized) {
            JButton closeRegistrationButton = new JButton("Đóng đăng ký");
            closeRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            closeRegistrationButton.setBackground(new Color(34, 139, 34));
            closeRegistrationButton.setForeground(Color.WHITE);
            closeRegistrationButton.addActionListener(e -> {
                RegistrationManager.updateRegisterStatus(false);
                showNotification("Đã đóng đăng ký lớp học!");
            });
            buttonPanel.add(closeRegistrationButton);
            
            JButton openRegistrationButton = new JButton("Mở đăng ký");
            openRegistrationButton.setFont(new Font("Arial", Font.BOLD, 16));
            openRegistrationButton.setBackground(new Color(34, 139, 34));
            openRegistrationButton.setForeground(Color.WHITE);
            openRegistrationButton.addActionListener(e -> {
                RegistrationManager.updateRegisterStatus(true);
                showNotification("Đã mở đăng ký lớp học!");
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

    private void showNotification(String message) {
        notificationLabel.setText(message);

        Timer timer = new Timer(3000, e -> notificationLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
}

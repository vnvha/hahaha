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
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giáo viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Clazz> clazzes = clazzDAO.getClazzesByTeacher(teacher);
        JPanel clazzPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        clazzPanel.setBackground(Color.WHITE);

        // Hiển thị từng lớp học
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

        // Nút quay lại
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Login"));
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
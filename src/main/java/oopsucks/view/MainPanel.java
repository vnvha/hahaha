package oopsucks.view;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ClassListPanel classListPanel;
    private RegisteredPanel registeredPanel;

    public MainPanel() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Tạo 2 panel con và liên kết
        registeredPanel = new RegisteredPanel(this);
        classListPanel = new ClassListPanel(this, registeredPanel);

        cardPanel.add(classListPanel, "ClassList");
        cardPanel.add(registeredPanel, "RegisteredClasses");

        add(cardPanel, BorderLayout.CENTER);
    }

    public void showClassListPanel() {
        cardLayout.show(cardPanel, "ClassList");
    }

    public void showRegisteredPanel() {
        cardLayout.show(cardPanel, "RegisteredClasses");
        registeredPanel.loadRegisteredClasses();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Class Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new MainPanel());
            frame.setVisible(true);
        });
    }
}

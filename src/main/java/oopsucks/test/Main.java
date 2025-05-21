package oopsucks.test;

import oopsucks.view.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Class Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            CardLayout cardLayout = new CardLayout();
            JPanel cardPanel = new JPanel(cardLayout);

            LoginPanel loginPanel = new LoginPanel(cardPanel, cardLayout);
            cardPanel.add(loginPanel, "Login");
            frame.add(cardPanel);
            cardLayout.show(cardPanel, "Login");

            frame.setVisible(true);
        });
    }
}
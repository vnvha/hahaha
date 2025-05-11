package oopsucks.test;

import oopsucks.view.*;
import javax.swing.*;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Chạy giao diện trên thread của Swing
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
package oopsucks.test;

import oopsucks.view.*;
import javax.swing.*;

public class Main {
	 public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	        	MainPanel panel = new MainPanel();
	            panel.setVisible(true);
	        });
	    }
}
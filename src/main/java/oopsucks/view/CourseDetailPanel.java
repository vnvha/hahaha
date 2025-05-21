package oopsucks.view;

import oopsucks.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseDetailPanel extends JPanel {
    private final String courseId;
    private final CourseDAO courseDAO;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    public CourseDetailPanel(String courseId, CourseDAO courseDAO, JPanel cardPanel, CardLayout cardLayout) {
        this.courseId = courseId;
        this.courseDAO = courseDAO;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Chi Tiết Môn Học");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "trainingProgram"));
        headerPanel.add(backButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Panel for Course Details and Prerequisites
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Course Details
        Course course = courseDAO.getCourseById(courseId);
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        if (course != null) {
            addDetailRow(detailsPanel, gbc, "Mã HP:", course.getCourseID(), 0);
            addDetailRow(detailsPanel, gbc, "Tên học phần:", course.getCourseName(), 1);
            addDetailRow(detailsPanel, gbc, "Số tín chỉ:", String.valueOf(course.getCreditNumber()), 2);
            addDetailRow(detailsPanel, gbc, "Số tín chỉ học phí:", String.valueOf(course.getChargeableCredits()), 3);
            addDetailRow(detailsPanel, gbc, "Viện:", course.getInstitute(), 4);
            addDetailRow(detailsPanel, gbc, "Tỷ trọng thi cuối kỳ:", String.format("%.2f", course.getFinalExamWeight()), 5);
            addDetailRow(detailsPanel, gbc, "Loại:", course.getTypeID(), 6);
            addDetailRow(detailsPanel, gbc, "Bắt buộc:", course.isMandatory() ? "Có" : "Không", 7);
        } else {
            JLabel errorLabel = new JLabel("Không tìm thấy thông tin môn học");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            errorLabel.setForeground(new Color(200, 0, 0));
            detailsPanel.add(errorLabel, gbc);
        }

        centerPanel.add(detailsPanel, BorderLayout.NORTH);

        // Prerequisites Table
        String[] columnNames = {"Mã HP", "Tên học phần", "Số tín chỉ", "Viện"};
        DefaultTableModel prereqTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable prereqTable = new JTable(prereqTableModel);
        prereqTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prereqTable.setRowHeight(25);
        prereqTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        prereqTable.setAutoCreateRowSorter(true);

        if (course != null) {
            List<Course> prereqCourses = courseDAO.getPrerequisites(course);
            for (Course prereq : prereqCourses) {
                prereqTableModel.addRow(new Object[]{
                    prereq.getCourseID(),
                    prereq.getCourseName(),
                    prereq.getCreditNumber(),
                    prereq.getInstitute()
                });
            }
        }

        JScrollPane prereqScrollPane = new JScrollPane(prereqTable);
        prereqScrollPane.setBorder(BorderFactory.createTitledBorder("Các học phần điều kiện"));

        centerPanel.add(prereqScrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(valueComponent, gbc);
    }
}
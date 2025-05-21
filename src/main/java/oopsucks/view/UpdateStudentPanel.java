package oopsucks.view;

import oopsucks.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UpdateStudentPanel extends JPanel {
    private UserDAO userDAO;
    private TuitionFeeDAO tuitionFeeDAO;
    private AnnualTuitionFeeDAO annualTuitionFeeDAO; // Thêm AnnualTuitionFeeDAO
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String adminID;
    
    private JTextField studentIDField;
    private JButton searchButton;
    private JPanel studentInfoPanel;
    private JPanel tuitionFeePanel;
    
    private JTextField nameField;
    private JTextField dobField;
    private JTextField majorField;
    private JComboBox<Integer> semesterComboBox;
    private JCheckBox paidStatusCheckBox;
    
    private Student currentStudent;
    private Object currentTuitionFee; // Sử dụng Object để hỗ trợ cả TuitionFee và AnnualTuitionFee
    private JLabel notificationLabel;

    public UpdateStudentPanel(String adminID, JPanel cardPanel, CardLayout cardLayout) {
        this.adminID = adminID;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.userDAO = new UserDAO();
        this.tuitionFeeDAO = new TuitionFeeDAO();
        this.annualTuitionFeeDAO = new AnnualTuitionFeeDAO(); // Khởi tạo AnnualTuitionFeeDAO
        
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel tiêu đề
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Cập Nhật Thông Tin Sinh Viên", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Panel chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel tìm kiếm sinh viên
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Mã số sinh viên:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        studentIDField = new JTextField(15);
        studentIDField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        
        searchButton.addActionListener(e -> searchStudent());
        
        searchPanel.add(searchLabel);
        searchPanel.add(studentIDField);
        searchPanel.add(searchButton);
        
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Panel thông tin sinh viên (sẽ được điền sau khi tìm kiếm)
        studentInfoPanel = new JPanel();
        studentInfoPanel.setLayout(new BoxLayout(studentInfoPanel, BoxLayout.Y_AXIS));
        studentInfoPanel.setBackground(Color.WHITE);
        studentInfoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
            "Thông tin sinh viên",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16)
        ));
        studentInfoPanel.setVisible(false);
        
        // Panel học phí (sẽ được điền sau khi tìm kiếm)
        tuitionFeePanel = new JPanel();
        tuitionFeePanel.setLayout(new BoxLayout(tuitionFeePanel, BoxLayout.Y_AXIS));
        tuitionFeePanel.setBackground(Color.WHITE);
        tuitionFeePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
            "Thông tin học phí",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16)
        ));
        tuitionFeePanel.setVisible(false);
        
        mainPanel.add(studentInfoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(tuitionFeePanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút điều khiển
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        
        // Nút quay lại
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Teacher"));
        
        // Panel thông báo
        JPanel notificationPanel = new JPanel();
        notificationPanel.setBackground(Color.WHITE);
        notificationLabel = new JLabel("");
        notificationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        notificationPanel.add(notificationLabel);
        
        controlPanel.add(backButton, BorderLayout.WEST);
        controlPanel.add(notificationPanel, BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void searchStudent() {
        String studentID = studentIDField.getText().trim();
        if (studentID.isEmpty()) {
            showNotification("Vui lòng nhập mã số sinh viên!", Color.RED);
            return;
        }
        
        // Tìm sinh viên trong CSDL
        currentStudent = userDAO.getStudent(studentID);
        if (currentStudent == null) {
            showNotification("Không tìm thấy sinh viên với mã số " + studentID, Color.RED);
            studentInfoPanel.setVisible(false);
            tuitionFeePanel.setVisible(false);
            revalidate();
            repaint();
            return;
        }
        
        // Hiển thị thông tin sinh viên
        displayStudentInfo();
        
        // Hiển thị thông tin học phí dựa trên loại sinh viên
        if (currentStudent instanceof CreditBasedStudent) {
            List<TuitionFee> tuitionFees = tuitionFeeDAO.getTuitionFeesByStudent(studentID);
            displayTuitionFeeInfo(tuitionFees);
        } else if (currentStudent instanceof YearBasedStudent) {
            List<AnnualTuitionFee> tuitionFees = annualTuitionFeeDAO.getAnnualTuitionFeesByStudent(studentID);
            displayAnnualTuitionFeeInfo(tuitionFees);
        }
        
        showNotification("Đã tìm thấy thông tin sinh viên " + currentStudent.getUserName(), new Color(0, 128, 0));
    }
    
    private void displayStudentInfo() {
        studentInfoPanel.removeAll();
        
        // Tạo layout grid cho thông tin sinh viên
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        gridPanel.setBackground(Color.WHITE);
        
        // Thêm các trường thông tin
        JLabel nameLabel = new JLabel("Họ và tên:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameField = new JTextField(currentStudent.getUserName());
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel dobLabel = new JLabel("Ngày sinh:");
        dobLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dobField = new JTextField(currentStudent.getDob());
        dobField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel majorLabel = new JLabel("Ngành học:");
        majorLabel.setFont(new Font("Arial", Font.BOLD, 16));
        majorField = new JTextField(currentStudent.getMajor());
        majorField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        gridPanel.add(nameLabel);
        gridPanel.add(nameField);
        gridPanel.add(dobLabel);
        gridPanel.add(dobField);
        gridPanel.add(majorLabel);
        gridPanel.add(majorField);
        
        // Thêm nút cập nhật thông tin sinh viên
        JButton updateStudentButton = new JButton("Cập nhật thông tin sinh viên");
        updateStudentButton.setFont(new Font("Arial", Font.BOLD, 16));
        updateStudentButton.setBackground(new Color(70, 130, 180));
        updateStudentButton.setForeground(Color.WHITE);
        updateStudentButton.addActionListener(e -> updateStudentInfo());
        
        studentInfoPanel.add(gridPanel);
        studentInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel cho nút cập nhật
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(updateStudentButton);
        studentInfoPanel.add(buttonPanel);
        
        studentInfoPanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    private void displayTuitionFeeInfo(List<TuitionFee> tuitionFees) {
        tuitionFeePanel.removeAll();
        
        if (tuitionFees == null || tuitionFees.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có thông tin học phí cho sinh viên này");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            tuitionFeePanel.add(emptyLabel);
        } else {
            // Combobox chọn kỳ học
            JPanel semesterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            semesterPanel.setBackground(Color.WHITE);
            
            JLabel semesterLabel = new JLabel("Chọn kỳ học:");
            semesterLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            semesterComboBox = new JComboBox<>();
            semesterComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
            
            for (TuitionFee fee : tuitionFees) {
                semesterComboBox.addItem(fee.getSemester());
            }
            
            semesterComboBox.addActionListener(e -> {
                Integer selectedSemester = (Integer) semesterComboBox.getSelectedItem();
                if (selectedSemester != null) {
                    for (TuitionFee fee : tuitionFees) {
                        if (fee.getSemester().equals(selectedSemester)) {
                            currentTuitionFee = fee;
                            displayTuitionDetails(fee);
                            break;
                        }
                    }
                }
            });
            
            semesterPanel.add(semesterLabel);
            semesterPanel.add(semesterComboBox);
            tuitionFeePanel.add(semesterPanel);
            
            // Hiển thị thông tin chi tiết của kỳ học đầu tiên
            if (!tuitionFees.isEmpty()) {
                currentTuitionFee = tuitionFees.get(0);
                displayTuitionDetails((TuitionFee) currentTuitionFee);
            }
        }
        
        tuitionFeePanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    private void displayAnnualTuitionFeeInfo(List<AnnualTuitionFee> tuitionFees) {
        tuitionFeePanel.removeAll();
        
        if (tuitionFees == null || tuitionFees.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có thông tin học phí cho sinh viên này");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            tuitionFeePanel.add(emptyLabel);
        } else {
            // Combobox chọn kỳ học
            JPanel semesterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            semesterPanel.setBackground(Color.WHITE);
            
            JLabel semesterLabel = new JLabel("Chọn kỳ học:");
            semesterLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            semesterComboBox = new JComboBox<>();
            semesterComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
            
            for (AnnualTuitionFee fee : tuitionFees) {
                semesterComboBox.addItem(fee.getSemester());
            }
            
            semesterComboBox.addActionListener(e -> {
                Integer selectedSemester = (Integer) semesterComboBox.getSelectedItem();
                if (selectedSemester != null) {
                    for (AnnualTuitionFee fee : tuitionFees) {
                        if (fee.getSemester().equals(selectedSemester)) {
                            currentTuitionFee = fee;
                            displayAnnualTuitionDetails(fee);
                            break;
                        }
                    }
                }
            });
            
            semesterPanel.add(semesterLabel);
            semesterPanel.add(semesterComboBox);
            tuitionFeePanel.add(semesterPanel);
            
            // Hiển thị thông tin chi tiết của kỳ học đầu tiên
            if (!tuitionFees.isEmpty()) {
                currentTuitionFee = tuitionFees.get(0);
                displayAnnualTuitionDetails((AnnualTuitionFee) currentTuitionFee);
            }
        }
        
        tuitionFeePanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    private void displayTuitionDetails(TuitionFee fee) {
        // Xóa các phần tử chi tiết học phí hiện có (nếu có)
        Component[] components = tuitionFeePanel.getComponents();
        for (int i = 1; i < components.length; i++) {
            tuitionFeePanel.remove(components[i]);
        }
        
        // Panel thông tin chi tiết học phí
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        detailsPanel.setBackground(Color.WHITE);
        
        JLabel creditFeeLabel = new JLabel("Học phí mỗi tín chỉ:");
        creditFeeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel creditFeeValueLabel = new JLabel(String.format("%,.0f VNĐ", fee.getCreditFee()));
        creditFeeValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel totalCreditsLabel = new JLabel("Số tín chỉ học phí:");
        totalCreditsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel totalCreditsValueLabel = new JLabel(fee.getTotalChargeableCredits().toString());
        totalCreditsValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel tuitionLabel = new JLabel("Tổng học phí:");
        tuitionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel tuitionValueLabel = new JLabel(String.format("%,.0f VNĐ", fee.getTuition()));
        tuitionValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paidStatusCheckBox = new JCheckBox("Đã đóng học phí", fee.getStatus());
        paidStatusCheckBox.setFont(new Font("Arial", Font.PLAIN, 16));
        paidStatusCheckBox.setBackground(Color.WHITE);
        
        detailsPanel.add(creditFeeLabel);
        detailsPanel.add(creditFeeValueLabel);
        detailsPanel.add(totalCreditsLabel);
        detailsPanel.add(totalCreditsValueLabel);
        detailsPanel.add(tuitionLabel);
        detailsPanel.add(tuitionValueLabel);
        detailsPanel.add(statusLabel);
        detailsPanel.add(paidStatusCheckBox);
        
        // Thêm nút cập nhật học phí
        JButton updateTuitionButton = new JButton("Cập nhật trạng thái học phí");
        updateTuitionButton.setFont(new Font("Arial", Font.BOLD, 16));
        updateTuitionButton.setBackground(new Color(70, 130, 180));
        updateTuitionButton.setForeground(Color.WHITE);
        updateTuitionButton.addActionListener(e -> updateTuitionStatus());
        
        // Panel cho nút cập nhật
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(updateTuitionButton);
        
        tuitionFeePanel.add(detailsPanel);
        tuitionFeePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        tuitionFeePanel.add(buttonPanel);
        
        tuitionFeePanel.revalidate();
        tuitionFeePanel.repaint();
    }
    
    private void displayAnnualTuitionDetails(AnnualTuitionFee fee) {
        // Xóa các phần tử chi tiết học phí hiện có (nếu có)
        Component[] components = tuitionFeePanel.getComponents();
        for (int i = 1; i < components.length; i++) {
            tuitionFeePanel.remove(components[i]);
        }
        
        // Panel thông tin chi tiết học phí
        JPanel detailsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        detailsPanel.setBackground(Color.WHITE);
        
        JLabel tuitionLabel = new JLabel("Học phí kỳ:");
        tuitionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel tuitionValueLabel = new JLabel(String.format("%,.0f VNĐ", fee.getSemesterFee()));
        tuitionValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paidStatusCheckBox = new JCheckBox("Đã đóng học phí", fee.getStatus());
        paidStatusCheckBox.setFont(new Font("Arial", Font.PLAIN, 16));
        paidStatusCheckBox.setBackground(Color.WHITE);
        
        detailsPanel.add(tuitionLabel);
        detailsPanel.add(tuitionValueLabel);
        detailsPanel.add(statusLabel);
        detailsPanel.add(paidStatusCheckBox);
        
        // Thêm nút cập nhật học phí
        JButton updateTuitionButton = new JButton("Cập nhật trạng thái học phí");
        updateTuitionButton.setFont(new Font("Arial", Font.BOLD, 16));
        updateTuitionButton.setBackground(new Color(70, 130, 180));
        updateTuitionButton.setForeground(Color.WHITE);
        updateTuitionButton.addActionListener(e -> updateTuitionStatus());
        
        // Panel cho nút cập nhật
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(updateTuitionButton);
        
        tuitionFeePanel.add(detailsPanel);
        tuitionFeePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        tuitionFeePanel.add(buttonPanel);
        
        tuitionFeePanel.revalidate();
        tuitionFeePanel.repaint();
    }
    
    private void updateStudentInfo() {
        if (currentStudent == null) {
            showNotification("Không có thông tin sinh viên để cập nhật!", Color.RED);
            return;
        }
        
        // Cập nhật thông tin sinh viên
        currentStudent.setUserName(nameField.getText().trim());
        currentStudent.setDob(dobField.getText().trim());
        currentStudent.setMajor(majorField.getText().trim());
        
        // Lưu vào CSDL
        try {
            userDAO.updateStudent(currentStudent);
            showNotification("Đã cập nhật thông tin sinh viên thành công!", new Color(0, 128, 0));
        } catch (Exception e) {
            showNotification("Lỗi khi cập nhật thông tin: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }
    
    private void updateTuitionStatus() {
        if (currentTuitionFee == null) {
            showNotification("Không có thông tin học phí để cập nhật!", Color.RED);
            return;
        }
        
        // Cập nhật trạng thái đóng học phí
        boolean isChecked = paidStatusCheckBox.isSelected();
        
        // Lưu vào CSDL
        try {
            boolean success;
            if (currentTuitionFee instanceof TuitionFee) {
                TuitionFee tuitionFee = (TuitionFee) currentTuitionFee;
                success = tuitionFeeDAO.updatePaymentStatus(tuitionFee.getId(), isChecked);
                if (success) {
                    tuitionFee.setStatus(isChecked);
                    showNotification("Đã cập nhật trạng thái học phí thành công!", new Color(0, 128, 0));
                } else {
                    showNotification("Không thể cập nhật trạng thái học phí!", Color.RED);
                }
            } else if (currentTuitionFee instanceof AnnualTuitionFee) {
                AnnualTuitionFee annualTuitionFee = (AnnualTuitionFee) currentTuitionFee;
                success = annualTuitionFeeDAO.updatePaymentStatus(annualTuitionFee.getId(), isChecked);
                if (success) {
                    annualTuitionFee.setStatus(isChecked);
                    showNotification("Đã cập nhật trạng thái học phí thành công!", new Color(0, 128, 0));
                } else {
                    showNotification("Không thể cập nhật trạng thái học phí!", Color.RED);
                }
            } else {
                showNotification("Loại học phí không hợp lệ!", Color.RED);
            }
        } catch (Exception e) {
            showNotification("Lỗi khi cập nhật trạng thái học phí: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }
    
    private void showNotification(String message, Color color) {
        notificationLabel.setText(message);
        notificationLabel.setForeground(color);
        
        // Timer để xóa thông báo sau 3 giây
        Timer timer = new Timer(3000, e -> notificationLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
}
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RecruiterPortal extends JFrame {
    private JTable appTable;
    private DefaultTableModel model;
    
    private JTextField txtTitle = new JTextField(10);
    private JTextField txtReqExp = new JTextField(3);
    private JTextField txtReqSkill = new JTextField(12);

    public RecruiterPortal() {
        setTitle("Recruiter Dashboard");
        setSize(750, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevents straight closing without logout confirmation
        setLayout(new BorderLayout());

        // Header Panel with Title and Logout button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(new JLabel("Logged in as Recruiter"), BorderLayout.WEST);
        JButton btnLogout = new JButton("Log Out");
        headerPanel.add(btnLogout, BorderLayout.EAST);

        // Job Posting form panel
        JPanel addJobPanel = new JPanel(new FlowLayout());
        addJobPanel.setBorder(BorderFactory.createTitledBorder("Post a New Job"));
        addJobPanel.add(new JLabel("Title:")); addJobPanel.add(txtTitle);
        addJobPanel.add(new JLabel("Req Exp (Yrs):")); addJobPanel.add(txtReqExp);
        addJobPanel.add(new JLabel("Req Skills (e.g. Java, SQL):")); addJobPanel.add(txtReqSkill);
        JButton btnAddJob = new JButton("Post Job");
        addJobPanel.add(btnAddJob);

        // Combine Header and Job form into a single Top Area
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(addJobPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Read-only Applications Table
        model = new DefaultTableModel(new String[]{"App ID", "Job Title", "Applicant", "Experience", "Skills", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appTable = new JTable(model);
        loadApplications();
        add(new JScrollPane(appTable), BorderLayout.CENTER);

        // Bottom Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton btnAccept = new JButton("Accept Applicant");
        JButton btnReject = new JButton("Reject Applicant");
        actionPanel.add(btnAccept); actionPanel.add(btnReject);
        add(actionPanel, BorderLayout.SOUTH);

        // Event Listeners
        btnAddJob.addActionListener(e -> postNewJob());
        btnAccept.addActionListener(e -> updateStatus("Accepted"));
        btnReject.addActionListener(e -> updateStatus("Rejected"));
        
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginWindow();
                this.dispose();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadApplications() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT a.app_id, j.title, a.candidate_username, c.experience, c.skills, a.status " +
                           "FROM applications a " +
                           "JOIN jobs j ON a.job_id = j.job_id " +
                           "JOIN candidate_users c ON a.candidate_username = c.username";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("app_id"),
                    rs.getString("title"),
                    rs.getString("candidate_username"),
                    rs.getInt("experience") + " Years",
                    rs.getString("skills"),
                    rs.getString("status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void postNewJob() {
        String title = txtTitle.getText().trim();
        String expStr = txtReqExp.getText().trim();
        String skill = txtReqSkill.getText().trim();

        if (title.isEmpty() || expStr.isEmpty() || skill.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all job fields!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO jobs (title, req_experience, req_skill) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, title);
            ps.setInt(2, Integer.parseInt(expStr));
            ps.setString(3, skill);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Job Posted Successfully!");
            txtTitle.setText(""); txtReqExp.setText(""); txtReqSkill.setText("");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateStatus(String newStatus) {
        int selectedRow = appTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an applicant from the table first!");
            return;
        }

        int appId = (int) model.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE applications SET status = ? WHERE app_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, newStatus);
            ps.setInt(2, appId);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Applicant has been " + newStatus + "!");
            loadApplications();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CandidatePortal extends JFrame {
    private String username;
    private JTable jobTable;
    private DefaultTableModel model;
    private int currentExp;
    private String currentSkills;

    public CandidatePortal(String username, int exp, String skills) {
        this.username = username;
        this.currentExp = exp;
        this.currentSkills = skills;
        
        setTitle("Candidate Portal - Welcome " + username);
        setSize(650, 450);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevents closing directly without logging out
        setLayout(new BorderLayout());

        // Top Panel for Header & Logout Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Your Profile Skills: " + skills), BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Log Out");
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Read-only Table
        model = new DefaultTableModel(new String[]{"Job ID", "Title", "Req. Exp", "Req. Skill", "Application Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        jobTable = new JTable(model);
        loadMatchingJobs();
        add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JButton btnApply = new JButton("Apply to Selected Job");
        add(btnApply, BorderLayout.SOUTH);

        // Action Listeners
        btnApply.addActionListener(e -> applyToJob());
        
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

    private void loadMatchingJobs() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            // This query splits candidate's skills by comma and checks if any match the job's required skill
            String query = "SELECT j.job_id, j.title, j.req_experience, j.req_skill, a.status " +
                           "FROM jobs j " +
                           "LEFT JOIN applications a ON j.job_id = a.job_id AND a.candidate_username = ? " +
                           "WHERE j.req_experience <= ? AND ? REGEXP REPLACE(j.req_skill, ', *', '|')";
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setInt(2, currentExp);
            ps.setString(3, currentSkills);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                if (status == null) status = "Not Applied";
                
                model.addRow(new Object[]{
                    rs.getInt("job_id"), 
                    rs.getString("title"), 
                    rs.getInt("req_experience"), 
                    rs.getString("req_skill"),
                    status
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void applyToJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job first!");
            return;
        }

        String currentStatus = (String) model.getValueAt(selectedRow, 4);
        if (!currentStatus.equals("Not Applied")) {
            JOptionPane.showMessageDialog(this, "You have already applied to this job!");
            return;
        }

        int jobId = (int) model.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO applications (job_id, candidate_username, status) VALUES (?, ?, 'Pending')";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, jobId);
            ps.setString(2, username);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Application submitted successfully!");
            
            loadMatchingJobs(); // Refresh the table layout instantly
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
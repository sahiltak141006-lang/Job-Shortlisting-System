import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.*;

public class SignupWindow extends JFrame {
    private JTextField txtUser = new JTextField(15);
    private JTextField txtPass = new JTextField(15);
    private JCheckBox chkRecruiter = new JCheckBox("Register as Recruiter");
    
    private JTextField txtExp = new JTextField(5);
    private JTextField txtSkills = new JTextField(15);
    private JTextArea txtInfo = new JTextArea(3, 15);
    
    private JPanel candidatePanel;

    public SignupWindow(String passedUser, String passedPass, boolean passedIsRecruiter) {
        setTitle("Job Portal - Create Account");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Using a cleaner vertical layout so components don't stretch in one long line
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtUser.setText(passedUser);
        txtPass.setText(passedPass);
        chkRecruiter.setSelected(passedIsRecruiter);

        mainPanel.add(new JLabel("Username:")); mainPanel.add(txtUser);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(new JLabel("Password:")); mainPanel.add(txtPass);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(chkRecruiter);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Candidate Fields Panel
        candidatePanel = new JPanel();
        candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
        candidatePanel.add(new JLabel("Experience (Years):")); candidatePanel.add(txtExp);
        candidatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        candidatePanel.add(new JLabel("Skills (e.g. Java, SQL):")); candidatePanel.add(txtSkills);
        candidatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        candidatePanel.add(new JLabel("Relevant Information:")); candidatePanel.add(new JScrollPane(txtInfo));
        
        mainPanel.add(candidatePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSubmit = new JButton("Register");
        JButton btnBack = new JButton("Back to Login");
        buttonPanel.add(btnSubmit); buttonPanel.add(btnBack);
        mainPanel.add(buttonPanel);

        add(mainPanel);

        candidatePanel.setVisible(!passedIsRecruiter);
        chkRecruiter.addItemListener(e -> {
            candidatePanel.setVisible(!chkRecruiter.isSelected());
            revalidate();
            repaint();
        });

        btnSubmit.addActionListener(e -> handleRegister());
        btnBack.addActionListener(e -> {
            new LoginWindow();
            this.dispose();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleRegister() {
        String user = txtUser.getText().trim();
        String pass = txtPass.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be blank!");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed! Is XAMPP MySQL running?");
                return;
            }

            if (chkRecruiter.isSelected()) {
                String query = "INSERT INTO recruiter_users VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, user); ps.setString(2, pass);
                ps.executeUpdate();
            } else {
                String query = "INSERT INTO candidate_users VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, user); ps.setString(2, pass);
                ps.setInt(3, Integer.parseInt(txtExp.getText().trim()));
                ps.setString(4, txtSkills.getText().trim());
                ps.setString(5, txtInfo.getText());
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Registration Successful! Returning to Login.");
            new LoginWindow();
            this.dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for experience.");
        } catch (Exception ex) { 
            // CRITICAL CHANGE: This will show you the ACTUAL database error message now!
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            ex.printStackTrace(); 
        }
    }
}
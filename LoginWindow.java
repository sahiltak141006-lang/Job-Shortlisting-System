import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class LoginWindow extends JFrame {
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);
    private JCheckBox chkRecruiter = new JCheckBox("I am a Recruiter");

    public LoginWindow() {
        setTitle("Job Portal - Login");
        setSize(300, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 15));

        add(new JLabel("Username:")); add(txtUser);
        add(new JLabel("Password:")); add(txtPass);
        add(chkRecruiter);

        JButton btnLogin = new JButton("Login");
        JButton btnSignup = new JButton("Sign Up");
        add(btnLogin); add(btnSignup);

        btnLogin.addActionListener(e -> handleLogin());
        
        // Signup button action logic
        btnSignup.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            boolean isRecruiter = chkRecruiter.isSelected();
            
            // Open signup window, carrying over whatever they typed
            new SignupWindow(user, pass, isRecruiter);
            this.dispose(); // Close login window
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();
        boolean isRecruiter = chkRecruiter.isSelected();
        String table = isRecruiter ? "recruiter_users" : "candidate_users";

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM " + table + " WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                this.dispose();
                if (isRecruiter) {
                    new RecruiterPortal();
                } else {
                    new CandidatePortal(user, rs.getInt("experience"), rs.getString("skills"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Wrong Credentials! User does not exist or password matches incorrectly.");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        new LoginWindow();
    }
}
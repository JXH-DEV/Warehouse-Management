package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.model.User;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("Menaxhimi i Magazines - Hyrje");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeroPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.BG_DARK, getWidth(), getHeight(), new Color(10, 25, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(64, 156, 255, 22));
                g2.fillOval(-80, -80, 280, 280);
                g2.setColor(new Color(0, 212, 200, 14));
                g2.fillOval(getWidth() - 180, getHeight() - 160, 220, 220);
            }
        };
        hero.setPreferredSize(new Dimension(320, 0));
        hero.setOpaque(true);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        JComponent monogram = UITheme.monogramBadge("MM", 64);
        monogram.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(monogram);
        stack.add(Box.createVerticalStrut(20));

        JLabel appName = new JLabel("Menaxhimi i Magazines", SwingConstants.CENTER);
        appName.setFont(UITheme.FONT_TITLE);
        appName.setForeground(UITheme.TEXT_PRIMARY);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(appName);
        stack.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Sistemi i Menaxhimit të Magazinës", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(subtitle);
        stack.add(Box.createVerticalStrut(24));

        JLabel ubt = new JLabel("Studentët UBT · 2026", SwingConstants.CENTER);
        ubt.setFont(UITheme.FONT_SMALL);
        ubt.setForeground(new Color(80, 100, 140));
        ubt.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(ubt);

        hero.add(stack);
        return hero;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG_PANEL);

        JPanel card = UITheme.loginFormCard(12);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        card.setPreferredSize(new Dimension(340, 380));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel heading = new JLabel("Hyrje");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(heading);
        form.add(Box.createVerticalStrut(4));

        JLabel subtext = UITheme.muted("Vendosni kredencialet tuaja");
        subtext.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtext);
        form.add(Box.createVerticalStrut(24));

        JLabel userLbl = UITheme.muted("Emri i Përdoruesit");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLbl);
        form.add(Box.createVerticalStrut(6));

        usernameField = UITheme.styledField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.wireFieldFocus(usernameField);
        form.add(usernameField);
        form.add(Box.createVerticalStrut(16));

        JLabel passLbl = UITheme.muted("Fjalëkalimi");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));

        passwordField = UITheme.styledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.wireFieldFocus(passwordField);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.ACCENT_RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setMinimumSize(new Dimension(0, 18));
        errorLabel.setPreferredSize(new Dimension(0, 18));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));

        JButton loginBtn = UITheme.loginPrimaryButton("Hyr");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());
        form.add(loginBtn);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());

        DocumentListener clearError = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { clearLoginError(); }
            @Override public void removeUpdate(DocumentEvent e) { clearLoginError(); }
            @Override public void changedUpdate(DocumentEvent e) { clearLoginError(); }
        };
        usernameField.getDocument().addDocumentListener(clearError);
        passwordField.getDocument().addDocumentListener(clearError);

        card.add(form, BorderLayout.CENTER);
        outer.add(card);
        return outer;
    }

    private void clearLoginError() {
        errorLabel.setText(" ");
        passwordField.setBorder(UITheme.defaultFieldBorder());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Ju lutem plotësoni të gjitha fushat.");
            return;
        }

        Optional<User> user = DataStore.getInstance().findUserByCredentials(username, password);
        if (user.isPresent()) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(user.get()).setVisible(true));
        } else {
            errorLabel.setText("Emri i përdoruesit ose fjalëkalimi janë gabim.");
            passwordField.setText("");
            passwordField.setBorder(UITheme.errorFieldBorder());
        }
    }
}

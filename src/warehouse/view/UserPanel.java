package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.model.User;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UserPanel extends JPanel {

    private final User currentUser;
    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(UITheme.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(UITheme.BG_DARK);
        JButton addBtn = UITheme.successButton("Përdorues i Ri");
        addBtn.addActionListener(e -> showUserDialog(null));
        toolbar.add(addBtn);

        String[] cols = {"ID", "Username", "Emri i Plotë", "Roli"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(UITheme.BG_DARK);
        JButton editBtn   = UITheme.warningButton("Ndrysho");
        JButton deleteBtn = UITheme.dangerButton("Fshi");
        JButton passBtn   = UITheme.ghostButton("Ndrysho Fjalëkalimin");
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        passBtn.addActionListener(e -> changePassword());
        actions.add(editBtn); actions.add(deleteBtn); actions.add(passBtn);

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Menaxhimi i Përdoruesve"), BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (User u : ds.getAllUsers()) {
            tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole().name()});
        }
    }

    private User getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Zgjidhni një përdorues.", "Info", JOptionPane.INFORMATION_MESSAGE); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return ds.findUserById(id).orElse(null);
    }

    private void editSelected() {
        User u = getSelected();
        if (u != null) showUserDialog(u);
    }

    private void deleteSelected() {
        User u = getSelected();
        if (u == null) return;
        if (u.getId() == currentUser.getId()) { JOptionPane.showMessageDialog(this, "Nuk mund të fshini veten!", "Gabim", JOptionPane.ERROR_MESSAGE); return; }
        int c = JOptionPane.showConfirmDialog(this, "Fshini përdoruesin \"" + u.getUsername() + "\"?", "Konfirmo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            ds.removeUser(u.getId());
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        User u = getSelected();
        if (u == null) return;
        JPasswordField pf = new JPasswordField(16);
        pf.setBackground(UITheme.BG_INPUT);
        pf.setForeground(UITheme.TEXT_PRIMARY);
        int r = JOptionPane.showConfirmDialog(this, new Object[]{"Fjalëkalimi i ri për " + u.getUsername() + ":", pf},
            "Ndrysho Fjalëkalimin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            String np = new String(pf.getPassword()).trim();
            if (np.length() < 4) { JOptionPane.showMessageDialog(this, "Fjalëkalimi duhet të ketë të paktën 4 karaktere.", "Gabim", JOptionPane.ERROR_MESSAGE); return; }
            u.setPassword(np);
            ds.updateUser(u);
            JOptionPane.showMessageDialog(this, "Fjalëkalimi u ndryshua me sukses.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showUserDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Ndrysho Përdoruesin" : "Përdorues i Ri", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userF = UITheme.styledField(18);
        JTextField nameF = UITheme.styledField(18);
        JPasswordField passF = new JPasswordField(18);
        passF.setBackground(UITheme.BG_INPUT); passF.setForeground(UITheme.TEXT_PRIMARY);
        passF.setFont(UITheme.FONT_BODY);
        passF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR,1), BorderFactory.createEmptyBorder(6,10,6,10)));
        JComboBox<User.Role> roleBox = new JComboBox<>(User.Role.values());
        roleBox.setBackground(UITheme.BG_INPUT); roleBox.setForeground(UITheme.TEXT_PRIMARY);

        if (isEdit) {
            userF.setText(existing.getUsername());
            nameF.setText(existing.getFullName());
            roleBox.setSelectedItem(existing.getRole());
            userF.setEnabled(false);
        }

        String[] lbs = {"Username:", "Emri i Plotë:", isEdit ? "(Mos ndrysho fjalëkalimin)" : "Fjalëkalimi:", "Roli:"};
        Component[] flds = {userF, nameF, passF, roleBox};
        for (int i = 0; i < lbs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel l = new JLabel(lbs[i]); l.setForeground(UITheme.TEXT_MUTED); l.setFont(UITheme.FONT_BODY);
            form.add(l, gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            flds[i].setPreferredSize(new Dimension(190, 32));
            form.add(flds[i], gbc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.BG_CARD);
        JButton cancel = UITheme.ghostButton("Anulo");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = UITheme.successButton(isEdit ? "Ruaj" : "Shto");
        save.addActionListener(e -> {
            try {
                String name = nameF.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Emri është i detyrueshëm.");
                User.Role role = (User.Role) roleBox.getSelectedItem();
                if (isEdit) {
                    if (existing.getRole() == User.Role.ADMIN && role != User.Role.ADMIN && ds.countAdmins() <= 1) {
                        throw new IllegalArgumentException("Nuk mund të hiqet roli i adminit të fundit.");
                    }
                    existing.setFullName(name);
                    existing.setRole(role);
                    String np = new String(passF.getPassword()).trim();
                    if (!np.isEmpty()) existing.setPassword(np);
                    ds.updateUser(existing);
                } else {
                    String uname = userF.getText().trim();
                    String pass  = new String(passF.getPassword()).trim();
                    if (uname.isEmpty() || pass.isEmpty()) throw new IllegalArgumentException("Username dhe fjalëkalimi janë të detyrueshëm.");
                    boolean exists = ds.getAllUsers().stream().anyMatch(u -> u.getUsername().equals(uname));
                    if (exists) throw new IllegalArgumentException("Ky username ekziston tashmë.");
                    ds.addUser(new User(ds.nextUserId(), uname, pass, name, role));
                }
                refresh(); dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx = 0; gbc.gridy = lbs.length; gbc.gridwidth = 2; gbc.insets = new Insets(14, 6, 0, 6);
        form.add(btns, gbc);
        dialog.add(form);
        dialog.setVisible(true);
    }
}

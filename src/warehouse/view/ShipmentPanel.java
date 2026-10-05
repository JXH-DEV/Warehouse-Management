package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.model.*;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ShipmentPanel extends JPanel {

    private final User currentUser;
    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;

    public ShipmentPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(UITheme.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UITheme.BG_DARK);
        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setBackground(UITheme.BG_DARK);
        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton addBtn = UITheme.primaryButton("Dërgesë e Re");
            addBtn.addActionListener(e -> showShipmentDialog());
            rightTools.add(addBtn);
        }
        toolbar.add(rightTools, BorderLayout.EAST);

        // Table
        String[] cols = {"Nr. Gjurmimi", "Porosia", "Data Dërgimit", "Arr. Planifikuar", "Transportuesi", "Destinacioni", "Pesha (kg)", "Statusi"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(UITheme.BG_DARK);
        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton statusBtn = UITheme.warningButton("Ndrysho Statusin");
            statusBtn.addActionListener(e -> changeStatus());
            JButton deleteBtn = UITheme.dangerButton("Fshi");
            deleteBtn.addActionListener(e -> deleteSelected());
            actions.add(statusBtn);
            actions.add(deleteBtn);
        }

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Lista e Dërgesave"), BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (Shipment s : ds.getAllShipments()) {
            tableModel.addRow(new Object[]{
                s.getTrackingNumber(),
                s.getOrder().getOrderNumber(),
                s.getShipDate().toString(),
                s.getEstimatedArrival() != null ? s.getEstimatedArrival().toString() : "—",
                s.getCarrier(),
                s.getDestination(),
                s.getWeight(),
                s.getStatus().name()
            });
        }
    }

    private Shipment getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Zgjidhni një dërgesë.", "Info", JOptionPane.INFORMATION_MESSAGE); return null; }
        String trk = (String) tableModel.getValueAt(row, 0);
        return ds.getAllShipments().stream().filter(s -> s.getTrackingNumber().equals(trk)).findFirst().orElse(null);
    }

    private void changeStatus() {
        Shipment s = getSelected();
        if (s == null) return;
        Shipment.Status newStatus = (Shipment.Status) JOptionPane.showInputDialog(this,
            "Statusi aktual: " + s.getStatus() + "\nZgjidhni statusin e ri:",
            "Ndrysho Statusin", JOptionPane.QUESTION_MESSAGE, null, Shipment.Status.values(), s.getStatus());
        if (newStatus != null) { s.setStatus(newStatus); ds.updateShipment(s); refresh(); }
    }

    private void deleteSelected() {
        Shipment s = getSelected();
        if (s == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Fshini dërgesën " + s.getTrackingNumber() + "?",
            "Konfirmo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { ds.removeShipment(s.getId()); refresh(); }
    }

    private void showShipmentDialog() {
        List<Order> orders = ds.getAllOrders();
        if (orders.isEmpty()) { JOptionPane.showMessageDialog(this, "Nuk ka porosi të disponueshme.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Dërgesë e Re", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Order> orderBox = UITheme.styledComboBox(orders.toArray(new Order[0]));

        JTextField carrierF = UITheme.styledField(18);
        JTextField destF    = UITheme.styledField(18);
        JTextField weightF  = UITheme.styledField(8);
        JTextField arrF     = UITheme.styledField(12);
        JTextField notesF   = UITheme.styledField(18);
        arrF.setText(LocalDate.now().plusDays(5).toString());

        String[] lbs = {"Porosia:", "Transportuesi:", "Destinacioni:", "Pesha (kg):", "Arr. Planifikuar:", "Shënime:"};
        Component[] flds = {orderBox, carrierF, destF, weightF, arrF, notesF};
        for (int i = 0; i < lbs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel l = new JLabel(lbs[i]); l.setForeground(UITheme.TEXT_MUTED); l.setFont(UITheme.FONT_BODY);
            form.add(l, gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            flds[i].setPreferredSize(new Dimension(200, 32));
            form.add(flds[i], gbc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.BG_CARD);
        JButton cancel = UITheme.ghostButton("Anulo");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = UITheme.primaryButton("Krijo Dërgesën");
        save.addActionListener(e -> {
            try {
                Order order = (Order) orderBox.getSelectedItem();
                String carrier = carrierF.getText().trim();
                String dest    = destF.getText().trim();
                if (carrier.isEmpty() || dest.isEmpty()) throw new IllegalArgumentException("Transportuesi dhe destinacioni janë të detyrueshëm.");
                double weight = Double.parseDouble(weightF.getText().trim());
                LocalDate arr = LocalDate.parse(arrF.getText().trim());
                Shipment s = new Shipment(ds.nextShipmentId(), ds.generateTrackingNumber(),
                    order, LocalDate.now(), arr, Shipment.Status.PREPARING, carrier, dest, weight, notesF.getText().trim());
                ds.addShipment(s);
                refresh();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Dërgesa u krijua!\nNr. Gjurmimi: " + s.getTrackingNumber(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Gabim: " + ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx = 0; gbc.gridy = lbs.length; gbc.gridwidth = 2; gbc.insets = new Insets(16, 6, 0, 6);
        form.add(btns, gbc);
        dialog.add(form);
        dialog.setVisible(true);
    }
}

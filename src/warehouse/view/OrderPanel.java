package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.model.*;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {

    private final User currentUser;
    private final DataStore ds = DataStore.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> filterBox;
    private List<Order> cachedOrders = List.of();

    public OrderPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(UITheme.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(UITheme.BG_DARK);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setBackground(UITheme.BG_DARK);
        filterBox = new JComboBox<>(new String[]{"Të Gjitha", "Hyrëse (Blerje)", "Dalëse (Shitje)"});
        filterBox.setBackground(UITheme.BG_INPUT);
        filterBox.setForeground(UITheme.TEXT_PRIMARY);
        filterBox.setFont(UITheme.FONT_BODY);
        filterBox.addActionListener(e -> refresh());
        JLabel filterLbl = new JLabel("Filter: ");
        filterLbl.setForeground(UITheme.TEXT_MUTED);
        filterLbl.setFont(UITheme.FONT_BODY);
        leftTools.add(filterLbl);
        leftTools.add(filterBox);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setBackground(UITheme.BG_DARK);
        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton addInBtn = UITheme.successButton("Porosi Hyrëse");
            addInBtn.addActionListener(e -> showOrderDialog(Order.Type.INCOMING));
            JButton addOutBtn = UITheme.primaryButton("Porosi Dalëse");
            addOutBtn.addActionListener(e -> showOrderDialog(Order.Type.OUTGOING));
            rightTools.add(addInBtn);
            rightTools.add(addOutBtn);
        }
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        String[] cols = {"Nr. Porosi", "Lloji", "Data", "Dorëzimi", "Klienti/Furnitori", "Statusi", "Artikuj", "Shuma (L)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(UITheme.BG_DARK);

        JButton detailBtn = UITheme.ghostButton("Detajet");
        detailBtn.addActionListener(e -> showDetails());
        actions.add(detailBtn);

        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton itemsBtn = UITheme.ghostButton("Menaxho Artikujt");
            itemsBtn.addActionListener(e -> manageItems());
            JButton statusBtn = UITheme.warningButton("Ndrysho Statusin");
            statusBtn.addActionListener(e -> changeStatus());
            JButton deleteBtn = UITheme.dangerButton("Fshi");
            deleteBtn.addActionListener(e -> deleteSelected());
            actions.add(itemsBtn);
            actions.add(statusBtn);
            actions.add(deleteBtn);
        }

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UITheme.sectionLabel("Lista e Porosive"), BorderLayout.NORTH);
        tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        tableCard.add(actions, BorderLayout.SOUTH);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Order> orders;
        int sel = filterBox.getSelectedIndex();
        if (sel == 1) orders = ds.getOrdersByType(Order.Type.INCOMING);
        else if (sel == 2) orders = ds.getOrdersByType(Order.Type.OUTGOING);
        else orders = ds.getAllOrders();
        cachedOrders = orders;

        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                o.getOrderNumber(),
                o.getType() == Order.Type.INCOMING ? "Hyrëse" : "Dalëse",
                o.getOrderDate().toString(),
                o.getDeliveryDate() != null ? o.getDeliveryDate().toString() : "—",
                o.getSupplierOrClient(),
                o.getStatus().name(),
                o.getItems().size(),
                String.format("%.0f L", o.getTotalAmount())
            });
        }
    }

    private Order getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Zgjidhni një porosi.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        String num = (String) tableModel.getValueAt(row, 0);
        return cachedOrders.stream().filter(o -> o.getOrderNumber().equals(num)).findFirst().orElse(null);
    }

    private void showDetails() {
        Order o = getSelected();
        if (o == null) return;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detajet e Porosisë " + o.getOrderNumber(), true);
        d.setSize(600, 420);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel info = new JPanel(new GridLayout(5, 2, 8, 6));
        info.setBackground(UITheme.BG_CARD);
        String[][] rows = {
            {"Nr. Porosi:", o.getOrderNumber()},
            {"Lloji:", o.getType() == Order.Type.INCOMING ? "Hyrëse (Blerje)" : "Dalëse (Shitje)"},
            {"Klienti/Furnitori:", o.getSupplierOrClient()},
            {"Data e Porosisë:", o.getOrderDate().toString()},
            {"Statusi:", o.getStatus().name()}
        };
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0]); k.setForeground(UITheme.TEXT_MUTED); k.setFont(UITheme.FONT_BODY);
            JLabel v = new JLabel(r[1]); v.setForeground(UITheme.TEXT_PRIMARY); v.setFont(new Font("Segoe UI", Font.BOLD, 13));
            info.add(k); info.add(v);
        }
        panel.add(info, BorderLayout.NORTH);

        String[] cols = {"Produkti", "Sasia", "Çmimi Unit.", "Nëntotali"};
        DefaultTableModel im = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (OrderItem item : o.getItems()) {
            im.addRow(new Object[]{item.getProduct().getName(), item.getQuantity(),
                String.format("%.0f L", item.getUnitPrice()), String.format("%.0f L", item.getSubtotal())});
        }
        JTable it = new JTable(im);
        UITheme.styleTable(it);
        panel.add(UITheme.scrollPane(it), BorderLayout.CENTER);

        JLabel total = new JLabel("TOTALI: " + String.format("%.0f L", o.getTotalAmount()), SwingConstants.RIGHT);
        total.setFont(new Font("Segoe UI", Font.BOLD, 15));
        total.setForeground(UITheme.ACCENT_GREEN);
        panel.add(total, BorderLayout.SOUTH);

        d.add(panel);
        d.setVisible(true);
    }

    private void changeStatus() {
        Order o = getSelected();
        if (o == null) return;
        Order.Status oldStatus = o.getStatus();
        Order.Status[] statuses = Order.Status.values();
        Order.Status newStatus = (Order.Status) JOptionPane.showInputDialog(this,
            "Statusi aktual: " + oldStatus + "\nZgjidhni statusin e ri:",
            "Ndrysho Statusin", JOptionPane.QUESTION_MESSAGE, null, statuses, oldStatus);
        if (newStatus == null || newStatus == oldStatus) return;
        try {
            ds.updateOrderStatus(o, oldStatus, newStatus);
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Order o = getSelected();
        if (o == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Fshini porosinë " + o.getOrderNumber() + "?",
            "Konfirmo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            ds.removeOrder(o.getId());
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manageItems() {
        Order order = getSelected();
        if (order == null) return;
        if (order.getStatus() == Order.Status.DELIVERED || order.getStatus() == Order.Status.CANCELLED) {
            JOptionPane.showMessageDialog(this, "Porosia e dorëzuar ose e anuluar nuk mund të ndryshohet.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Product> products = ds.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nuk ka produkte të disponueshme.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Menaxho Artikujt — " + order.getOrderNumber(), true);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] cols = {"Produkti", "Sasia", "Çmimi Unit.", "Nëntotali"};
        DefaultTableModel itemsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        Runnable reloadItems = () -> {
            itemsModel.setRowCount(0);
            Order fresh = ds.findOrderById(order.getId()).orElse(order);
            for (OrderItem item : fresh.getItems()) {
                itemsModel.addRow(new Object[]{
                    item.getProduct().getName(), item.getQuantity(),
                    String.format("%.0f", item.getUnitPrice()), String.format("%.0f", item.getSubtotal())
                });
            }
        };
        reloadItems.run();

        JTable itemsTable = new JTable(itemsModel);
        UITheme.styleTable(itemsTable);
        JScrollPane itemsScroll = UITheme.scrollPane(itemsTable);
        itemsScroll.setPreferredSize(new Dimension(0, 180));
        panel.add(itemsScroll, BorderLayout.CENTER);

        JPanel addRow = buildProductItemControlsPanel();
        JComboBox<Product> productBox = UITheme.styledComboBox(products.toArray(new Product[0]));
        JTextField qtyField = UITheme.styledField(6);
        qtyField.setText("1");
        JTextField priceField = UITheme.styledField(8);
        productBox.addActionListener(e -> {
            Product p = (Product) productBox.getSelectedItem();
            if (p != null) priceField.setText(String.valueOf((int) p.getPrice()));
        });
        priceField.setText(String.valueOf((int) products.get(0).getPrice()));

        JButton addBtn = UITheme.successButton("Shto");
        addBtn.addActionListener(e -> {
            try {
                Product p = (Product) productBox.getSelectedItem();
                if (p == null) throw new IllegalArgumentException("Zgjidhni një produkt.");
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                ds.addOrderItem(order.getId(), p.getId(), qty, price);
                reloadItems.run();
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, formatOrderError(ex), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton removeBtn = UITheme.dangerButton("Hiq");
        removeBtn.addActionListener(e -> {
            int row = itemsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Zgjidhni një artikull.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                Order fresh = ds.findOrderById(order.getId()).orElse(order);
                OrderItem item = fresh.getItems().get(row);
                ds.removeOrderItem(order.getId(), item.getId());
                reloadItems.run();
                refresh();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });

        populateProductItemControls(addRow, productBox, qtyField, priceField, addBtn, removeBtn);
        panel.add(addRow, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showOrderDialog(Order.Type type) {
        List<Product> products = ds.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Shtoni të paktën një produkt para se të krijoni porosi.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Porosi e Re: " + (type == Order.Type.INCOMING ? "Hyrëse" : "Dalëse"), true);
        dialog.setSize(720, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JPanel headerForm = new JPanel(new GridBagLayout());
        headerForm.setBackground(UITheme.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String label1 = type == Order.Type.INCOMING ? "Furnitori:" : "Klienti:";
        JTextField supplierField = UITheme.styledField(20);
        JTextField notesField = UITheme.styledField(20);
        JTextField deliveryField = UITheme.styledField(10);
        deliveryField.setText(LocalDate.now().plusDays(7).toString());

        String[] lbs = {label1, "Data e Dorëzimit (YYYY-MM-DD):", "Shënime:"};
        Component[] flds = {supplierField, deliveryField, notesField};
        for (int i = 0; i < lbs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel l = new JLabel(lbs[i]); l.setForeground(UITheme.TEXT_MUTED); l.setFont(UITheme.FONT_BODY);
            headerForm.add(l, gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            flds[i].setPreferredSize(new Dimension(200, 32));
            headerForm.add(flds[i], gbc);
        }
        form.add(headerForm, BorderLayout.NORTH);

        String[] itemCols = {"Produkti", "Sasia", "Çmimi Unit.", "Nëntotali"};
        DefaultTableModel itemsModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable itemsTable = new JTable(itemsModel);
        UITheme.styleTable(itemsTable);
        JScrollPane itemsScroll = UITheme.scrollPane(itemsTable);
        itemsScroll.setPreferredSize(new Dimension(0, 180));
        form.add(itemsScroll, BorderLayout.CENTER);

        ArrayList<Integer> itemProductIds = new ArrayList<>();
        ArrayList<Integer> itemQtys = new ArrayList<>();
        ArrayList<Double> itemPrices = new ArrayList<>();

        JPanel itemControls = buildProductItemControlsPanel();
        JComboBox<Product> productBox = UITheme.styledComboBox(products.toArray(new Product[0]));
        JTextField qtyField = UITheme.styledField(6);
        qtyField.setText("1");
        JTextField priceField = UITheme.styledField(8);
        priceField.setText(String.valueOf((int) products.get(0).getPrice()));
        productBox.addActionListener(e -> {
            Product p = (Product) productBox.getSelectedItem();
            if (p != null) priceField.setText(String.valueOf((int) p.getPrice()));
        });

        JButton addItemBtn = UITheme.successButton("Shto Artikull");
        addItemBtn.addActionListener(e -> {
            try {
                Product p = (Product) productBox.getSelectedItem();
                if (p == null) throw new IllegalArgumentException("Zgjidhni një produkt.");
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                if (qty <= 0 || price < 0) throw new IllegalArgumentException("Sasia dhe çmimi duhet të jenë valid.");

                int productId = p.getId();
                int existingIdx = itemProductIds.indexOf(productId);
                if (existingIdx >= 0) {
                    int newQty = itemQtys.get(existingIdx) + qty;
                    itemQtys.set(existingIdx, newQty);
                    itemsModel.setValueAt(newQty, existingIdx, 1);
                    itemsModel.setValueAt(String.format("%.0f", newQty * itemPrices.get(existingIdx)), existingIdx, 3);
                } else {
                    itemProductIds.add(productId);
                    itemQtys.add(qty);
                    itemPrices.add(price);
                    itemsModel.addRow(new Object[]{p.getName(), qty, String.format("%.0f", price),
                            String.format("%.0f", qty * price)});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton removeItemBtn = UITheme.dangerButton("Hiq");
        removeItemBtn.addActionListener(e -> {
            int row = itemsTable.getSelectedRow();
            if (row >= 0) {
                itemProductIds.remove(row);
                itemQtys.remove(row);
                itemPrices.remove(row);
                itemsModel.removeRow(row);
            }
        });

        populateProductItemControls(itemControls, productBox, qtyField, priceField, addItemBtn, removeItemBtn);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG_CARD);
        footer.add(itemControls, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.BG_CARD);
        JButton cancel = UITheme.ghostButton("Anulo");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = UITheme.successButton("Krijo Porosinë");
        save.addActionListener(e -> {
            try {
                if (itemProductIds.isEmpty()) {
                    throw new IllegalArgumentException("Shtoni të paktën një artikull në porosi.");
                }
                String supplier = supplierField.getText().trim();
                if (supplier.isEmpty()) throw new IllegalArgumentException("Furnitori/Klienti është i detyrueshëm.");
                LocalDate delivery = LocalDate.parse(deliveryField.getText().trim());
                int orderId = ds.nextOrderId();
                Order order = new Order(orderId, ds.generateOrderNumber(orderId),
                    LocalDate.now(), delivery, Order.Status.PENDING, type,
                    supplier, notesField.getText().trim(), currentUser.getId());
                List<DataStore.NewOrderLine> lines = new ArrayList<>();
                for (int i = 0; i < itemProductIds.size(); i++) {
                    lines.add(new DataStore.NewOrderLine(
                            itemProductIds.get(i), itemQtys.get(i), itemPrices.get(i)));
                }
                ds.createOrderWithItems(order, lines);
                refresh();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Porosia u krijua me sukses!\nNr: " + order.getOrderNumber(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, formatOrderError(ex), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel);
        btns.add(save);
        footer.add(btns, BorderLayout.SOUTH);
        form.add(footer, BorderLayout.SOUTH);

        dialog.add(form);
        dialog.setVisible(true);
    }

    private JPanel buildProductItemControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_CARD);
        return panel;
    }

    private void populateProductItemControls(JPanel panel, JComboBox<Product> productBox,
            JTextField qtyField, JTextField priceField, JButton addBtn, JButton removeBtn) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel productLabel = new JLabel("Produkti:");
        productLabel.setForeground(UITheme.TEXT_MUTED);
        panel.add(productLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 3;
        panel.add(productBox, gbc);

        gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel qtyLabel = new JLabel("Sasia:");
        qtyLabel.setForeground(UITheme.TEXT_MUTED);
        panel.add(qtyLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.3;
        panel.add(qtyField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JLabel priceLabel = new JLabel("Çmimi:");
        priceLabel.setForeground(UITheme.TEXT_MUTED);
        panel.add(priceLabel, gbc);

        gbc.gridx = 3; gbc.weightx = 0.3;
        panel.add(priceField, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        panel.add(addBtn, gbc);

        gbc.gridx = 5;
        panel.add(removeBtn, gbc);
    }

    private String formatOrderError(Exception ex) {
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("unique") || msg.contains("duplicate"))) {
            return "Ky produkt ekziston tashmë në porosi.";
        }
        return msg != null ? msg : "Gabim i panjohur.";
    }
}

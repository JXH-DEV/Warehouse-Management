package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.model.*;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private final User currentUser;
    private final DataStore ds = DataStore.getInstance();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private List<Product> currentProducts;

    public ProductPanel(User currentUser) {
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
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(UITheme.BG_DARK);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setBackground(UITheme.BG_DARK);
        searchField = UITheme.styledField(22);
        searchField.setToolTipText("Kërko produkte...");
        JButton searchBtn = UITheme.primaryButton("Kërko");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        JButton clearBtn = UITheme.ghostButton("Pastro");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        leftTools.add(searchField);
        leftTools.add(searchBtn);
        leftTools.add(clearBtn);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setBackground(UITheme.BG_DARK);
        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton addBtn = UITheme.successButton("Produkt i Ri");
            addBtn.addActionListener(e -> showProductDialog(null));
            rightTools.add(addBtn);
        }
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Emri", "Kategoria", "Çmimi (L)", "Sasia", "Njësia", "Min. Stok", "Vlera Totale", "Statusi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(UITheme.BG_DARK);

        if (currentUser.getRole() != User.Role.OPERATOR) {
            JButton editBtn = UITheme.warningButton("Ndrysho");
            editBtn.addActionListener(e -> editSelected());
            JButton deleteBtn = UITheme.dangerButton("Fshi");
            deleteBtn.addActionListener(e -> deleteSelected());
            JButton adjustBtn = UITheme.ghostButton("Rregulllo Stokun");
            adjustBtn.addActionListener(e -> adjustStock());
            actions.add(editBtn);
            actions.add(deleteBtn);
            actions.add(adjustBtn);
        }

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UITheme.sectionLabel("Lista e Produkteve"), BorderLayout.NORTH);
        tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        tableCard.add(actions, BorderLayout.SOUTH);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        currentProducts = ds.getAllProducts();
        populateTable(currentProducts);
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { refresh(); return; }
        currentProducts = ds.searchProducts(q);
        populateTable(currentProducts);
    }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            String status = p.isLowStock() ? "Stok i Ulët" : "Normal";
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory().name(),
                String.format("%.0f", p.getPrice()), p.getQuantity(),
                p.getUnit(), p.getMinStock(),
                String.format("%.0f", p.getTotalValue()), status
            });
        }
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Zgjidhni një produkt.", "Info", JOptionPane.INFORMATION_MESSAGE); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return ds.findProductById(id).orElse(null);
    }

    private void editSelected() {
        Product p = getSelectedProduct();
        if (p != null) showProductDialog(p);
    }

    private void deleteSelected() {
        Product p = getSelectedProduct();
        if (p == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Fshini produktin \"" + p.getName() + "\"?", "Konfirmo Fshirjen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            try {
                ds.removeProduct(p.getId());
                refresh();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adjustStock() {
        Product p = getSelectedProduct();
        if (p == null) return;
        String input = JOptionPane.showInputDialog(this,
            "Produkti: " + p.getName() + "\nSasia aktuale: " + p.getQuantity() + "\nShkruani sasinë (+ ose -):", "Rregulllo Stokun", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int delta = Integer.parseInt(input.trim());
            int newQty = p.getQuantity() + delta;
            if (newQty < 0) { JOptionPane.showMessageDialog(this, "Stoku nuk mund të jetë negativ!", "Gabim", JOptionPane.ERROR_MESSAGE); return; }
            p.setQuantity(newQty);
            ds.updateProduct(p);
            refresh();
            JOptionPane.showMessageDialog(this, "Stoku u rregullua. Sasia e re: " + newQty, "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Numër i pavlefshëm.", "Gabim", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Ndrysho Produktin" : "Produkt i Ri", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF       = UITheme.styledField(20);
        JTextField descF       = UITheme.styledField(20);
        JTextField priceF      = UITheme.styledField(10);
        JTextField qtyF        = UITheme.styledField(10);
        JTextField unitF       = UITheme.styledField(10);
        JTextField minStockF   = UITheme.styledField(10);
        JComboBox<Product.Category> catBox = new JComboBox<>(Product.Category.values());
        catBox.setBackground(UITheme.BG_INPUT);
        catBox.setForeground(UITheme.TEXT_PRIMARY);

        if (isEdit) {
            nameF.setText(existing.getName());
            descF.setText(existing.getDescription());
            priceF.setText(String.valueOf((int) existing.getPrice()));
            qtyF.setText(String.valueOf(existing.getQuantity()));
            unitF.setText(existing.getUnit());
            minStockF.setText(String.valueOf(existing.getMinStock()));
            catBox.setSelectedItem(existing.getCategory());
        }

        String[] labels = {"Emri:", "Përshkrimi:", "Çmimi (L):", "Sasia:", "Kategoria:", "Njësia:", "Min. Stok:"};
        Component[] fields = {nameF, descF, priceF, qtyF, catBox, unitF, minStockF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(UITheme.TEXT_MUTED);
            lbl.setFont(UITheme.FONT_BODY);
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            fields[i].setPreferredSize(new Dimension(200, 32));
            form.add(fields[i], gbc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.BG_CARD);
        JButton cancel = UITheme.ghostButton("Anulo");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = UITheme.successButton(isEdit ? "Ruaj Ndryshimet" : "Shto Produktin");
        save.addActionListener(e -> {
            try {
                String name = nameF.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Emri është i detyrueshëm.");
                double price = Double.parseDouble(priceF.getText().trim());
                int qty = Integer.parseInt(qtyF.getText().trim());
                int minS = Integer.parseInt(minStockF.getText().trim());
                if (price < 0 || qty < 0 || minS < 0) {
                    throw new IllegalArgumentException("Çmimi, sasia dhe min. stoku duhet të jenë jo-negativ.");
                }
                Product.Category cat = (Product.Category) catBox.getSelectedItem();
                if (isEdit) {
                    existing.setName(name); existing.setDescription(descF.getText().trim());
                    existing.setPrice(price); existing.setQuantity(qty);
                    existing.setCategory(cat); existing.setUnit(unitF.getText().trim());
                    existing.setMinStock(minS);
                    ds.updateProduct(existing);
                } else {
                    ds.addProduct(new Product(ds.nextProductId(), name, descF.getText().trim(),
                        price, qty, cat, unitF.getText().trim(), minS));
                }
                refresh();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Ju lutem vendosni numra të vlefshëm.", "Gabim", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel); btns.add(save);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2; gbc.insets = new Insets(16, 6, 0, 6);
        form.add(btns, gbc);
        dialog.add(form);
        dialog.setVisible(true);
    }
}

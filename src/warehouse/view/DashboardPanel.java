package warehouse.view;

import warehouse.dao.DataStore;
import warehouse.dao.DataStore.DashboardSnapshot;
import warehouse.dao.DataStore.RecentOrderRow;
import warehouse.model.*;
import warehouse.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final DataStore ds = DataStore.getInstance();

    private JPanel statsRow;
    private DefaultTableModel lowStockModel;
    private DefaultTableModel recentOrderModel;

    private int refreshGeneration;
    private SwingWorker<DashboardSnapshot, Void> activeWorker;

    public DashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(UITheme.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel welcome = new JLabel("Mirë se vini, " + currentUser.getFullName() + "!");
        welcome.setFont(UITheme.FONT_TITLE);
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(welcome);
        wrapper.add(Box.createVerticalStrut(4));

        JLabel sub = UITheme.muted("Ja një pamje e përgjithshme e magazinës sot.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(sub);
        wrapper.add(Box.createVerticalStrut(22));

        statsRow = new JPanel(new GridLayout(1, 5, 14, 0));
        statsRow.setBackground(UITheme.BG_DARK);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(statsRow);
        wrapper.add(Box.createVerticalStrut(24));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setBackground(UITheme.BG_DARK);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lowCard = UITheme.card();
        lowCard.setLayout(new BorderLayout(0, 10));
        lowCard.add(UITheme.sectionLabel("Stok i Ulët"), BorderLayout.NORTH);
        lowStockModel = new DefaultTableModel(new String[]{"Produkti", "Stoku", "Min. Stok"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lowStockTable = new JTable(lowStockModel);
        UITheme.styleTable(lowStockTable);
        lowCard.add(UITheme.scrollPane(lowStockTable), BorderLayout.CENTER);

        JPanel ordCard = UITheme.card();
        ordCard.setLayout(new BorderLayout(0, 10));
        ordCard.add(UITheme.sectionLabel("Porositë e Fundit"), BorderLayout.NORTH);
        recentOrderModel = new DefaultTableModel(new String[]{"Nr.", "Lloji", "Klienti/Furnitori", "Statusi", "Shuma (L)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable recentOrdersTable = new JTable(recentOrderModel);
        UITheme.styleTable(recentOrdersTable);
        ordCard.add(UITheme.scrollPane(recentOrdersTable), BorderLayout.CENTER);

        bottomRow.add(lowCard);
        bottomRow.add(ordCard);
        wrapper.add(bottomRow);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBackground(UITheme.BG_DARK);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {
        refreshAsync();
    }

    public void refreshAsync() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        final int generation = ++refreshGeneration;

        activeWorker = new SwingWorker<DashboardSnapshot, Void>() {
            @Override
            protected DashboardSnapshot doInBackground() {
                return ds.getDashboardSnapshot();
            }

            @Override
            protected void done() {
                if (isCancelled() || generation != refreshGeneration) return;
                try {
                    applySnapshot(get());
                } catch (Exception ex) {
                    if (generation == refreshGeneration) {
                        JOptionPane.showMessageDialog(DashboardPanel.this,
                                ex.getMessage() != null ? ex.getMessage() : "Gabim i panjohur.",
                                "Gabim", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        activeWorker.execute();
    }

    private void applySnapshot(DashboardSnapshot snapshot) {
        statsRow.removeAll();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        statsRow.add(UITheme.statCard("Totali Produkteve", String.valueOf(snapshot.totalProducts), UITheme.ACCENT_BLUE));
        statsRow.add(UITheme.statCard("Porosi Aktive", String.valueOf(snapshot.pendingOrders), UITheme.ACCENT_ORANGE));
        statsRow.add(UITheme.statCard("Dërgesa në Transit", String.valueOf(snapshot.activeShipments), UITheme.ACCENT_CYAN));
        statsRow.add(UITheme.statCard("Totali Porosive", String.valueOf(snapshot.totalOrders), UITheme.ACCENT_PURPLE));
        statsRow.add(UITheme.statCard("Vlera Inventarit (L)", nf.format(snapshot.inventoryValue), UITheme.ACCENT_GREEN));
        statsRow.revalidate();
        statsRow.repaint();

        lowStockModel.setRowCount(0);
        List<Product> lowStock = snapshot.lowStock;
        for (Product p : lowStock) {
            lowStockModel.addRow(new Object[]{p.getName(), p.getQuantity(), p.getMinStock()});
        }
        if (lowStock.isEmpty()) {
            lowStockModel.addRow(new Object[]{"Asnjë produkt me stok të ulët", "", ""});
        }

        recentOrderModel.setRowCount(0);
        for (RecentOrderRow row : snapshot.recentOrders) {
            recentOrderModel.addRow(new Object[]{
                row.orderNumber,
                row.type == Order.Type.INCOMING ? "Hyrëse" : "Dalëse",
                row.supplierOrClient,
                row.status.name(),
                nf.format((long) row.totalAmount)
            });
        }
    }
}

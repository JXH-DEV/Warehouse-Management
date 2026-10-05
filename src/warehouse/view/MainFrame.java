package warehouse.view;

import warehouse.model.User;
import warehouse.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel pageTitle;

    private DashboardPanel dashboardPanel;
    private ProductPanel productPanel;
    private OrderPanel orderPanel;
    private ShipmentPanel shipmentPanel;
    private UserPanel userPanel;

    private static final String DASHBOARD  = "DASHBOARD";
    private static final String PRODUCTS   = "PRODUCTS";
    private static final String ORDERS     = "ORDERS";
    private static final String SHIPMENTS  = "SHIPMENTS";
    private static final String USERS      = "USERS";

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Menaxhimi i Magazines - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 650));
        buildUI();
        SwingUtilities.invokeLater(() -> dashboardPanel.refreshAsync());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_PANEL);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(UITheme.BG_DARK);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        brand.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel brandName = new JLabel("Menaxhimi i Magazines");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(UITheme.ACCENT_BLUE);
        brand.add(brandName, BorderLayout.CENTER);
        sidebar.add(brand);

        JPanel userInfo = new JPanel(new BorderLayout(10, 0));
        userInfo.setBackground(UITheme.BG_CARD);
        userInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        userInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        JPanel userText = new JPanel(new GridLayout(2, 1, 0, 0));
        userText.setOpaque(false);
        JLabel nameL = new JLabel(currentUser.getFullName());
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameL.setForeground(UITheme.TEXT_PRIMARY);
        JLabel roleL = new JLabel(currentUser.getRole().name());
        roleL.setFont(UITheme.FONT_SMALL);
        roleL.setForeground(UITheme.ACCENT_CYAN);
        userText.add(nameL);
        userText.add(roleL);
        userInfo.add(userText, BorderLayout.CENTER);
        sidebar.add(userInfo);
        sidebar.add(Box.createVerticalStrut(10));

        String[][] navItems = {
            {"Dashboard",   DASHBOARD},
            {"Produktet",   PRODUCTS},
            {"Porositë",    ORDERS},
            {"Dërgesat",    SHIPMENTS},
        };
        ButtonGroup bg = new ButtonGroup();
        for (String[] item : navItems) {
            JToggleButton btn = navButton(item[0]);
            bg.add(btn);
            if (item[1].equals(DASHBOARD)) btn.setSelected(true);
            btn.addActionListener(e -> navigate(item[1], item[0]));
            sidebar.add(btn);
        }

        if (currentUser.getRole() == User.Role.ADMIN) {
            JLabel adminSec = UITheme.muted("  ADMIN");
            adminSec.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 0));
            sidebar.add(adminSec);
            JToggleButton usersBtn = navButton("Përdoruesit");
            bg.add(usersBtn);
            usersBtn.addActionListener(e -> navigate(USERS, "Menaxhimi i Përdoruesve"));
            sidebar.add(usersBtn);
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Dilni");
        logoutBtn.setFont(UITheme.FONT_BODY);
        logoutBtn.setBackground(new Color(50, 20, 20));
        logoutBtn.setForeground(UITheme.ACCENT_RED);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        JSeparator logoutSep = new JSeparator();
        logoutSep.setForeground(UITheme.BORDER_COLOR);
        logoutSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(logoutSep);
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private JToggleButton navButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_MUTED);
        btn.setBackground(UITheme.BG_PANEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(new Color(64, 156, 255, 30));
                btn.setForeground(UITheme.ACCENT_BLUE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, UITheme.ACCENT_BLUE),
                    BorderFactory.createEmptyBorder(10, 13, 10, 16)
                ));
            } else {
                btn.setBackground(UITheme.BG_PANEL);
                btn.setForeground(UITheme.TEXT_MUTED);
                btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            }
        });
        return btn;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(UITheme.BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_PANEL);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));
        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(UITheme.FONT_HEADING);
        pageTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel dateLbl = new JLabel(java.time.LocalDate.now().toString());
        dateLbl.setFont(UITheme.FONT_SMALL);
        dateLbl.setForeground(UITheme.TEXT_MUTED);
        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(dateLbl, BorderLayout.EAST);
        area.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_DARK);

        dashboardPanel = new DashboardPanel(currentUser);
        contentPanel.add(dashboardPanel, DASHBOARD);

        area.add(contentPanel, BorderLayout.CENTER);
        return area;
    }

    private ProductPanel ensureProductPanel() {
        if (productPanel == null) {
            productPanel = new ProductPanel(currentUser);
            contentPanel.add(productPanel, PRODUCTS);
        }
        return productPanel;
    }

    private OrderPanel ensureOrderPanel() {
        if (orderPanel == null) {
            orderPanel = new OrderPanel(currentUser);
            contentPanel.add(orderPanel, ORDERS);
        }
        return orderPanel;
    }

    private ShipmentPanel ensureShipmentPanel() {
        if (shipmentPanel == null) {
            shipmentPanel = new ShipmentPanel(currentUser);
            contentPanel.add(shipmentPanel, SHIPMENTS);
        }
        return shipmentPanel;
    }

    private UserPanel ensureUserPanel() {
        if (userPanel == null) {
            userPanel = new UserPanel(currentUser);
            contentPanel.add(userPanel, USERS);
        }
        return userPanel;
    }

    private void navigate(String card, String title) {
        cardLayout.show(contentPanel, card);
        pageTitle.setText(title);
        if (DASHBOARD.equals(card)) {
            dashboardPanel.refreshAsync();
        } else if (PRODUCTS.equals(card)) {
            ensureProductPanel().refresh();
        } else if (ORDERS.equals(card)) {
            ensureOrderPanel().refresh();
        } else if (SHIPMENTS.equals(card)) {
            ensureShipmentPanel().refresh();
        } else if (USERS.equals(card) && currentUser.getRole() == User.Role.ADMIN) {
            ensureUserPanel().refresh();
        }
    }
}

package warehouse.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {

    public static final Color BG_DARK       = new Color(15, 20, 35);
    public static final Color BG_PANEL      = new Color(22, 30, 50);
    public static final Color BG_CARD       = new Color(30, 40, 65);
    public static final Color BG_INPUT      = new Color(20, 28, 48);
    public static final Color ACCENT_BLUE   = new Color(64, 156, 255);
    public static final Color ACCENT_CYAN   = new Color(0, 212, 200);
    public static final Color ACCENT_GREEN  = new Color(52, 211, 153);
    public static final Color ACCENT_ORANGE = new Color(251, 146, 60);
    public static final Color ACCENT_RED    = new Color(248, 113, 113);
    public static final Color ACCENT_PURPLE = new Color(167, 139, 250);
    public static final Color TEXT_PRIMARY  = new Color(240, 245, 255);
    public static final Color TEXT_MUTED    = new Color(120, 140, 175);
    public static final Color BORDER_COLOR  = new Color(40, 55, 90);
    public static final Color TABLE_ALT     = new Color(25, 34, 57);
    public static final Color TABLE_SEL     = new Color(64, 156, 255, 60);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    public static void applyGlobalLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background", ACCENT_BLUE);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_BLUE);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("ScrollPane.background", BG_PANEL);
        UIManager.put("Viewport.background", BG_PANEL);
        UIManager.put("Table.background", BG_PANEL);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", ACCENT_BLUE);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", BG_CARD);
        UIManager.put("TableHeader.foreground", ACCENT_CYAN);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Spinner.background", BG_INPUT);
        UIManager.put("Spinner.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.background", BG_PANEL);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected", BG_CARD);
        UIManager.put("List.background", BG_INPUT);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("CheckBox.background", BG_CARD);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("RadioButton.background", BG_CARD);
        UIManager.put("RadioButton.foreground", TEXT_PRIMARY);
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_RED);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_GREEN);
        btn.setForeground(BG_DARK);
        return btn;
    }

    public static JButton warningButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_ORANGE);
        btn.setForeground(BG_DARK);
        return btn;
    }

    public static JButton ghostButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(BG_CARD);
        btn.setForeground(TEXT_MUTED);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(7, 17, 7, 17)
        ));
        return btn;
    }

    public static JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setFont(FONT_BODY);
        tf.setBorder(defaultFieldBorder());
        return tf;
    }

    public static JPasswordField styledPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_BLUE);
        pf.setFont(FONT_BODY);
        pf.setBorder(defaultFieldBorder());
        return pf;
    }

    @SafeVarargs
    public static <T> JComboBox<T> styledComboBox(T... items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setBackground(BG_INPUT);
        box.setForeground(TEXT_PRIMARY);
        box.setFont(FONT_BODY);
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(ACCENT_BLUE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(BG_INPUT);
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        });
        return box;
    }

    public static Border defaultFieldBorder() {
        return fieldBorder(BORDER_COLOR);
    }

    public static Border focusFieldBorder() {
        return fieldBorder(ACCENT_BLUE);
    }

    public static Border errorFieldBorder() {
        return fieldBorder(ACCENT_RED);
    }

    public static Border fieldBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    public static void wireFieldFocus(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(focusFieldBorder());
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(defaultFieldBorder());
            }
        });
    }

    public static JButton loginPrimaryButton(String text) {
        Color hover = new Color(
            Math.min(255, ACCENT_BLUE.getRed() + 25),
            Math.min(255, ACCENT_BLUE.getGreen() + 25),
            Math.min(255, ACCENT_BLUE.getBlue() + 25));
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_BLUE);
            }
        });
        return btn;
    }

    public static JPanel roundedPanel(Color bg, int radius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /** Login form card: rounded corners, top accent stripe, subtle shadow. */
    public static JPanel loginFormCard(int radius) {
        return new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRoundRect(4, 6, w - 8, h - 8, radius, radius);
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(2, 3, w - 4, h - 4, radius, radius);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
                g2.setColor(ACCENT_CYAN);
                g2.fillRoundRect(0, 0, w - 1, 3, radius, radius);
                g2.fillRect(0, 2, w - 1, 2);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
                g2.dispose();
            }
        };
    }

    public static JComponent monogramBadge(String initials, int size) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 50));
                g2.fillOval(0, 0, size, size);
                g2.setColor(ACCENT_BLUE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, size - 2, size - 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(size, size));
        badge.setMaximumSize(new Dimension(size, size));
        badge.setLayout(new GridBagLayout());
        JLabel lbl = new JLabel(initials, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(ACCENT_BLUE);
        badge.add(lbl);
        return badge;
    }

    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(ACCENT_CYAN);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    public static JLabel muted(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(64, 156, 255, 80));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_CARD);
        header.setForeground(ACCENT_CYAN);
        header.setFont(FONT_HEADING);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        header.setReorderingAllowed(false);

        // Alternating row renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? BG_PANEL : TABLE_ALT);
                c.setForeground(sel ? Color.WHITE : TEXT_PRIMARY);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static JScrollPane scrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setBackground(BG_CARD);
        return sp;
    }

    public static JPanel statCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            )
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accentColor);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }
}

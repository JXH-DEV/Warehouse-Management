package warehouse;

import warehouse.dao.DataStore;
import warehouse.util.UITheme;
import warehouse.view.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalLook();
        try {
            DataStore.testConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Nuk u lidh me databazën Supabase.\n\n"
                            + "Gabim: " + e.getMessage(),
                    "Gabim Lidhjeje", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}

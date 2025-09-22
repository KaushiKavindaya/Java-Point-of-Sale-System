package app;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // The application MUST start with the DashboardFrame.
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            }
        });
    }
}
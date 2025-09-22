package app;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CheckoutDialog extends JDialog {

    private final BigDecimal totalPayable;
    private boolean paymentSuccessful = false;

    // Shared components
    private JRadioButton cashRadioButton;
    private JRadioButton cardRadioButton;
    private JPanel paymentDetailsPanel;
    private CardLayout cardLayout;

    // Cash payment components
    private JTextField cashTenderedField;
    private JLabel changeAmountLabel;

    // Card payment components
    private JComboBox<String> cardTypeComboBox;

    // Constants for CardLayout
    private static final String CASH_PANEL = "CashPayment";
    private static final String CARD_PANEL = "CardPayment";

    public CheckoutDialog(Frame owner, double totalAmount) {
        super(owner, "Checkout", true); // true for modal
        this.totalPayable = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP);

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel: Display Total ---
        JPanel totalPanel = new JPanel();
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel totalLabel = new JLabel(String.format("Total Payable: Rs.%.2f", totalPayable));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalPanel.add(totalLabel);
        add(totalPanel, BorderLayout.NORTH);

        // --- Center Panel: Payment Method and Details ---
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Radio buttons to switch payment method
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cashRadioButton = new JRadioButton("Cash", true);
        cardRadioButton = new JRadioButton("Card");
        ButtonGroup group = new ButtonGroup();
        group.add(cashRadioButton);
        group.add(cardRadioButton);
        radioPanel.add(cashRadioButton);
        radioPanel.add(cardRadioButton);
        centerPanel.add(radioPanel, BorderLayout.NORTH);

        // Panel with CardLayout to hold different payment detail forms
        cardLayout = new CardLayout();
        paymentDetailsPanel = new JPanel(cardLayout);
        paymentDetailsPanel.add(createCashPanel(), CASH_PANEL);
        paymentDetailsPanel.add(createCardPanel(), CARD_PANEL);
        centerPanel.add(paymentDetailsPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Panel: Action Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("Confirm Payment");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        cashRadioButton.addActionListener(e -> cardLayout.show(paymentDetailsPanel, CASH_PANEL));
        cardRadioButton.addActionListener(e -> cardLayout.show(paymentDetailsPanel, CARD_PANEL));

        cancelButton.addActionListener(e -> dispose());

        confirmButton.addActionListener(e -> {
            if (validatePayment()) {
                paymentSuccessful = true;
                dispose();
            }
        });
    }

    private JPanel createCashPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Amount Tendered:"));
        cashTenderedField = new JTextField();
        panel.add(cashTenderedField);

        panel.add(new JLabel("Change:"));
        changeAmountLabel = new JLabel("Rs.0.00");
        changeAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(changeAmountLabel);

        // Listener to calculate change in real-time
        cashTenderedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateChange(); }
            public void removeUpdate(DocumentEvent e) { calculateChange(); }
            public void changedUpdate(DocumentEvent e) { calculateChange(); }
        });

        return panel;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Card Type:"));
        cardTypeComboBox = new JComboBox<>(new String[]{"Visa", "Mastercard", "Amex", "Discover"});
        panel.add(cardTypeComboBox);

        return panel;
    }

    private void calculateChange() {
        try {
            BigDecimal tendered = new BigDecimal(cashTenderedField.getText());
            BigDecimal change = tendered.subtract(totalPayable);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeAmountLabel.setForeground(Color.BLACK);
                changeAmountLabel.setText(String.format("Rs.%.2f", change));
            } else {
                changeAmountLabel.setForeground(Color.RED);
                changeAmountLabel.setText("Insufficient Amount");
            }
        } catch (NumberFormatException e) {
            changeAmountLabel.setForeground(Color.RED);
            changeAmountLabel.setText("Invalid Input");
        }
    }

    private boolean validatePayment() {
        if (cashRadioButton.isSelected()) {
            try {
                BigDecimal tendered = new BigDecimal(cashTenderedField.getText());
                if (tendered.compareTo(totalPayable) < 0) {
                    JOptionPane.showMessageDialog(this, "The amount tendered is less than the total payable.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount for cash tendered.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    // --- Public Getters to retrieve payment details ---
    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public String getPaymentMethod() {
        return cashRadioButton.isSelected() ? "Cash" : "Card";
    }

    public BigDecimal getCashTendered() {
        if (cashRadioButton.isSelected()) {
            try {
                return new BigDecimal(cashTenderedField.getText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public BigDecimal getChangeGiven() {
        if (cashRadioButton.isSelected()) {
            try {
                BigDecimal tendered = new BigDecimal(cashTenderedField.getText());
                BigDecimal change = tendered.subtract(totalPayable);
                return (change.compareTo(BigDecimal.ZERO) >= 0) ? change : BigDecimal.ZERO;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public String getCardType() {
        if (cardRadioButton.isSelected()) {
            return (String) cardTypeComboBox.getSelectedItem();
        }
        return null;
    }
}
package com.example.expensemanager.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Settlement implements Serializable {
    private String id;
    private Date date;
    private List<SettlementItem> settlements;
    private double totalAmount;
    private String description;

    // Empty constructor for GSON
    public Settlement() {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
    }

    public Settlement(Date date, List<SettlementItem> settlements, String description) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.settlements = settlements;
        this.totalAmount = calculateTotal(settlements);
        this.description = description;
    }

    private double calculateTotal(List<SettlementItem> items) {
        double total = 0;
        if (items != null) {
            for (SettlementItem item : items) {
                total += item.getAmount();
            }
        }
        return total;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) {
        this.date = date;
    }

    public List<SettlementItem> getSettlements() { return settlements; }
    public void setSettlements(List<SettlementItem> settlements) {
        this.settlements = settlements;
        this.totalAmount = calculateTotal(settlements);
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFormattedDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public String getFormattedDetails() {
        StringBuilder details = new StringBuilder();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        details.append("Settlement Date: ").append(getFormattedDate()).append("\n\n");
        details.append("Description: ").append(description != null ? description : "").append("\n\n");
        details.append("Total Amount: ").append(currencyFormat.format(totalAmount)).append("\n");
        details.append("Number of Transactions: ").append(settlements != null ? settlements.size() : 0).append("\n\n");

        if (settlements != null && !settlements.isEmpty()) {
            details.append("Transaction Details:\n");
            for (int i = 0; i < settlements.size(); i++) {
                SettlementItem item = settlements.get(i);
                details.append(i + 1).append(". ")
                        .append(item.getFrom().getName())
                        .append(" → ")
                        .append(item.getTo().getName())
                        .append(": ")
                        .append(currencyFormat.format(item.getAmount()))
                        .append("\n");
            }
        }

        return details.toString();
    }

    public String getShortSummary() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return String.format("%s - %d transactions - %s",
                getFormattedDate(),
                settlements != null ? settlements.size() : 0,
                currencyFormat.format(totalAmount));
    }
}
package com.example.expensemanager.model;

import java.io.Serializable;

public class SettlementItem implements Serializable {
    private Person from;
    private Person to;
    private double amount;
    private boolean settled;

    // Empty constructor for GSON
    public SettlementItem() {
    }

    public SettlementItem(Person from, Person to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.settled = false;
    }

    // Getters and Setters
    public Person getFrom() { return from; }
    public void setFrom(Person from) { this.from = from; }

    public Person getTo() { return to; }
    public void setTo(Person to) { this.to = to; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }

    @Override
    public String toString() {
        return String.format("%s → %s: $%.2f",
                from != null ? from.getName() : "Unknown",
                to != null ? to.getName() : "Unknown",
                amount);
    }
}
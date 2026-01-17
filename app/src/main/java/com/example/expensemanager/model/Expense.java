package com.example.expensemanager.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Expense implements Serializable {
    private String id;
    private Person paidBy;
    private double amount;
    private String description;
    private Date date;
    private String category;

    // Empty constructor for GSON
    public Expense() {
        this.id = UUID.randomUUID().toString();
    }

    public Expense(Person paidBy, double amount, String description, Date date, String category) {
        this.id = UUID.randomUUID().toString();
        this.paidBy = paidBy;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public Expense(String id, Person paidBy, double amount, String description, Date date, String category) {
        this.id = id;
        this.paidBy = paidBy;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Person getPaidBy() { return paidBy; }
    public void setPaidBy(Person paidBy) { this.paidBy = paidBy; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
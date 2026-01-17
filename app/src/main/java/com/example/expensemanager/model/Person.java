package com.example.expensemanager.model;

import java.io.Serializable;
import java.util.UUID;

public class Person implements Serializable {
    private String id;
    private String name;
    private double totalPaid;
    private String colorHex; // For UI color coding

    // Empty constructor for GSON
    public Person() {
        this.id = UUID.randomUUID().toString();
        this.colorHex = generateRandomColor();
    }

    public Person(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.totalPaid = 0.0;
        this.colorHex = generateRandomColor();
    }

    public Person(String id, String name, double totalPaid, String colorHex) {
        this.id = id;
        this.name = name;
        this.totalPaid = totalPaid;
        this.colorHex = colorHex;
    }

    private String generateRandomColor() {
        String[] colors = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
                "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
        };
        return colors[(int) (Math.random() * colors.length)];
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public void addPayment(double amount) { this.totalPaid += amount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
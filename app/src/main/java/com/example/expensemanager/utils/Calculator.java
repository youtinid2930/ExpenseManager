package com.example.expensemanager.utils;

import android.util.Log;

import com.example.expensemanager.model.Person;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {

    public static Map<Person, Double> calculateBalances(List<Person> people, double totalExpenses) {
        if (people.isEmpty()) {
            return new HashMap<>();
        }
        Log.d("Information Balance", "people size: "+people.size()+" totalExpenses: "+totalExpenses);
        double sharePerPerson = totalExpenses / people.size();
        Map<Person, Double> balances = new HashMap<>();

        for (Person person : people) {
            double balance = person.getTotalPaid() - sharePerPerson;
            // Round to 2 decimal places to avoid floating point issues
            balance = Math.round(balance * 100.0) / 100.0;
            balances.put(person, balance);
            Log.d("Information Balance", "person: "+person+" balance: "+balance);
        }

        return balances;
    }

    public static List<Settlement> getSettlementSuggestions(Map<Person, Double> balances) {
        List<Settlement> settlements = new ArrayList<>();

        // Create sorted lists of debtors (negative balance) and creditors (positive balance)
        List<Person> debtors = new ArrayList<>();
        List<Person> creditors = new ArrayList<>();

        for (Map.Entry<Person, Double> entry : balances.entrySet()) {
            if (entry.getValue() < 0) {
                debtors.add(entry.getKey());
            } else if (entry.getValue() > 0) {
                creditors.add(entry.getKey());
            }
        }

        // Sort by absolute balance
        debtors.sort(Comparator.comparing(p -> Math.abs(balances.get(p))));
        creditors.sort(Comparator.comparing(p -> Math.abs(balances.get(p))));
        debtors.sort(Comparator.comparing(p -> balances.get(p)));
        creditors.sort(Comparator.comparing(p -> -balances.get(p)));

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Person debtor = debtors.get(0);
            Person creditor = creditors.get(0);

            double debt = Math.abs(balances.get(debtor));
            double credit = balances.get(creditor);

            double amount = Math.min(debt, credit);

            if (amount > 0.01) { // Ignore trivial amounts
                settlements.add(new Settlement(debtor, creditor, amount));

                // Update balances
                balances.put(debtor, balances.get(debtor) + amount);
                balances.put(creditor, balances.get(creditor) - amount);

                // Remove settled persons
                if (Math.abs(balances.get(debtor)) < 0.01) {
                    debtors.remove(0);
                }
                if (Math.abs(balances.get(creditor)) < 0.01) {
                    creditors.remove(0);
                }
            }
        }

        return settlements;
    }

    public static class Settlement {
        private Person from;
        private Person to;
        private double amount;

        public Settlement(Person from, Person to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        public Person getFrom() { return from; }
        public Person getTo() { return to; }
        public double getAmount() { return amount; }

        @Override
        public String toString() {
            return String.format("%s → %s: $%.2f", from.getName(), to.getName(), amount);
        }
    }
}
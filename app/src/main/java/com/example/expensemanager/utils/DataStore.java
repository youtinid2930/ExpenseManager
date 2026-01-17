package com.example.expensemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.Person;
import com.example.expensemanager.model.Settlement;
import com.example.expensemanager.model.SettlementItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class DataStore {
    private static DataStore instance;
    private List<Person> peopleList;
    private List<Expense> expenseList;
    private List<Expense> archivedExpenses;
    private List<Settlement> settlements;
    private Map<String, Person> peopleMap;

    private Context context;
    private SharedPreferences preferences;
    private Gson gson;

    // Preference keys
    private static final String PREF_NAME = "ExpenseManagerData";
    private static final String KEY_PEOPLE = "people_list";
    private static final String KEY_EXPENSES = "expenses_list";
    private static final String KEY_ARCHIVED_EXPENSES = "archived_expenses";
    private static final String KEY_SETTLEMENTS = "settlements";
    private static final String KEY_LAST_PERSON_ID = "last_person_id";
    private static final String KEY_LAST_EXPENSE_ID = "last_expense_id";
    private static final String KEY_LAST_SETTLEMENT_ID = "last_settlement_id";

    private DataStore(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();

        loadAllData();
    }

    public static synchronized DataStore getInstance(Context context) {
        if (instance == null) {
            instance = new DataStore(context);
        }
        return instance;
    }

    // Initialize with Application context in your Application class
    public static void initialize(Context context) {
        if (instance == null) {
            instance = new DataStore(context);
        }
    }

    // For backward compatibility
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DataStore not initialized. Call initialize() first.");
        }
        return instance;
    }

    // Load all data from SharedPreferences
    private void loadAllData() {
        loadPeople();
        loadExpenses();
        loadArchivedExpenses();
        loadSettlements();
        rebuildPeopleMap();
    }

    // Save all data to SharedPreferences
    private void saveAllData() {
        savePeople();
        saveExpenses();
        saveArchivedExpenses();
        saveSettlements();
    }

    // People data methods
    private void loadPeople() {
        String json = preferences.getString(KEY_PEOPLE, "[]");
        Type type = new TypeToken<List<Person>>(){}.getType();
        peopleList = gson.fromJson(json, type);
        if (peopleList == null) {
            peopleList = new ArrayList<>();
        }
    }

    private void savePeople() {
        String json = gson.toJson(peopleList);
        preferences.edit().putString(KEY_PEOPLE, json).apply();
    }

    // Expense data methods
    private void loadExpenses() {
        String json = preferences.getString(KEY_EXPENSES, "[]");
        Type type = new TypeToken<List<Expense>>(){}.getType();
        expenseList = gson.fromJson(json, type);
        if (expenseList == null) {
            expenseList = new ArrayList<>();
        }
    }

    private void saveExpenses() {
        String json = gson.toJson(expenseList);
        preferences.edit().putString(KEY_EXPENSES, json).apply();
    }

    // Archived expenses methods
    private void loadArchivedExpenses() {
        String json = preferences.getString(KEY_ARCHIVED_EXPENSES, "[]");
        Type type = new TypeToken<List<Expense>>(){}.getType();
        archivedExpenses = gson.fromJson(json, type);
        if (archivedExpenses == null) {
            archivedExpenses = new ArrayList<>();
        }
    }

    private void saveArchivedExpenses() {
        String json = gson.toJson(archivedExpenses);
        preferences.edit().putString(KEY_ARCHIVED_EXPENSES, json).apply();
    }

    // Settlements methods
    private void loadSettlements() {
        String json = preferences.getString(KEY_SETTLEMENTS, "[]");
        Type type = new TypeToken<List<Settlement>>(){}.getType();
        settlements = gson.fromJson(json, type);
        if (settlements == null) {
            settlements = new ArrayList<>();
        }
    }

    private void saveSettlements() {
        String json = gson.toJson(settlements);
        preferences.edit().putString(KEY_SETTLEMENTS, json).apply();
    }

    // Rebuild people map after loading
    private void rebuildPeopleMap() {
        peopleMap = new HashMap<>();
        for (Person person : peopleList) {
            peopleMap.put(person.getId(), person);
        }
    }

    // Generate unique IDs
    private String generatePersonId() {
        int lastId = preferences.getInt(KEY_LAST_PERSON_ID, 0);
        lastId++;
        preferences.edit().putInt(KEY_LAST_PERSON_ID, lastId).apply();
        return "P" + lastId;
    }

    private String generateExpenseId() {
        int lastId = preferences.getInt(KEY_LAST_EXPENSE_ID, 0);
        lastId++;
        preferences.edit().putInt(KEY_LAST_EXPENSE_ID, lastId).apply();
        return "E" + lastId;
    }

    private String generateSettlementId() {
        int lastId = preferences.getInt(KEY_LAST_SETTLEMENT_ID, 0);
        lastId++;
        preferences.edit().putInt(KEY_LAST_SETTLEMENT_ID, lastId).apply();
        return "S" + lastId;
    }

    // People operations (updated with persistence)
    public void addPerson(Person person) {
        if (!peopleMap.containsKey(person.getId())) {
            if (person.getId() == null || person.getId().isEmpty()) {
                person.setId(generatePersonId());
            }
            peopleList.add(person);
            peopleMap.put(person.getId(), person);
            savePeople();
        }
    }

    public boolean removePerson(String personId) {
        Person person = peopleMap.remove(personId);
        if (person != null) {
            // Remove person from expenses
            List<Expense> toRemove = new ArrayList<>();
            for (Expense expense : expenseList) {
                if (expense.getPaidBy().getId().equals(personId)) {
                    toRemove.add(expense);
                }
            }
            expenseList.removeAll(toRemove);

            boolean removed = peopleList.remove(person);
            if (removed) {
                savePeople();
                saveExpenses();
            }
            return removed;
        }
        return false;
    }

    public Person getPersonById(String id) {
        return peopleMap.get(id);
    }

    public Person getPersonByName(String name) {
        for (Person person : peopleList) {
            if (person.getName().equalsIgnoreCase(name)) {
                return person;
            }
        }
        return null;
    }

    public List<Person> getPeople() {
        return new ArrayList<>(peopleList);
    }

    // Expense operations (updated with persistence)
    public void addExpense(Expense expense) {
        if (expense.getId() == null || expense.getId().isEmpty()) {
            expense.setId(generateExpenseId());
        }
        expenseList.add(expense);
        expense.getPaidBy().addPayment(expense.getAmount());
        saveExpenses();
        savePeople(); // Update person's total paid
    }

    public boolean removeExpense(String expenseId) {
        for (Expense expense : expenseList) {
            if (expense.getId().equals(expenseId)) {
                // Subtract amount from person's total paid
                Person payer = expense.getPaidBy();
                payer.addPayment(-expense.getAmount());
                boolean removed = expenseList.remove(expense);
                if (removed) {
                    saveExpenses();
                    savePeople();
                }
                return removed;
            }
        }
        return false;
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenseList);
    }

    public List<Expense> getExpensesByPerson(String personId) {
        List<Expense> result = new ArrayList<>();
        for (Expense expense : expenseList) {
            if (expense.getPaidBy().getId().equals(personId)) {
                result.add(expense);
            }
        }
        return result;
    }

    // Clear all data
    public void clearAll() {
        peopleList.clear();
        expenseList.clear();
        peopleMap.clear();
        archivedExpenses.clear();
        settlements.clear();

        preferences.edit()
                .remove(KEY_PEOPLE)
                .remove(KEY_EXPENSES)
                .remove(KEY_ARCHIVED_EXPENSES)
                .remove(KEY_SETTLEMENTS)
                .remove(KEY_LAST_PERSON_ID)
                .remove(KEY_LAST_EXPENSE_ID)
                .remove(KEY_LAST_SETTLEMENT_ID)
                .apply();
    }

    // Calculate total expenses
    public double getTotalExpenses() {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        return total;
    }

    // Settlement methods (updated with persistence)
    public void addSettlement(Settlement settlement) {
        if (settlement.getId() == null || settlement.getId().isEmpty()) {
            settlement.setId(generateSettlementId());
        }
        settlements.add(settlement);
        saveSettlements();
    }

    public List<Settlement> getSettlements() {
        return new ArrayList<>(settlements);
    }

    public List<Expense> getArchivedExpenses() {
        return new ArrayList<>(archivedExpenses);
    }

    // Reset cycle - archive current expenses and reset totals
    public Settlement resetCycle(String description) {
        // Get current settlement suggestions
        List<Calculator.Settlement> currentSettlements =
                Calculator.getSettlementSuggestions(calculateCurrentBalances());

        // Create settlement items
        List<SettlementItem> settlementItems = new ArrayList<>();
        for (Calculator.Settlement calcSettlement : currentSettlements) {
            settlementItems.add(new SettlementItem(
                    calcSettlement.getFrom(),
                    calcSettlement.getTo(),
                    calcSettlement.getAmount()
            ));
        }

        // Create settlement record
        Settlement settlement = new Settlement(new Date(), settlementItems, description);
        settlement.setId(generateSettlementId());
        settlements.add(settlement);

        // Archive current expenses
        archivedExpenses.addAll(expenseList);

        // Reset people's totals
        for (Person person : peopleList) {
            person.setTotalPaid(0.0);
        }

        // Clear current expenses
        expenseList.clear();

        // Save all changes
        saveAllData();

        return settlement;
    }

    private Map<Person, Double> calculateCurrentBalances() {
        double totalExpenses = getTotalExpenses();
        return Calculator.calculateBalances(peopleList, totalExpenses);
    }

    // Check if cycle can be ended (all balances are zero)
    public boolean canEndCycle() {
        Map<Person, Double> balances = calculateCurrentBalances();
        for (double balance : balances.values()) {
            if (Math.abs(balance) > 0.01) { // Allow small rounding errors
                return false;
            }
        }
        return true;
    }

    public boolean removeSettlement(String settlementId) {
        for (Settlement settlement : settlements) {
            if (settlement.getId().equals(settlementId)) {
                boolean removed = settlements.remove(settlement);
                if (removed) {
                    saveSettlements();
                }
                return removed;
            }
        }
        return false;
    }
}
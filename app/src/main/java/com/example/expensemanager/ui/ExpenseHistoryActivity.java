package com.example.expensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.Person;
import com.example.expensemanager.utils.DataStore;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExpenseHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private TextView tvEmptyState, tvTotalExpenses, tvExpenseCount;
    private View emptyStateLayout;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabAddExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        setSupportActionBar(findViewById(R.id.toolbar));

        // Enable back button for secondary activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        initViews();
        setupRecyclerView();
        setupFilterChips();
        updateUI();

        fabAddExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewExpenses);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvExpenseCount = findViewById(R.id.tvExpenseCount);
        emptyStateLayout = findViewById(R.id.emptyState);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        fabAddExpense = findViewById(R.id.fabAddExpense);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        // Clear existing chips
        chipGroupFilters.removeAllViews();

        // Add "All" chip
        Chip chipAll = new Chip(this);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.filter(null, null);
                chipGroupFilters.clearCheck();
                chipAll.setChecked(true);
            }
        });
        chipGroupFilters.addView(chipAll);

        // Add person filters
        Set<String> addedPeople = new HashSet<>();
        for (Expense expense : DataStore.getInstance().getExpenses()) {
            String personName = expense.getPaidBy().getName();
            if (!addedPeople.contains(personName)) {
                addedPeople.add(personName);
                Chip chip = new Chip(this);
                chip.setText(personName);
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        adapter.filter(personName, null);
                    }
                });
                chipGroupFilters.addView(chip);
            }
        }

        // Add category filters
        Set<String> addedCategories = new HashSet<>();
        for (Expense expense : DataStore.getInstance().getExpenses()) {
            String category = expense.getCategory();
            if (!addedCategories.contains(category)) {
                addedCategories.add(category);
                Chip chip = new Chip(this);
                chip.setText(category);
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        adapter.filter(null, category);
                    }
                });
                chipGroupFilters.addView(chip);
            }
        }
    }

    private void refreshData() {
        if (adapter != null) {
            adapter.refreshData();
            setupFilterChips();
            updateUI();
        }
    }

    private void updateUI() {
        List<Expense> allExpenses = DataStore.getInstance().getExpenses();

        if (allExpenses.isEmpty()) {
            // Show empty state
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            chipGroupFilters.setVisibility(View.GONE);
        } else {
            // Show data
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            chipGroupFilters.setVisibility(View.VISIBLE);

            // Update statistics
            updateStatistics(allExpenses);
        }
    }

    private void updateStatistics(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        tvTotalExpenses.setText(currencyFormat.format(total));
        tvExpenseCount.setText(String.valueOf(expenses.size()));
    }

    // Called from empty state button
    public void onAddExpenseClick(View view) {
        startActivity(new Intent(this, AddExpenseActivity.class));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Expense Adapter
    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> implements Filterable {

        private List<Expense> originalList;
        private List<Expense> filteredList;
        private SimpleDateFormat dateFormat;
        private NumberFormat currencyFormat;

        public ExpenseAdapter() {
            this.originalList = DataStore.getInstance().getExpenses();
            this.filteredList = new ArrayList<>(originalList);
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        }

        public void refreshData() {
            this.originalList = DataStore.getInstance().getExpenses();
            this.filteredList = new ArrayList<>(originalList);
            notifyDataSetChanged();
        }

        public void filter(String personName, String category) {
            filteredList.clear();

            for (Expense expense : originalList) {
                boolean matchesPerson = personName == null ||
                        expense.getPaidBy().getName().equals(personName);
                boolean matchesCategory = category == null ||
                        expense.getCategory().equals(category);

                if (matchesPerson && matchesCategory) {
                    filteredList.add(expense);
                }
            }

            notifyDataSetChanged();
            updateStatistics(filteredList);
        }

        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            Expense expense = filteredList.get(position);
            holder.bind(expense);

            holder.itemView.setOnLongClickListener(v -> {
                showExpenseDetails(expense);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    return null;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    // Not used
                }
            };
        }

        private void showExpenseDetails(Expense expense) {
            // Create a custom dialog to show expense details
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ExpenseHistoryActivity.this);

            View dialogView = LayoutInflater.from(ExpenseHistoryActivity.this)
                    .inflate(R.layout.dialog_expense_details, null);

            // Setup dialog views
            TextView tvDialogDescription = dialogView.findViewById(R.id.tvDialogDescription);
            TextView tvDialogAmount = dialogView.findViewById(R.id.tvDialogAmount);
            TextView tvDialogPerson = dialogView.findViewById(R.id.tvDialogPerson);
            TextView tvDialogCategory = dialogView.findViewById(R.id.tvDialogCategory);
            TextView tvDialogDate = dialogView.findViewById(R.id.tvDialogDate);

            tvDialogDescription.setText(expense.getDescription());
            tvDialogAmount.setText(currencyFormat.format(expense.getAmount()));
            tvDialogPerson.setText("Paid by: " + expense.getPaidBy().getName());
            tvDialogCategory.setText("Category: " + expense.getCategory());
            tvDialogDate.setText("Date: " + dateFormat.format(expense.getDate()));

            builder.setView(dialogView)
                    .setTitle("Expense Details")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .setNeutralButton("Delete", (dialog, which) -> {
                        showDeleteConfirmation(expense);
                    });

            builder.create().show();
        }

        private void showDeleteConfirmation(Expense expense) {
            new androidx.appcompat.app.AlertDialog.Builder(ExpenseHistoryActivity.this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete '" + expense.getDescription() +
                            "' for " + currencyFormat.format(expense.getAmount()) + "?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        DataStore.getInstance().removeExpense(expense.getId());
                        refreshData();
                        updateUI();
                        setupFilterChips();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescription, tvAmount, tvPerson, tvDate, tvCategory, tvInitial;
            View colorIndicator;

            public ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvPerson = itemView.findViewById(R.id.tvPerson);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvInitial = itemView.findViewById(R.id.tvInitial);
                colorIndicator = itemView.findViewById(R.id.colorIndicator);
            }

            public void bind(Expense expense) {
                tvDescription.setText(expense.getDescription());
                tvAmount.setText(currencyFormat.format(expense.getAmount()));
                tvPerson.setText(expense.getPaidBy().getName());
                tvDate.setText(dateFormat.format(expense.getDate()));
                tvCategory.setText(expense.getCategory());

                // Set initial and color
                Person person = expense.getPaidBy();
                if (!person.getName().isEmpty()) {
                    tvInitial.setText(String.valueOf(person.getName().charAt(0)).toUpperCase());
                }

                try {
                    colorIndicator.setBackgroundColor(
                            android.graphics.Color.parseColor(person.getColorHex()));
                } catch (Exception e) {
                    colorIndicator.setBackgroundColor(0xFF6200EE);
                }
            }
        }
    }

    // Calculate category distribution
    private Map<String, Double> getCategoryDistribution() {
        Map<String, Double> distribution = new HashMap<>();
        List<Expense> expenses = DataStore.getInstance().getExpenses();

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getAmount();

            distribution.put(category, distribution.getOrDefault(category, 0.0) + amount);
        }

        return distribution;
    }
}
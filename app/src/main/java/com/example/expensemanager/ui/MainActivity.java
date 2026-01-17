package com.example.expensemanager.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.expensemanager.R;
import com.example.expensemanager.utils.Calculator;
import com.example.expensemanager.utils.DataStore;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalExpenses, tvTotalPeople, tvAverageExpense;
    private CardView cardPeople, cardExpenses, cardBalances, cardHistory;
    private CardView cardSettle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initViews();
        updateStats();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_end_cycle) {
            showEndCycleDialog();
            return true;
        } else if (id == R.id.action_view_settlements) {
            startActivity(new Intent(this, SettlementHistoryActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            showExitConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit Expense Manager?")
                .setPositiveButton("EXIT", (dialog, which) -> {
                    // Exit the app
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void initViews() {
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvTotalPeople = findViewById(R.id.tvTotalPeople);
        tvAverageExpense = findViewById(R.id.tvAverageExpense);

        cardPeople = findViewById(R.id.cardPeople);
        cardExpenses = findViewById(R.id.cardExpenses);
        cardBalances = findViewById(R.id.cardBalances);
        cardHistory = findViewById(R.id.cardHistory);
        cardSettle = findViewById(R.id.cardSettle);
    }

    private void updateStats() {
        DataStore dataStore = DataStore.getInstance();
        int totalPeople = dataStore.getPeople().size();
        double totalExpenses = dataStore.getTotalExpenses();
        double average = totalPeople > 0 ? totalExpenses / totalPeople : 0;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        tvTotalExpenses.setText(currencyFormat.format(totalExpenses));
        tvTotalPeople.setText(String.valueOf(totalPeople));
        tvAverageExpense.setText(currencyFormat.format(average));
    }

    private void setupClickListeners() {
        cardPeople.setOnClickListener(v ->
                startActivity(new Intent(this, AddPeopleActivity.class)));

        cardExpenses.setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class)));

        cardBalances.setOnClickListener(v ->
                startActivity(new Intent(this, BalanceActivity.class)));

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseHistoryActivity.class)));

        // Add Settle Up button
        cardSettle.setOnClickListener(v ->
                showEndCycleDialog());

        findViewById(R.id.fabAddExpense).setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class)));
    }

    private void showEndCycleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("End Current Cycle");

        // Create custom dialog view
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_end_cycle, null);
        builder.setView(dialogView);

        TextView tvSummary = dialogView.findViewById(R.id.tvSummary);
        android.widget.EditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Show summary
        DataStore dataStore = DataStore.getInstance();
        StringBuilder summary = new StringBuilder();

        if (dataStore.getPeople().isEmpty()) {
            summary.append("No people added yet.\n");
        } else if (dataStore.getExpenses().isEmpty()) {
            summary.append("No expenses recorded yet.\n");
        } else {
            summary.append("Cycle Summary:\n");
            summary.append("Total Expenses: ").append(
                    NumberFormat.getCurrencyInstance(Locale.US).format(dataStore.getTotalExpenses())
            ).append("\n");
            summary.append("People: ").append(dataStore.getPeople().size()).append("\n");
            summary.append("\nSettlement Suggestions:\n");

            // Get settlement suggestions
            List<Calculator.Settlement> settlements =
                    Calculator.getSettlementSuggestions(
                            Calculator.calculateBalances(
                                    dataStore.getPeople(),
                                    dataStore.getTotalExpenses()
                            )
                    );

            if (settlements.isEmpty()) {
                summary.append("All settled up! ✓\n");
            } else {
                for (Calculator.Settlement s : settlements) {
                    summary.append(String.format("• %s → %s: $%.2f\n",
                            s.getFrom().getName(), s.getTo().getName(), s.getAmount()));
                }
            }
        }

        tvSummary.setText(summary.toString());

        builder.setPositiveButton("END CYCLE", (dialog, which) -> {
            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                description = "Cycle ended on " + new java.text.SimpleDateFormat("MMM dd, yyyy").format(new Date());
            }

            // Reset cycle
            dataStore.resetCycle(description);

            Toast.makeText(this, "Cycle ended successfully!", Toast.LENGTH_LONG).show();
            updateStats();
        });

        builder.setNegativeButton("CANCEL", null);

        // Only enable END button if there are expenses
        AlertDialog dialog = builder.create();
        dialog.show();

        // Disable END button if no expenses
        if (dataStore.getExpenses().isEmpty()) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
}
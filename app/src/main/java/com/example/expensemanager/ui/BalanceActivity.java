package com.example.expensemanager.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Person;
import com.example.expensemanager.utils.Calculator;
import com.example.expensemanager.utils.DataStore;

import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BalanceActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBalances, recyclerViewSettlements;
    private TextView tvTotalExpenses, tvSharePerPerson;
    private BalanceAdapter balanceAdapter;
    private SettlementAdapter settlementAdapter;
    private static final String TAG = "BalanceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        setSupportActionBar(findViewById(R.id.toolbar));

        // Enable back button for secondary activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        initViews();
        calculateAndDisplayBalances();
    }

    private void initViews() {
        recyclerViewBalances = findViewById(R.id.recyclerViewBalances);
        recyclerViewSettlements = findViewById(R.id.recyclerViewSettlements);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvSharePerPerson = findViewById(R.id.tvSharePerPerson);

        recyclerViewBalances.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSettlements.setLayoutManager(new LinearLayoutManager(this));
    }

    private void calculateAndDisplayBalances() {
        DataStore dataStore = DataStore.getInstance();
        List<Person> people = dataStore.getPeople();
        double totalExpenses = dataStore.getTotalExpenses();

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Update total expenses
        tvTotalExpenses.setText(currencyFormat.format(totalExpenses));

        if (people.isEmpty()) {
            tvSharePerPerson.setText("$0.00");
            return;
        }

        // Calculate share per person
        double sharePerPerson = totalExpenses / people.size();
        tvSharePerPerson.setText(currencyFormat.format(sharePerPerson));

        // Calculate balances
        Map<Person, Double> balances = Calculator.calculateBalances(people, totalExpenses);
        for (Map.Entry<Person, Double> entry : balances.entrySet()) {
            Person person = entry.getKey();
            Double balance = entry.getValue();
            Log.d("Blance", "person: "+person+" balance: "+balance);
        }


        // Update balance list
        balanceAdapter = new BalanceAdapter(balances);


        recyclerViewBalances.setAdapter(balanceAdapter);

        // Calculate and display settlements
        List<Calculator.Settlement> settlements = Calculator.getSettlementSuggestions(balances);
        settlementAdapter = new SettlementAdapter(settlements);
        recyclerViewSettlements.setAdapter(settlementAdapter);

        // Show/hide settlement header
        TextView tvSettlementHeader = findViewById(R.id.tvSettlementHeader);
        if (settlements.isEmpty()) {
            tvSettlementHeader.setVisibility(View.GONE);
            recyclerViewSettlements.setVisibility(View.GONE);
        } else {
            tvSettlementHeader.setVisibility(View.VISIBLE);
            recyclerViewSettlements.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // Balance Adapter
    private static class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {

        private final List<Map.Entry<Person, Double>> balanceList;
        private final NumberFormat currencyFormat;
        private static final String TAG = "BalanceAdapter";

        public BalanceAdapter(Map<Person, Double> balances) {
            Log.d(TAG, "=== Adapter Constructor ===");
            Log.d(TAG, "Input balances size: " + balances.size());

            // Create DEEP copies of entries
            this.balanceList = new ArrayList<>();
            for (Map.Entry<Person, Double> entry : balances.entrySet()) {
                // Create new immutable entry
                Map.Entry<Person, Double> newEntry = new AbstractMap.SimpleEntry<>(
                        entry.getKey(), // Person is likely immutable
                        entry.getValue() // Double is immutable
                );
                balanceList.add(newEntry);
                Log.d(TAG, "Added: " + entry.getKey().getName() + " = " + entry.getValue());
            }

            this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            Log.d(TAG, "BalanceList size: " + balanceList.size());
        }

        @NonNull
        @Override
        public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_balance, parent, false);
            return new BalanceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
            Map.Entry<Person, Double> entry = balanceList.get(position);
            Person person = entry.getKey();
            double balance = entry.getValue();

            holder.tvName.setText(person.getName());

            Log.d(TAG,
                    "Person: " + person.getName() +
                            " | Balance: " + balance +
                            " | Position: " + position);

            // Set color based on balance
            if (balance > 0) {
                // Positive - should receive money
                holder.tvStatus.setText("Should Receive");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
                holder.tvBalance.setText("- " + String.format("%.2f", Math.abs(balance)));
                holder.tvBalance.setTextColor(Color.parseColor("#2E7D32"));
                holder.ivIcon.setImageResource(R.drawable.ic_arrow_upward);
                holder.ivIcon.setColorFilter(Color.parseColor("#2E7D32"));
            } else if (balance < 0) {
                // Negative - should pay
                holder.tvStatus.setText("Should Pay");
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
                holder.tvBalance.setText("+ " + String.format("%.2f", Math.abs(balance)));
                holder.tvBalance.setTextColor(Color.parseColor("#D32F2F"));
                holder.ivIcon.setImageResource(R.drawable.ic_arrow_downward);
                holder.ivIcon.setColorFilter(Color.parseColor("#D32F2F"));
            } else {
                // Zero - settled
                holder.tvStatus.setText("Settled Up");
                holder.tvStatus.setTextColor(Color.parseColor("#757575")); // Gray
                holder.tvBalance.setText("$0.00 ");
                holder.tvBalance.setTextColor(Color.parseColor("#757575"));
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivIcon.setColorFilter(Color.parseColor("#757575"));
            }

            // Set person color indicator
            try {
                holder.colorIndicator.setBackgroundColor(Color.parseColor(person.getColorHex()));
            } catch (Exception e) {
                holder.colorIndicator.setBackgroundColor(0xFF6200EE);
            }
        }

        @Override
        public int getItemCount() {
            return balanceList.size();
        }

        static class BalanceViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus, tvBalance;
            View colorIndicator;
            android.widget.ImageView ivIcon;

            public BalanceViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPersonName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvBalance = itemView.findViewById(R.id.tvBalanceAmount);
                colorIndicator = itemView.findViewById(R.id.colorIndicator);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }

    // Settlement Adapter
    private static class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.SettlementViewHolder> {

        private final List<Calculator.Settlement> settlements;
        private final NumberFormat currencyFormat;

        public SettlementAdapter(List<Calculator.Settlement> settlements) {
            this.settlements = settlements;
            this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        }

        @NonNull
        @Override
        public SettlementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_settlement, parent, false);
            return new SettlementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SettlementViewHolder holder, int position) {
            Calculator.Settlement settlement = settlements.get(position);

            holder.tvFrom.setText(settlement.getFrom().getName());
            holder.tvTo.setText(settlement.getTo().getName());
            holder.tvAmount.setText(currencyFormat.format(settlement.getAmount()));

            // Set colors
            try {
                holder.colorFrom.setBackgroundColor(Color.parseColor(settlement.getFrom().getColorHex()));
                holder.colorTo.setBackgroundColor(Color.parseColor(settlement.getTo().getColorHex()));
            } catch (Exception e) {
                holder.colorFrom.setBackgroundColor(0xFF6200EE);
                holder.colorTo.setBackgroundColor(0xFF03DAC5);
            }
        }

        @Override
        public int getItemCount() {
            return settlements.size();
        }

        static class SettlementViewHolder extends RecyclerView.ViewHolder {
            TextView tvFrom, tvTo, tvAmount;
            View colorFrom, colorTo;

            public SettlementViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFrom = itemView.findViewById(R.id.tvFrom);
                tvTo = itemView.findViewById(R.id.tvTo);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                colorFrom = itemView.findViewById(R.id.colorFrom);
                colorTo = itemView.findViewById(R.id.colorTo);
            }
        }
    }
}
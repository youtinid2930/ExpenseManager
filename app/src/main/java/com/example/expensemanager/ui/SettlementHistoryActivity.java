package com.example.expensemanager.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Settlement;
import com.example.expensemanager.model.SettlementItem;
import com.example.expensemanager.utils.DataStore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SettlementHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SettlementAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settlement_history);

        setSupportActionBar(findViewById(R.id.toolbar));

        // Enable back button for secondary activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewSettlements);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SettlementAdapter();
        recyclerView.setAdapter(adapter);

        updateUI();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        adapter.refreshData();
        updateUI();
    }

    private void updateUI() {
        List<Settlement> settlements = DataStore.getInstance().getSettlements();
        if (settlements.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showSettlementDetails(Settlement settlement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settlement Details");

        // Create custom dialog view
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settlement_details, null);
        builder.setView(dialogView);

        TextView tvDetails = dialogView.findViewById(R.id.tvDetails);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        TextView tvCount = dialogView.findViewById(R.id.tvCount);
        TextView tvDescription = dialogView.findViewById(R.id.tvDescription);

        // Format details
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Set basic info
        tvDate.setText(dateFormat.format(settlement.getDate()));
        tvTotal.setText(currencyFormat.format(settlement.getTotalAmount()));
        tvCount.setText(settlement.getSettlements().size() + " transaction(s)");
        tvDescription.setText(settlement.getDescription());

        // Build transaction list
        StringBuilder details = new StringBuilder();
        List<SettlementItem> items = settlement.getSettlements();

        if (items.isEmpty()) {
            details.append("No transactions recorded.");
        } else {
            details.append("Transactions:\n\n");
            for (int i = 0; i < items.size(); i++) {
                SettlementItem item = items.get(i);
                details.append(i + 1).append(". ")
                        .append(item.getFrom().getName())
                        .append(" → ")
                        .append(item.getTo().getName())
                        .append(": ")
                        .append(currencyFormat.format(item.getAmount()));

                if (item.isSettled()) {
                    details.append(" ✓");
                }
                details.append("\n");
            }
        }

        tvDetails.setText(details.toString());

        builder.setPositiveButton("CLOSE", (dialog, which) -> dialog.dismiss());


        builder.setNeutralButton("DELETE", (dialog, which) -> {
            showDeleteConfirmation(settlement);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteConfirmation(Settlement settlement) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Settlement")
                .setMessage("Are you sure you want to delete this settlement record?\n\n" +
                        "Date: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(settlement.getDate()) + "\n" +
                        "Description: " + settlement.getDescription())
                .setPositiveButton("DELETE", (dialog, which) -> {
                    // Remove from DataStore
                    boolean removed = DataStore.getInstance().removeSettlement(settlement.getId());
                    if (removed) {
                        // Refresh the adapter data
                        adapter.refreshData();
                        updateUI();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(SettlementHistoryActivity.this,
                                "Settlement deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettlementHistoryActivity.this,
                                "Failed to delete settlement", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.SettlementViewHolder> {

        private List<Settlement> settlements;
        private SimpleDateFormat dateFormat;
        private NumberFormat currencyFormat;

        public SettlementAdapter() {
            refreshData();
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        }

        public void refreshData() {
            this.settlements = DataStore.getInstance().getSettlements();
        }

        @NonNull
        @Override
        public SettlementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_settlement_history, parent, false);
            return new SettlementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SettlementViewHolder holder, int position) {
            Settlement settlement = settlements.get(position);
            holder.bind(settlement);

            // Click listener for the whole item
            holder.itemView.setOnClickListener(v -> {
                showSettlementDetails(settlement);
            });

            // Long click listener for delete option
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteConfirmation(settlement);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return settlements.size();
        }

        class SettlementViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvDescription, tvAmount, tvCount;

            public SettlementViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvCount = itemView.findViewById(R.id.tvCount);
            }

            public void bind(Settlement settlement) {
                tvDate.setText(dateFormat.format(settlement.getDate()));
                tvDescription.setText(settlement.getDescription());
                tvAmount.setText(currencyFormat.format(settlement.getTotalAmount()));

                int transactionCount = settlement.getSettlements().size();
                String countText = transactionCount + " transaction" + (transactionCount != 1 ? "s" : "");
                tvCount.setText(countText);
            }
        }
    }
}
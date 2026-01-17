package com.example.expensemanager.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.Person;
import com.example.expensemanager.utils.DataStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddPeopleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PeopleAdapter adapter;
    private TextView tvEmptyState, tvTotalPeople, tvTotalPaidAll;
    private View emptyStateLayout;
    private MaterialButton btnAddFirstPerson;
    private EditText etSearch;
    private FloatingActionButton fabAddPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        setSupportActionBar(findViewById(R.id.toolbar));

        // Enable back button for secondary activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        updateUI();

        fabAddPerson.setOnClickListener(v -> showAddPersonDialog());
        btnAddFirstPerson.setOnClickListener(v -> showAddPersonDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPeople);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTotalPeople = findViewById(R.id.tvTotalPeople);
        tvTotalPaidAll = findViewById(R.id.tvTotalPaidAll);
        emptyStateLayout = findViewById(R.id.emptyState);
        btnAddFirstPerson = findViewById(R.id.btnAddFirstPerson);
        etSearch = findViewById(R.id.etSearch);
        fabAddPerson = findViewById(R.id.fabAddPerson);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PeopleAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshData() {
        if (adapter != null) {
            adapter.refreshData();
            updateUI();
        }
    }

    private void updateUI() {
        List<Person> people = DataStore.getInstance().getPeople();

        // Update stats
        tvTotalPeople.setText(String.valueOf(people.size()));

        double totalPaid = 0;
        for (Person person : people) {
            totalPaid += person.getTotalPaid();
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        tvTotalPaidAll.setText(currencyFormat.format(totalPaid));

        // Show/hide empty state
        if (people.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showAddPersonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Person");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null);
        EditText etName = dialogView.findViewById(R.id.etName);

        builder.setView(dialogView);

        builder.setPositiveButton("ADD", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                addPerson(name);
            }
        });

        builder.setNegativeButton("CANCEL", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Custom positive button to prevent dialog from closing on empty input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name is required");
                etName.requestFocus();
            } else if (DataStore.getInstance().getPersonByName(name) != null) {
                etName.setError("Person already exists");
                etName.requestFocus();
            } else {
                addPerson(name);
                dialog.dismiss();
            }
        });
    }

    private void addPerson(String name) {
        Person person = new Person(name);
        DataStore.getInstance().addPerson(person);
        refreshData();
        Toast.makeText(this, name + " added successfully", Toast.LENGTH_SHORT).show();
    }

    private void showEditPersonDialog(Person person) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Person");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        etName.setText(person.getName());

        builder.setView(dialogView);

        builder.setPositiveButton("SAVE", null);
        builder.setNegativeButton("CANCEL", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Custom positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) {
                etName.setError("Name is required");
                etName.requestFocus();
            } else if (!newName.equals(person.getName()) &&
                    DataStore.getInstance().getPersonByName(newName) != null) {
                etName.setError("Name already exists");
                etName.requestFocus();
            } else {
                person.setName(newName);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void showDeleteConfirmation(Person person) {
        int expenseCount = DataStore.getInstance().getExpensesByPerson(person.getId()).size();

        String message;
        if (expenseCount > 0) {
            message = "Are you sure you want to delete " + person.getName() +
                    "? This will also delete " + expenseCount + " expense(s) associated with them.";
        } else {
            message = "Are you sure you want to delete " + person.getName() + "?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Person")
                .setMessage(message)
                .setPositiveButton("DELETE", (dialog, which) -> {
                    boolean success = DataStore.getInstance().removePerson(person.getId());
                    if (success) {
                        refreshData();
                        Toast.makeText(this, "Person deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // People Adapter
    private class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PersonViewHolder> {

        private List<Person> originalList;
        private List<Person> filteredList;
        private NumberFormat currencyFormat;

        public PeopleAdapter() {
            this.originalList = DataStore.getInstance().getPeople();
            this.filteredList = new ArrayList<>(originalList);
            this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        }

        public void refreshData() {
            this.originalList = DataStore.getInstance().getPeople();
            this.filteredList = new ArrayList<>(originalList);
            notifyDataSetChanged();
        }

        public void filter(String query) {
            filteredList.clear();

            if (query == null || query.isEmpty()) {
                filteredList.addAll(originalList);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                for (Person person : originalList) {
                    if (person.getName().toLowerCase().contains(lowerCaseQuery)) {
                        filteredList.add(person);
                    }
                }
            }

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_person, parent, false);
            return new PersonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
            Person person = filteredList.get(position);
            holder.bind(person);

            holder.btnEdit.setOnClickListener(v -> showEditPersonDialog(person));
            holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(person));

            holder.itemView.setOnClickListener(v -> {
                // Show person details or edit dialog
                showEditPersonDialog(person);
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class PersonViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvTotalPaid, tvInitial, tvExpenseCount;
            View colorIndicator;
            ImageButton btnEdit, btnDelete;

            public PersonViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPersonName);
                tvTotalPaid = itemView.findViewById(R.id.tvTotalPaid);
                tvInitial = itemView.findViewById(R.id.tvInitial);
                tvExpenseCount = itemView.findViewById(R.id.tvExpenseCount);
                colorIndicator = itemView.findViewById(R.id.colorIndicator);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            public void bind(Person person) {
                tvName.setText(person.getName());
                tvTotalPaid.setText("Paid: " + currencyFormat.format(person.getTotalPaid()));

                // Set initial
                if (!person.getName().isEmpty()) {
                    tvInitial.setText(String.valueOf(person.getName().charAt(0)).toUpperCase());
                }

                // Set color
                try {
                    colorIndicator.setBackgroundColor(
                            android.graphics.Color.parseColor(person.getColorHex()));
                } catch (Exception e) {
                    colorIndicator.setBackgroundColor(0xFF6200EE);
                }

                // Set expense count
                List<Expense> personExpenses = DataStore.getInstance().getExpensesByPerson(person.getId());
                String expenseText = personExpenses.size() + " expense" + (personExpenses.size() != 1 ? "s" : "");
                tvExpenseCount.setText(expenseText);
            }
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
}
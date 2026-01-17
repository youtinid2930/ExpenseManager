package com.example.expensemanager.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.Person;
import com.example.expensemanager.utils.DataStore;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerPerson, spinnerCategory;
    private EditText etAmount, etDescription;
    private TextView tvDate;
    private Button btnAddExpense;
    private TextInputLayout tilAmount, tilDescription;

    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        setSupportActionBar(findViewById(R.id.toolbar));

        // Enable back button for secondary activities
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initViews();
        setupDatePicker();
        updateSpinners();

        btnAddExpense.setOnClickListener(v -> addExpense());
    }


    private void initViews() {
        spinnerPerson = findViewById(R.id.spinnerPerson);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        tilAmount = findViewById(R.id.tilAmount);
        tilDescription = findViewById(R.id.tilDescription);

        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void setupDatePicker() {
        tvDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    tvDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateSpinners() {
        DataStore dataStore = DataStore.getInstance();
        List<Person> people = dataStore.getPeople();
        // Person spinner
        List<String> personNames = new ArrayList<>();
        personNames.add("Select person");
        if (people.isEmpty()) {
            HintAdapter personAdapter = new HintAdapter(this, personNames);
            spinnerPerson.setAdapter(personAdapter);
            spinnerPerson.setSelection(0);
            spinnerPerson.setEnabled(false);
            Toast.makeText(this, "Please add people first", Toast.LENGTH_SHORT).show();
            btnAddExpense.setEnabled(false);
            return;
        }

        for (Person person : people) {
            personNames.add(person.getName());
        }
        HintAdapter personAdapter = new HintAdapter(this, personNames);
        spinnerPerson.setAdapter(personAdapter);
        spinnerPerson.setSelection(0);

        // Category spinner
        String[] categories = {
                "Food & Dining", "Groceries", "Transportation", "Utilities",
                "Entertainment", "Shopping", "Healthcare", "Other"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void addExpense() {
        // Reset errors
        tilAmount.setError(null);
        tilDescription.setError(null);

        // Get values
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String selectedPersonName = spinnerPerson.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validation
        boolean hasError = false;

        if (amountStr.isEmpty()) {
            tilAmount.setError("Amount is required");
            hasError = true;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    tilAmount.setError("Amount must be greater than 0");
                    hasError = true;
                } else if (amount > 1000000) {
                    tilAmount.setError("Amount is too large");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("Invalid amount");
                hasError = true;
            }
        }

        if (description.isEmpty()) {
            tilDescription.setError("Description is required");
            hasError = true;
        } else if (description.length() > 100) {
            tilDescription.setError("Description too long");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Find person
        DataStore dataStore = DataStore.getInstance();
        Person selectedPerson = dataStore.getPersonByName(selectedPersonName);

        if (selectedPerson == null) {
            Toast.makeText(this, "Person not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create expense
        double amount = Double.parseDouble(amountStr);
        Date date = selectedDate.getTime();
        Expense expense = new Expense(selectedPerson, amount, description, date, category);

        dataStore.addExpense(expense);

        // Show success
        Toast.makeText(this,
                String.format("Added $%.2f expense for %s", amount, selectedPerson.getName()),
                Toast.LENGTH_SHORT).show();

        // Clear fields
        etAmount.setText("");
        etDescription.setText("");
        etDescription.clearFocus();
        etAmount.requestFocus();

        // Update total paid in UI if needed
        updateSpinners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class HintAdapter extends ArrayAdapter<String> {

        public HintAdapter(Context context, List<String> objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;

            if (position == 0) {
                // Gray out the hint text
                textView.setTextColor(Color.GRAY);
            }
            else {
                textView.setTextColor(Color.BLACK);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView textView = (TextView) view;

            if (position == 0) {
                textView.setTextColor(Color.GRAY);
            }

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            // Disable the first item (hint) from being selected
            return position != 0;
        }
    }
}
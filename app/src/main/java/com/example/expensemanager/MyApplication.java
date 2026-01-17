package com.example.expensemanager;

import android.app.Application;
import com.example.expensemanager.utils.DataStore;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize DataStore with application context
        DataStore.initialize(this);
    }
}
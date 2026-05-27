package com.example.smishingsmskn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://smishing-backend-en4u.onrender.com/"; // Replace with your server IP
    private static final int SMS_PERMISSION_CODE = 1;

    private ApiService api;
    private SmsAdapter adapter;
    private List<SmsResult> smsResults = new ArrayList<>();

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView overallResult;
    private TextView scanStatus;
    private TextView threatLevel;
    private TextView messagesHeader;
    private LinearLayout emptyState;
    private ExtendedFloatingActionButton fabScan;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SMS Security Scan");
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SmsAdapter(smsResults);
        recyclerView.setAdapter(adapter);

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        // Setup listeners
        swipeRefreshLayout.setOnRefreshListener(this::performSecurityScan);

        fabScan.setOnClickListener(v -> {
            swipeRefreshLayout.setRefreshing(true);
            performSecurityScan();
        });

        // Check permissions and auto-scan
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
        } else {
            // Auto-scan on first launch
            swipeRefreshLayout.post(() -> {
                swipeRefreshLayout.setRefreshing(true);
                performSecurityScan();
            });
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.smsRecyclerView);
        overallResult = findViewById(R.id.overallResult);
        scanStatus = findViewById(R.id.scanStatus);
        threatLevel = findViewById(R.id.threatLevel);
        messagesHeader = findViewById(R.id.messagesHeader);
        emptyState = findViewById(R.id.emptyState);
        fabScan = findViewById(R.id.fabScan);
    }

    private void performSecurityScan() {
        // Update UI to scanning state
        updateUIForScanning();

        List<String> messages = readAllSms();

        if (messages.isEmpty()) {
            handleEmptyMessages();
            return;
        }

        // Hide empty state and show content
        emptyState.setVisibility(View.GONE);
        messagesHeader.setVisibility(View.VISIBLE);

        int total = messages.size();
        int[] completed = {0};
        int[] spamCount = {0};

        smsResults = new ArrayList<>(Collections.nCopies(total, null));

        for (int i = 0; i < total; i++) {
            final int index = i;
            String msg = messages.get(i);
            SmsRequest request = new SmsRequest(msg);

            api.checkSpam(request).enqueue(new Callback<SpamResponse>() {
                @Override
                public void onResponse(Call<SpamResponse> call, Response<SpamResponse> response) {
                    String result;
                    String reason = "";

                    if (response.isSuccessful() && response.body() != null) {
                        SpamResponse resp = response.body();
                        if (resp.isSpam()) {
                            result = "Threat Detected";
                            reason = resp.getReason();
                            spamCount[0]++;
                        } else {
                            result = "Secure";
                            reason = "";
                        }
                    } else {
                        result = "Analysis Failed";
                        reason = "Server error";
                    }

                    smsResults.set(index, new SmsResult(msg, result, reason));
                    updateProgress(++completed[0], total, spamCount[0]);
                }

                @Override
                public void onFailure(Call<SpamResponse> call, Throwable t) {
                    smsResults.set(index, new SmsResult(msg, "Connection Error", "Unable to reach server"));
                    Log.e("SMS_SECURITY", "API request failed", t);
                    updateProgress(++completed[0], total, spamCount[0]);
                }
            });
        }
    }

    private void updateUIForScanning() {
        scanStatus.setText("Analyzing messages...");
        overallResult.setText("Security Scan in Progress");
        overallResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        threatLevel.setVisibility(View.GONE);
    }

    private void handleEmptyMessages() {
        Toast.makeText(this, "No messages available for analysis", Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
        emptyState.setVisibility(View.VISIBLE);
        messagesHeader.setVisibility(View.GONE);
        scanStatus.setText("Swipe down to analyze messages");
        overallResult.setText("No Scan Results");
        overallResult.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }

    private void updateProgress(int done, int total, int spamCount) {
        if (done == total) {
            adapter.setSmsList(smsResults);
            swipeRefreshLayout.setRefreshing(false);
            updateScanResults(spamCount, total);
        }
    }

    private void updateScanResults(int spamCount, int total) {
        // Update main result text
        if (spamCount == 0) {
            overallResult.setText("✓ All Clear");
            overallResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            scanStatus.setText("No threats detected in " + total + " message" + (total != 1 ? "s" : ""));
            threatLevel.setText("Your messages are secure");
            threatLevel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            overallResult.setText("⚠ " + spamCount + " Threat" + (spamCount != 1 ? "s" : "") + " Found");
            overallResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            scanStatus.setText("Scanned " + total + " message" + (total != 1 ? "s" : ""));

            String threatText = spamCount + " suspicious message" + (spamCount != 1 ? "s" : "") + " detected";
            threatLevel.setText(threatText);
            threatLevel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }

        threatLevel.setVisibility(View.VISIBLE);

        // Show completion message
        String resultMessage = spamCount == 0
                ? "Scan complete. No threats detected."
                : "Scan complete. Please review flagged messages.";
        Toast.makeText(this, resultMessage, Toast.LENGTH_SHORT).show();
    }

    private List<String> readAllSms() {
        List<String> messages = new ArrayList<>();

        // Check permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("SMS_SECURITY", "SMS permission not granted");
            return messages;
        }

        try {
            Uri uri = Uri.parse("content://sms/");
            String[] projection = {"body", "date", "type", "address"};

            // Filter: inbox (1) and sent (2) messages only
            String selection = "type = ? OR type = ?";
            String[] selectionArgs = {"1", "2"};

            Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "date DESC"
            );

            if (cursor != null) {
                Log.d("SMS_SECURITY", "Found " + cursor.getCount() + " SMS messages");

                while (cursor.moveToNext()) {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                    if (body != null && !body.trim().isEmpty()) {
                        messages.add(body);
                        Log.d("SMS_SECURITY", "Type: " + type + ", From: " + address);
                    }
                }
                cursor.close();

                Log.d("SMS_SECURITY", "Total messages collected: " + messages.size());
            } else {
                Log.e("SMS_SECURITY", "Cursor is null - permission denied");
            }

        } catch (SecurityException e) {
            Log.e("SMS_SECURITY", "Permission denied", e);
            runOnUiThread(() ->
                    Toast.makeText(this, "SMS permission required for security scan", Toast.LENGTH_LONG).show()
            );
        } catch (Exception e) {
            Log.e("SMS_SECURITY", "Error reading SMS", e);
            runOnUiThread(() ->
                    Toast.makeText(this, "Error accessing messages", Toast.LENGTH_SHORT).show()
            );
        }

        return messages;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Starting security scan...", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(true);
                performSecurityScan();
            } else {
                Toast.makeText(this, "SMS permission is required for security analysis", Toast.LENGTH_LONG).show();
                emptyState.setVisibility(View.VISIBLE);
            }
        }
    }
}
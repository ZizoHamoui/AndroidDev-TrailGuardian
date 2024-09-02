package com.example.trailguardian;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView trailsRecyclerView;
    private HistoryAdapter adapter;
    private List<Trail> trailList;
    private Button returnB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        trailsRecyclerView = findViewById(R.id.routesRecyclerView);
        trailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        trailList = new ArrayList<>();
        loadTrails();

        returnB = findViewById(R.id.returnB);
        returnB.setOnClickListener(this);

        adapter = new HistoryAdapter(trailList);
        trailsRecyclerView.setAdapter(adapter);
    }

    // Gets trails from shared preferences and adds to the recyler item list
    private void loadTrails() {
        SharedPreferences prefs = getSharedPreferences("Trails", MODE_PRIVATE);
        Map<String, ?> trails = prefs.getAll();

        for (Map.Entry<String, ?> entry : trails.entrySet()) {
            String savedValue = (String) entry.getValue();
            String[] parts = savedValue.split(",");
            String date = parts[0];
            double lat = Double.parseDouble(parts[1]);
            double lng = Double.parseDouble(parts[2]);

            trailList.add(new Trail(date, lat, lng));
        }
    }

    // Button to return to previous page
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.returnB) {
            finish();
        }
    }
}

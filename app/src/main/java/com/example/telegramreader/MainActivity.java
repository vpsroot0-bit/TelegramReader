package com.example.telegramreader;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText edtChannel;
    private Button btnAdd;
    private RecyclerView recyclerChannels;
    private ChannelAdapter adapter;
    private List<String> channels = new ArrayList<>();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtChannel = findViewById(R.id.edtChannel);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerChannels = findViewById(R.id.recyclerChannels);

        recyclerChannels.setLayoutManager(new LinearLayoutManager(this));
        loadChannels();

        adapter = new ChannelAdapter(channels, channel -> {
            // با کلیک روی کانال، صفحه پیام‌ها رو باز کن
            MessagesActivity.start(this, channel);
        }, channel -> {
            // حذف کانال
            channels.remove(channel);
            saveChannels();
            adapter.notifyDataSetChanged();
        });
        recyclerChannels.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String name = edtChannel.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "نام کانال رو وارد کن", Toast.LENGTH_SHORT).show();
                return;
            }
            if (channels.contains(name)) {
                Toast.makeText(this, "این کانال از قبل هست", Toast.LENGTH_SHORT).show();
                return;
            }
            channels.add(name);
            saveChannels();
            adapter.notifyDataSetChanged();
            edtChannel.setText("");
        });
    }

    private void loadChannels() {
        String json = getPreferences(MODE_PRIVATE).getString("channels", "[]");
        Type listType = new TypeToken<List<String>>(){}.getType();
        channels = gson.fromJson(json, listType);
        if (channels == null) channels = new ArrayList<>();
    }

    private void saveChannels() {
        String json = gson.toJson(channels);
        getPreferences(MODE_PRIVATE).edit().putString("channels", json).apply();
    }
}

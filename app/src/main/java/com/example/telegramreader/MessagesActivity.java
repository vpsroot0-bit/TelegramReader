package com.example.telegramreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MessagesActivity extends AppCompatActivity {

    // 👇 این لینک رو با آدرس Web App که از Google Apps Script گرفتی، جایگزین کن
    private static final String PROXY_URL = "https://script.google.com/macros/s/AKfycbxtU1ZV8u7g9Ghk85odV_Ia-mT4aj9n9Jdn6m-V-SckG4m4XJYi3lk-SfHEO4X7Ryh1ig/exec";

    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private String channel;
    private Gson gson = new Gson();

    public static void start(Context context, String channel) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra("channel", channel);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        channel = getIntent().getStringExtra("channel");
        setTitle(channel);

        recyclerMessages = findViewById(R.id.recyclerMessages);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messages);
        recyclerMessages.setAdapter(adapter);

        loadMessages("0");
    }

    private void loadMessages(String before) {
        new FetchMessagesTask(channel, before).execute();
    }

    private class FetchMessagesTask extends AsyncTask<Void, Void, String> {

        private String chid, before;

        FetchMessagesTask(String chid, String before) {
            this.chid = chid;
            this.before = before;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String requestUrl = PROXY_URL + "?action=messages&chid=" + chid + "&before=" + before;
                URL url = new URL(requestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            if (json == null) {
                Toast.makeText(MessagesActivity.this, "خطا در اتصال به تلگرام", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Type listType = new TypeToken<List<Message>>() {}.getType();
                List<Message> newMessages = gson.fromJson(json, listType);
                if (newMessages != null) {
                    messages.clear();
                    messages.addAll(newMessages);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MessagesActivity.this, "پیامی یافت نشد", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MessagesActivity.this, "خطا در پردازش اطلاعات", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

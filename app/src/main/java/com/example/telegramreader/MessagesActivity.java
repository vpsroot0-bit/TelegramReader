package com.example.telegramreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private String channel;

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

    // AsyncTask برای دریافت پیام‌ها در پس‌زمینه
    private class FetchMessagesTask extends AsyncTask<Void, Void, String> {

        private String chid, before;

        FetchMessagesTask(String chid, String before) {
            this.chid = chid;
            this.before = before;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String[] domains = {"safebrowsing.google.com", "images.google.com", "maps.google.com",
                        "news.google.com", "scholar.google.com", "mail.google.com", "drive.google.com"};
                String domain = domains[new Random().nextInt(domains.length)];
                String urlStr = "https://" + domain + "/s/" + chid;
                if (!before.equals("0")) {
                    urlStr += "?before=" + before;
                }
                String sep = urlStr.contains("?") ? "&" : "?";
                urlStr += sep + "_x_tr_sl=el&_x_tr_tl=en&_x_tr_hl=en&_x_tr_pto=wapp";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Host", "t-me.translate.goog");
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
        protected void onPostExecute(String html) {
            if (html == null) {
                Toast.makeText(MessagesActivity.this, "خطا در اتصال به تلگرام", Toast.LENGTH_SHORT).show();
                return;
            }
            // Parse HTML and extract messages
            parseAndShow(html);
        }
    }

    private void parseAndShow(String html) {
        // خیلی ساده: هر تگ article رو پیدا می‌کنیم، متن داخل "tgme_widget_message_text" رو می‌گیریم.
        messages.clear();
        String[] parts = html.split("<article");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            Message msg = new Message();
            // text
            int idxText = part.indexOf("tgme_widget_message_text");
            if (idxText != -1) {
                int start = part.indexOf(">", idxText) + 1;
                int end = part.indexOf("</div>", start);
                if (end != -1) {
                    String text = part.substring(start, end);
                    // حذف تگ‌های HTML
                    text = text.replaceAll("<[^>]+>", "").replace("&nbsp;", " ");
                    msg.text = text.trim();
                }
            }
            // date
            int idxTime = part.indexOf("<time datetime=\"");
            if (idxTime != -1) {
                int start = part.indexOf("\"", idxTime) + 1;
                int end = part.indexOf("\"", start);
                msg.date = part.substring(start, end);
            }
            if (msg.text != null && !msg.text.isEmpty()) {
                messages.add(msg);
            }
        }
        adapter.notifyDataSetChanged();
    }
}

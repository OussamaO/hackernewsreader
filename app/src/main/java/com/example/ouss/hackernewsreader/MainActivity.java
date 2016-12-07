package com.example.ouss.hackernewsreader;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String URL_IDS = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String URL_ART_START = "https://hacker-news.firebaseio.com/v0/item/";
    final String URL_ART_END = ".json";

    ListView list;
    TextView progress;

    ArrayAdapter<String> adapter;
    ArrayList<String> titleLijst;
    ArrayList<String> urlList;


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.listView);
        progress = (TextView) findViewById(R.id.progress);

        titleLijst = new ArrayList<>();
        urlList = new ArrayList<>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titleLijst);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(urlList.get(position)));
                startActivity(browser);
            }
        });

        new DownloadTask().execute();
    }

    class DownloadTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String resArray = "";
            HttpURLConnection connection = null;
            InputStream input = null;
            InputStreamReader reader = null;

            try {
                URL urlIDS = new URL(URL_IDS);
                connection = (HttpURLConnection) urlIDS.openConnection();
                input = connection.getInputStream();
                reader = new InputStreamReader(input);

                int data = reader.read();
                while (data != -1) {
                    char c = (char) data;
                    resArray += c;
                    data = reader.read();
                }

                JSONArray jsonArray = new JSONArray(resArray);
                int index = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    String id = jsonArray.getString(i);
                    URL urlArticle = new URL(URL_ART_START + id + URL_ART_END);
                    connection = (HttpURLConnection) urlArticle.openConnection();
                    input = connection.getInputStream();
                    reader = new InputStreamReader(input);
                    String jsonObjectString = "";

                    data = reader.read();
                    while (data != -1) {
                        char c = (char) data;
                        jsonObjectString += c;
                        data = reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(jsonObjectString);
                    if (jsonObject.has("title") && jsonObject.has("url")) {
                        titleLijst.add(jsonObject.getString("title"));
                        urlList.add(jsonObject.getString("url"));
                        publishProgress("Downloaded: " + index + " articles");
                        index ++;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (input != null) {
                    try {
                        input.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String update = values[0];
            progress.setText(update);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            progress.setVisibility(View.GONE);
        }
    }
}
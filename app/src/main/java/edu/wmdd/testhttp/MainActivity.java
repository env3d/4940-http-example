package edu.wmdd.testhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Example app for OkHttp, you will need to first add the following
 * to your app/build.gradle file:
 *
 * implementation("com.squareup.okhttp3:okhttp:4.2.0")
 *
 * You also want to add the following permission in AndroidManifest.xml
 *
 * <uses-permission android:name="android.permission.INTERNET"/>
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener((view)->{

            // Here we instantiate the OkHttpClient and
            // GET the appropriate .json content from reddit frontpage
            OkHttpClient client = new OkHttpClient();
            String url;
            url = "https://www.reddit.com/.json";
            Request req = new Request.Builder().url(url).build();

            // We can't use blocking network calls in the UI thread,
            // so we need to create a new thread to handle the
            // network execution.

            // In production, you want to read
            // https://developer.android.com/guide/components/processes-and-threads
            // and use AsyncTask intead of Thread
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        Response response = client.newCall(req).execute();
                        String text = response.body().string();
                        Log.d("response", text );

                        JSONObject object = (JSONObject) new JSONTokener(text).nextValue();

                        JSONArray listings = object.getJSONObject("data").getJSONArray("children");

                        ArrayList<String> titles = new ArrayList<>(listings.length());

                        for (int i =0; i<listings.length(); i++) {
                            JSONObject item = listings.getJSONObject(i);
                            titles.add(item.getJSONObject("data").getString("title"));
                        }

                        // We can't update UI on a different thread, so we need to send
                        // the processing back to the UI thread via runOnUiThread method
                        runOnUiThread(()->{
                            String result = titles.stream().reduce("", (a, b) -> a += "\n" + b);
                            ((TextView)findViewById(R.id.textView)).setText(result);
                        });
                    } catch (IOException | JSONException e) {
                        runOnUiThread(()->{
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        });

                    }
                }
            };

            t.start();

        });
    }
}

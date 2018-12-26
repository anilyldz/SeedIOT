package com.seediot.iot.seediot;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "UsingThingspeakAPI";
    private static final String THINGSPEAK_CHANNEL_ID = "641691";
    private static final String THINGSPEAK_API_KEY = "ZD2WRTXS79GMFEPF"; //GARBAGE KEY
    private static final String THINGSPEAK_API_KEY_STRING = "THINGSPEAK READ API KEY";
    /* Be sure to use the correct fields for your own app*/
    private static final String THINGSPEAK_TEMP = "field1";
    private static final String THINGSPEAK_HUMI = "field2";
    private static final String THINGSPEAK_TEMP2 = "field3";
    private static final String THINGSPEAK_HUMI2 = "field4";
    private static final String THINGSPEAK_CHANNEL_URL = "https://api.thingspeak.com/channels/";
    private static final String THINGSPEAK_FEEDS_LAST = "/feeds/last?";
    private static final String THINGSPEAK_FEEDS_DAYS = "/feeds.json?start=2018-12-20%2000:00:00&end=2018-12-22%2000:00:00";
    TextView temp_tv, humi_tv, info_tv, temp_tv2, humi_tv2, advice_tv, avg_humi_n1_tv, avg_temp_n1_tv, avg_humi_n2_tv, avg_temp_n2_tv;
    Button get_btn ;
    NotificationCompat.Builder notification;
    int avg_humi_n1 = 0, avg_temp_n1=0, avg_humi_n2=0, avg_temp_n2=0;

    LineChart temp_n1_lc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp_tv= findViewById(R.id.temp_tv);
        humi_tv= findViewById(R.id.humi_tv);
        info_tv= findViewById(R.id.info_tv);
        temp_tv2 = findViewById(R.id.temp_tv2);
        humi_tv2 = findViewById(R.id.humi_tv2);
        advice_tv = findViewById(R.id.advice_tv);
        get_btn= findViewById(R.id.get_btn);
        avg_temp_n1_tv = findViewById(R.id.avg_temp_n1_tv);
        avg_humi_n1_tv = findViewById(R.id.avg_humi_n1_tv);
        avg_temp_n2_tv = findViewById(R.id.avg_temp_n2_tv);
        avg_humi_n2_tv = findViewById(R.id.avg_humi_n2_tv);
        temp_n1_lc = findViewById(R.id.temp_n1_lc);
        humi_tv.setText("");
        temp_tv.setText("");
        info_tv.setText("");
        temp_tv2.setText("");
        humi_tv2.setText("");
        advice_tv.setText("");
        avg_temp_n1_tv.setText("");
        avg_humi_n1_tv.setText("");
        avg_temp_n2_tv.setText("");
        avg_humi_n2_tv.setText("");

        temp_n1_lc.setDragEnabled(true);
        temp_n1_lc.setScaleEnabled(false);
        temp_n1_lc.getDescription().setText(" ");
        temp_n1_lc.setNoDataText(" ");

        get_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    new FetchThingspeakTask2().execute();
                    new FetchThingspeakTask().execute();

                }
                catch(Exception e){
                    info_tv.setText("Problem has Occurred");
                    Log.e("ERROR", e.getMessage(), e);
                }
            }
        });

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

    }


    class FetchThingspeakTask extends AsyncTask<Void, Void, String> {
        protected void onPreExecute() {
            info_tv.setText("Fetching Data from Server. Please Wait...");
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(THINGSPEAK_CHANNEL_URL + THINGSPEAK_CHANNEL_ID +
                        THINGSPEAK_FEEDS_LAST + THINGSPEAK_API_KEY_STRING + "=" +
                        THINGSPEAK_API_KEY + "");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                info_tv.setText("A problem has Occurred");
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                info_tv.setText("Response is empty");
                return;
            }
            try {
                JSONObject channel = (JSONObject) new JSONTokener(response).nextValue();
                String humi = channel.getString(THINGSPEAK_HUMI);
                String temp = channel.getString(THINGSPEAK_TEMP);
                String humi2 = channel.getString(THINGSPEAK_HUMI2);
                String temp2 = channel.getString(THINGSPEAK_TEMP2);
                Boolean status = true;

                if (temp!=null){
                    temp_tv.setText(temp);
                }else{
                    temp_tv.setText("Error Occurred");
                    sendNotification("Error Occurred on Node 1 Temperature Sensor");
                    status = false;
                }

                if (humi!=null){
                    humi_tv.setText(humi);
                }else{
                    humi_tv.setText("Error Occurred");
                    sendNotification("Error Occurred on Node 1 Humidity Sensor");
                    status = false;
                }

                if (temp2!=null){
                    temp_tv2.setText(temp2);
                }else{
                    temp_tv2.setText("Error Occurred");
                    sendNotification("Error Occurred on Node 2 Temperature Sensor");
                    status = false;
                }

                if (humi2!=null){
                    humi_tv2.setText(humi2);
                }else{
                    humi_tv2.setText("Error Occurred");
                    sendNotification("Error Occurred on Node 2 Humidity Sensor");
                    status = false;
                }

                advice_tv.setText(" ");

                if (temp!=null){
                    if (Integer.parseInt(temp)<avg_temp_n1){
                        advice_tv.append("Node 1 Temperature is below than average \n");
                        sendNotification("Node 1 Temperature is below than average");
                    }
                }

                if (humi!=null){
                    if (Integer.parseInt(humi)<avg_humi_n1){
                        advice_tv.append(" Node 1 Humidty is below than average\n");
                        sendNotification("Node 1 Humidty is below than average");
                    }
                }

                if (temp2!=null){
                    if (Integer.parseInt(temp2)<avg_temp_n2){
                        advice_tv.append("Node 2 Temperature is below than average \n");
                        sendNotification("Node 2 Temperature is below than average");
                    }
                }

                if (humi2!=null){
                    if (Integer.parseInt(humi2)<avg_humi_n2){
                        advice_tv.append(" Node 2 Humidty is below than average \n");
                        sendNotification("Node 2 Humidty is below than average");
                    }
                }

                if (status){
                    info_tv.setText("Fetching Data from Server is Successful");
                }else{
                    info_tv.setText("Fetching Data from Server is Unsuccessful");
                }

            }catch (Exception e){
                info_tv.setText("Fetching Data from Server is Unsuccessful");
            }


        }
    }

    class FetchThingspeakTask2 extends AsyncTask<Void, Void, String> {
        protected void onPreExecute() {
            info_tv.setText("Fetching Data from Server. Please Wait...");
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(THINGSPEAK_CHANNEL_URL + THINGSPEAK_CHANNEL_ID +
                        THINGSPEAK_FEEDS_DAYS + THINGSPEAK_API_KEY_STRING + "=" +
                        THINGSPEAK_API_KEY + "");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                info_tv.setText("A problem has Occurred");
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                info_tv.setText("Response is empty");
                return;
            }
            try {

                JSONObject channel = new JSONObject(response);    // create JSON obj from string
                JSONArray channelArray = channel.getJSONArray("feeds");    // this will return correct
                ArrayList<Entry> yValues = new ArrayList<>();

                for (int i=0; i<channelArray.length(); i++){
                    JSONObject Jasonobject = channelArray.getJSONObject(i);
                    String humi_n1 = Jasonobject.getString(THINGSPEAK_HUMI);
                    String temp_n1 = Jasonobject.getString(THINGSPEAK_TEMP);
                    String humi_n2 = Jasonobject.getString(THINGSPEAK_HUMI2);
                    String temp_n2 = Jasonobject.getString(THINGSPEAK_TEMP2);

                    if (humi_n1!=null){
                        avg_humi_n1 = avg_humi_n1 + Integer.parseInt(humi_n1);
                    }
                    if (temp_n1!=null){
                        avg_temp_n1 = avg_temp_n1 + Integer.parseInt(temp_n1);
                    }
                    if (humi_n2!=null){
                        avg_humi_n2 = avg_humi_n2 + Integer.parseInt(humi_n2);
                    }
                    if (temp_n2!=null){
                        avg_temp_n2 = avg_temp_n2 + Integer.parseInt(temp_n2);
                    }

                    yValues.add(new Entry(i,Float.valueOf(temp_n1)));

                }
                avg_humi_n1 = avg_humi_n1/channelArray.length();
                if (avg_humi_n1>0){
                    avg_humi_n1_tv.setText(String.valueOf(avg_humi_n1));
                }else{
                    avg_humi_n1_tv.setText("Error Occurred");
                }

                avg_temp_n1 = avg_temp_n1/channelArray.length();
                if (avg_temp_n1>0){
                    avg_temp_n1_tv.setText(String.valueOf(avg_temp_n1));
                }else {
                    avg_temp_n1_tv.setText("Error Occurred");
                }

                avg_humi_n2 = avg_humi_n2/channelArray.length();
                if (avg_humi_n2>0){
                    avg_humi_n2_tv.setText(String.valueOf(avg_humi_n2));
                }else {
                    avg_humi_n2_tv.setText("Error Occurred");
                }

                avg_temp_n2 = avg_temp_n2/channelArray.length();
                if (avg_temp_n2>0){
                    avg_temp_n2_tv.setText(String.valueOf(avg_temp_n2));
                }else {
                    avg_temp_n2_tv.setText("Error Occurred");
                }

                LineDataSet set1 = new LineDataSet(yValues, "Node 1 Temperature");
                set1.setFillAlpha(110);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);

                LineData data = new LineData(dataSets);
                temp_n1_lc.setData(data);

                info_tv.setText("Fetching Data from Server is Successful");

            }catch (Exception e){
                info_tv.setText("Fetching Data from Server is Unsuccessful");
            }


        }
    }

    public void sendNotification (String message){
        notification.setSmallIcon(R.drawable.seediot_48);
        notification.setTicker("Bu ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("There is a problem on the field");
        notification.setContentText(message);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Random random = new Random();
        int uniqueID = random.nextInt(9999 - 1000) + 1000;
        nm.notify(uniqueID, notification.build());

    }
}

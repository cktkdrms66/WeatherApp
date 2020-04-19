package com.weather.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String urlString = "http://www.kma.go.kr/DFSROOT/POINT/DATA/";
    public final static String urlStringWeather = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst";
    public final static String APIKey = "9jKgxfYDqIJMIS0VRq%2B4ybSb7TWTz0tmSaJuXmHXuh4LBt%2BHpTV9LWxEGK9IYLXD1ZBqxUCQQ2TLEOWKttkuuw%3D%3D";

    private JSONArray[] jsonArrays = new JSONArray[3];
    private int[] codes = new int[3];
    private List<String>[] lists = new ArrayList[3];
    private ArrayAdapter<String>[] arrayAdapters = new ArrayAdapter[3];

    private Spinner[] spinners = new Spinner[3];
    private TextView textView, textView_title;
    private ImageView imageView;
    private Button button, button_my;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if(!task.isSuccessful()){
//                    Log.w("FCM Log", "failed", task.getException());
//                    return;
//                }
//                String string = task.getResult().getToken();
//                Log.d("FCM Log", "FCM 토큰" + string);
//                Toast.makeText(getApplicationContext(),string, Toast.LENGTH_SHORT).show();
//            }
//        });


        button = findViewById(R.id.button);
        button_my = findViewById(R.id.button2);

        textView_title = findViewById(R.id.textView3);
        spinners[0] = findViewById(R.id.spinner);
        spinners[1] = findViewById(R.id.spinner2);
        spinners[2] = findViewById(R.id.spinner3);

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        lists[0] = new ArrayList<>();
        lists[1] = new ArrayList<>();
        lists[2] = new ArrayList<>();

        arrayAdapters[0] = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lists[0]);
        arrayAdapters[1] = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lists[1]);
        arrayAdapters[2] = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lists[2]);

        spinners[0].setAdapter(arrayAdapters[0]);
        spinners[1].setAdapter(arrayAdapters[1]);
        spinners[2].setAdapter(arrayAdapters[2]);

        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        textView_title.setText("지역을 선택해주세요");

        new BackTask().execute("0");

        final Geocoder geocoder = new Geocoder(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int pos = spinners[2].getSelectedItemPosition();
                    JSONObject jsonObject = jsonArrays[2].getJSONObject(pos);
                    int x = Integer.parseInt(jsonObject.get("x").toString());
                    int y = Integer.parseInt(jsonObject.get("y").toString());
                    new BackTaskWeather().execute(x, y);

                    String text = "대한민국 " + spinners[0].getSelectedItem().toString()
                            + " " + spinners[1].getSelectedItem().toString()
                            + " " + spinners[2].getSelectedItem().toString();
                    textView_title.setText(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        button_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                            0 );
                }else{
                    List<Address> addresses = null;
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location == null){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                    }
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    try{
                        addresses = geocoder.getFromLocation(latitude, longitude, 10);
                        textView_title.setText(addresses.get(0).getAddressLine(0));
                        String[] splits = addresses.get(0).getAddressLine(0).split(" ");
                        new BackTaskMy().execute(splits);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }




            }
        });

        spinners[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    int pos = spinners[0].getSelectedItemPosition();
                    JSONObject jsonObject = jsonArrays[0].getJSONObject(pos);
                    new BackTask().execute("1",jsonObject.get("code").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinners[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    int pos = spinners[1].getSelectedItemPosition();
                    JSONObject jsonObject = jsonArrays[1].getJSONObject(pos);
                    new BackTask().execute("2",jsonObject.get("code").toString());


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    class BackTaskMy extends AsyncTask<String, Void, Integer[]>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer[] doInBackground(String... strings) {
            if(!strings[0].equals("대한민국")){
                return null;
            }
            String value0 = strings[1];
            String value1 = strings[2];
            String value2 = strings[3];
            String code0 = "1";
            String code1 = "1";
            int x;
            int y;

            try{
                for(int i = 0; i < jsonArrays[0].length(); i++){
                    JSONObject jsonObject = jsonArrays[0].getJSONObject(i);
                    String value = jsonObject.get("value").toString();
                    if(value.equals(value0)){
                        code0 = jsonObject.get("code").toString();
                        break;
                    }
                }

                String urlStr = urlString + "mdl." + code0 + ".json.txt";
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String temp;
                StringBuffer stringBuffer = new StringBuffer();
                while((temp = bufferedReader.readLine()) != null){
                    stringBuffer.append(temp+"\n");
                }
                JSONArray jsonArray = new JSONArray(stringBuffer.toString().trim());

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String value = jsonObject.get("value").toString();
                    if(value.equals(value1)){
                        code1 = jsonObject.getString("code");
                        break;
                    }
                }

                String urlStr1 = urlString + "leaf." + code1 + ".json.txt";
                URL url1 = new URL(urlStr1);
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                InputStream inputStream1 = connection1.getInputStream();
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1, "UTF-8"));

                String temp1;
                StringBuffer stringBuffer1 = new StringBuffer();
                while((temp1 = bufferedReader1.readLine()) != null){
                    stringBuffer1.append(temp1+"\n");
                }
                JSONArray jsonArray1 = new JSONArray(stringBuffer1.toString().trim());

                JSONObject object = jsonArray1.getJSONObject(0);
                x = object.getInt("x");
                y = object.getInt("y");

                for(int i = 0; i < jsonArray1.length(); i++){
                    JSONObject jsonObject = jsonArray1.getJSONObject(i);
                    String value = jsonObject.getString("value");
                    if(value.equals(value2)){
                        x = jsonObject.getInt("x");
                        y = jsonObject.getInt("y");
                        break;
                    }
                }
                System.out.println(x + ", " + y + "xy");
                return new Integer[]{x,y};

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer[] integers) {
            if(integers == null){
                Toast.makeText(getApplicationContext(),"해당 위치는 서비스를 제공하지 않습니다.", Toast.LENGTH_LONG).show();
                imageView.setImageResource(R.drawable.ic_android);
                textView.setText("10000℃");
            }else{
                new BackTaskWeather().execute(integers[0], integers[1]);
            }

        }
    }
    class BackTaskWeather extends AsyncTask<Integer, Integer, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            int x = integers[0];
            int y = integers[1];

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Calendar nowCalendar = getCalendar(calendar);
            String today = new SimpleDateFormat("yyyyMMdd").format(nowCalendar.getTime());
            String time = nowCalendar.get(Calendar.HOUR_OF_DAY)+"";
            if(time.length() == 1){
                time = "0" + time;
            }
            String urlString = urlStringWeather + "?serviceKey=" + APIKey
                    + "&pageNo=" + 1
                    + "&numOfRows=" + 10
                    + "&dataType=" + "JSON"
                    + "&base_date=" + today
                    + "&base_time=" +  time + "00"
                    + "&nx=" + x
                    + "&ny=" + y;

            try{
                System.out.println(urlString);
                URL url = new URL(urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while((temp = bufferedReader.readLine()) != null){
                    stringBuilder.append(temp + "\r\n");
                }
                JSONObject jsonObject = new JSONObject(stringBuilder.toString().trim());
                JSONObject response = jsonObject.getJSONObject("response");
                JSONObject body = response.getJSONObject("body");
                JSONObject items = body.getJSONObject("items");
                JSONArray item = items.getJSONArray("item");

                int skyType = 0;
                int rainType = 0;
                int temperature = 0;
                for(int i = 0; i < body.getInt("numOfRows"); i++){
                    JSONObject jsonObject1 = item.getJSONObject(i);
                    String str = jsonObject1.getString("category");
                    if(str.equals("SKY")){
                        skyType = Integer.parseInt(jsonObject1.getString("fcstValue"));
                    }else if(str.equals("T3H")){
                        temperature = Integer.parseInt(jsonObject1.getString("fcstValue"));
                    }else if(str.equals("PTY")){
                        rainType = Integer.parseInt(jsonObject1.getString("fcstValue"));
                    }
                }
                System.out.println("skytype rain temp" + skyType + ", " + rainType + "," + temperature);
                publishProgress(skyType, rainType, temperature, Integer.parseInt(time));

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int image = R.drawable.ic_iconmonstr_weather_sunny;
            int time = values[3];
            switch (values[0]){
                case 1:
                    image = R.drawable.ic_iconmonstr_weather_sunny;
                    if(time < 600 || time > 1900){
                        image = R.drawable.ic_iconmonstr_weather_night;
                    }
                    break;
                case 3:
                    image = R.drawable.ic_iconmonstr_weather_little_cloudy;
                    if(time < 600 || time > 1900){
                        image = R.drawable.ic_iconmonstr_weather_nithgt_and_little_cloud;
                    }
                    break;
                case 4:
                    image = R.drawable.ic_iconmonstr_weather_cloud;
                    break;
                default:
                    break;
            }
            switch (values[1]){
                case 0:
                    break;
                case 1:
                    image = R.drawable.ic_iconmonstr_weather_rain;
                    break;
                case 2:
                    image = R.drawable.ic_iconmonstr_weather_rain_and_snow;
                    break;
                case 3:
                    image = R.drawable.ic_iconmonstr_weather_snow;
                    break;
                case 4:
                    image = R.drawable.ic_iconmonstr_weather_rain;
                    break;
                default:
                    break;
            }

            imageView.setImageResource(image);
            textView.setText(values[2] + "℃");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    class BackTask extends AsyncTask<String, Void, Integer>{
        String target;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            target = strings[0];


            try{
                int pos = Integer.parseInt(target);
                String urlStr;

                if(target.equals("0")){
                    urlStr = urlString + "top.json.txt";
                }else if(target.equals("1")){
                    urlStr = urlString + "mdl." + strings[1] + ".json.txt";
                }else{
                    urlStr = urlString + "leaf." + strings[1] + ".json.txt";
                }

                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line+"\r\n");
                }
                inputStream.close();
                bufferedReader.close();

                jsonArrays[pos] = new JSONArray(stringBuilder.toString().trim());
                lists[pos].clear();
                for(int i = 0; i < jsonArrays[pos].length(); i++){
                    lists[pos].add(jsonArrays[pos].getJSONObject(i).get("value").toString());
                }


                return pos;

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            arrayAdapters[integer].notifyDataSetChanged();
            if(integer == 1){
                spinners[1].setSelection(0);
                try {
                    int pos = 0;
                    JSONObject jsonObject = jsonArrays[1].getJSONObject(pos);
                    new BackTask().execute("2",jsonObject.get("code").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private Calendar getCalendar(Calendar calendar){
        int t = calendar.get(Calendar.HOUR_OF_DAY);
        if(t < 2){
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
        }else{
            calendar.set(Calendar.HOUR_OF_DAY, t - (t + 1) % 3);
        }
        return calendar;
    }

}

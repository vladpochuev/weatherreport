package space.lobanov.weatherreport;



import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button main_btn;
    private TextView result_info_temp, result_info_weather;
    private ImageView icon;

    private int responseCode = 0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //скрываем нижнюю панель навигации
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // появляется поверх приложения и исчезает

        user_field = findViewById(R.id.user_field);
        main_btn = findViewById(R.id.main_btn);
        result_info_temp = findViewById(R.id.result_info_temp);
        result_info_weather = findViewById(R.id.result_info_weather);
        icon = findViewById(R.id.icon);

        main_btn.setOnClickListener(view -> {
            if(user_field.getText().toString().trim().equals("")){
                Toast.makeText(this,R.string.no_user_input,Toast.LENGTH_LONG).show();
            } else {
                icon.setBackgroundResource((int)Color.TRANSPARENT);

                Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                bm.eraseColor(Color.TRANSPARENT);
                icon.setImageBitmap(bm);

                String city = user_field.getText().toString();
                String key = "205fcde27a17674e6802e322f472b29d";
                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";

                new GetURLData().execute(url);
            }
        });
    }
    private class GetURLData extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result_info_temp.setText(R.string.waiting);
            result_info_weather.setText("");
        }

        @Override
        protected String doInBackground(String ... strings) {
            HttpURLConnection connection;
            BufferedReader reader;
            InputStream stream;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if(responseCode>400){
                    stream = connection.getErrorStream();
                } else {
                    stream = connection.getInputStream();
                }

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder builder = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null){
                    builder.append(line).append("\n");
                }

                connection.disconnect();
                reader.close();

                return builder.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                if(responseCode > 400){
                    int message;
                    if(responseCode==404){
                        message = R.string.city_not_found;
                    } else if(responseCode==401 || responseCode==429){
                        message = R.string.try_later;
                    } else{
                        message = R.string.server_error;
                    }

                    result_info_temp.setText("");
                    Toast toast = Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
                    toast.show();

                    responseCode = 0;

                    return;
                }

                JSONObject jsonObject = new JSONObject(result);

                result_info_temp.setText(jsonObject.getJSONObject("main").getDouble("temp") + "℃");

                result_info_weather.setText(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description"));

                String iconId = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");

                String iconURL = "http://openweathermap.org/img/wn/" + iconId + "@2x.png";

                icon.setBackgroundResource(R.drawable.rounded);

                Picasso.get().load(iconURL).into(icon);

                responseCode = 0;

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
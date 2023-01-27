package space.lobanov.weatherreport;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button main_btn;
    private TextView result_info;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //скрываем нижнюю панель навигации
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // появляется поверх приложения и исчезает

        user_field = findViewById(R.id.user_field);
        main_btn = findViewById(R.id.main_btn);
        result_info = findViewById(R.id.result_info);

        main_btn.setOnClickListener(view -> {
            if(user_field.getText().toString().trim().equals("")){
                Toast.makeText(this,R.string.no_user_input,Toast.LENGTH_LONG).show();
            } else {
                String city = user_field.getText().toString();
                String key = "205fcde27a17674e6802e322f472b29d";
                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ua";

                new GetURLData().execute(url);
            }
        });
    }
    private class GetURLData extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Ожидайте...");
        }

        @Override
        protected String doInBackground(String ... strings) {
            HttpURLConnection connection;
            BufferedReader reader;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }

                connection.disconnect();
                reader.close();

                return buffer.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                result_info.setText("Температура: " + jsonObject.getJSONObject("main").getDouble("temp"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
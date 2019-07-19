package es.weatheraplikacioni.androidweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    //We have to call a URL to get the weather data
    private final static String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final static String API_KEY = "4985cef944555b5dd6bd6496f8f519c7";

    private Button button;
    private Button button1;
    private TextView temperature;
    private TextView temp_max;
    private TextView temp_min;
    private TextView cityView;
    private TextView humidity;
    private TextView pressure;
    private EditText cityInput;
    private ProgressBar progressBar;
    private ImageView imageView;
    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);
        temperature = findViewById(R.id.tVtemperature);
        temp_max = findViewById(R.id.tVtempMax);
        temp_min = findViewById(R.id.tVtempMin);
        cityView = findViewById(R.id.textView2);
        humidity = findViewById(R.id.tVpressure);
        pressure = findViewById(R.id.tVhumidity);
        cityInput = findViewById(R.id.editText);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_max.setText("");
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputText = cityInput.getText().toString();

                if(!inputText.isEmpty()){
                    getWeatherDataFromServer(inputText, API_KEY);
                }else{
                    Toast.makeText(MainActivity.this, "Type a city please", Toast.LENGTH_LONG).show();
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());
                currentLocation = location;
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        };
        //if device running less tha marshmallow
        //      if(Build.VERSION.SDK_INT < 23)

        // since MARSHMALLOW
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //ASK FOR PERMISSION
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }
    public ApiInterface getInterface(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build(); //retrofit object created
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        return apiInterface;
    }

    public void getWeatherDataFromServer(final String city, String appid){

        ApiInterface apiInterface = this.getInterface();
        Call<WeatherResponse> mService = apiInterface.getWeatherData(city, appid);
        Log.i("ser", mService.request() + "");
        progressBar.setVisibility(View.VISIBLE);
        mService.enqueue(new Callback<WeatherResponse>() { //it takes a callback interface
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                WeatherResponse weatherResponse = response.body();
                progressBar.setVisibility(View.INVISIBLE);
                if(weatherResponse != null){
                    Log.i("status", "if" );
                    Log.i("Res", weatherResponse.getMain().getTemp() + " ");
                    Log.i("Res", weatherResponse.getMain().getHumidity() + " ");

                    String icon = weatherResponse.getWeather().get(0).getIcon();
                    String iconLink = "https://openweathermap.org/img/w/" + icon + ".png";

                    Picasso.get().load(iconLink).into(imageView);
                    Log.i("WeatherIcon", iconLink);

                    Float _temperature = weatherResponse.getMain().getTemp() - 273.15f;
                    temperature.setText(String.format("%.2f", _temperature)+ "°C");

                    Float _temperatureMax = weatherResponse.getMain().getTemp_max() - 273.15f;
                    temp_max.setText(Math.round(_temperatureMax) + "°C");

                    Float _temperatureMin = weatherResponse.getMain().getTemp_min() - 273.15f;
                    temp_min.setText(Math.round(_temperatureMin ) + "°C");

                    cityView.setText(weatherResponse.getName());

                    Integer _pressure = weatherResponse.getMain().getPressure();
                    pressure.setText(_pressure  + " %");

                    Integer _humidity = weatherResponse.getMain().getHumidity();
                    humidity.setText(_humidity + " hPa");

                }else{
                   Log.i("status", " else");
                   Toast.makeText(MainActivity.this, "We couldn't find a city with name: " + cityInput.getText(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.i("failure response", t.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
                call.cancel();
            }
        });
    }

}

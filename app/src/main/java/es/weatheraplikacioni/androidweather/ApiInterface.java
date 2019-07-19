package es.weatheraplikacioni.androidweather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    /*Retrofit turns your HTTP API into a Java interface.
    Annotations on the interface methods and its parameters indicate how a request will be handled
    Ne kete rast po i marrim dy lloje te te dhenave permes emrim dhe tjetra permes lat dhe lon
    */

    //Pasi jsoni na kthen array list dmth, i thirrmi te dhenat me list
    @GET("weather")
    Call<WeatherResponse> getWeatherData(@Query("q") String q, @Query("appid") String appid);

}

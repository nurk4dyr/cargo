package kz.carveo.mvvm.geocoder;

import kz.carveo.mvvm.model.AsyncGeoCoderModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AsyncGeoCoderAPIService {

    @GET("/reverse?accept-language=ru&namedetails=0&extratags=0&format=json")
    Call<AsyncGeoCoderModel> getAddress(@Query("lat") double lat, @Query("lon") double lon);

}

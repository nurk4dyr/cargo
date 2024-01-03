package kz.carveo.mvvm.geocoder;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import org.osmdroid.util.GeoPoint;

import kz.carveo.mvvm.model.AsyncGeoCoderModel;
import kz.carveo.mvvm.utils.Config;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AsyncGeoCoder {

    private final Activity mParentActivity;
    private final Retrofit mClient;
    private final AsyncGeoCoderAPIService mApiService;

    public AsyncGeoCoder(@NonNull Activity activity) {

        mParentActivity = activity;

        mClient = new Retrofit.Builder()
                .baseUrl(Config.GEOCODER_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mClient.create(AsyncGeoCoderAPIService.class);

    }

    public void getFromGeoPoint(GeoPoint geoPoint, @NonNull AsyncGeoCoderCallback callback) {

        Call<AsyncGeoCoderModel> asyncGeoCoderModelCall = mApiService.getAddress(geoPoint.getLatitude(), geoPoint.getLongitude());

        asyncGeoCoderModelCall.enqueue(new Callback<AsyncGeoCoderModel>() {
            @Override
            public void onResponse(@NonNull Call<AsyncGeoCoderModel> call, @NonNull Response<AsyncGeoCoderModel> response) {
                if (!response.isSuccessful()) {
                    callback.onFailed(new Throwable("Something is wrong!"));
                    return;
                }

                AsyncGeoCoderModel asyncGeoCoderModel = response.body();

                mParentActivity.runOnUiThread(() -> {
                    callback.onSuccess(asyncGeoCoderModel.displayName);
                });
            }

            @Override
            public void onFailure(@NonNull Call<AsyncGeoCoderModel> call, @NonNull Throwable t) {
                mParentActivity.runOnUiThread(() -> {
                    callback.onFailed(t);
                });
            }
        });

    }

}

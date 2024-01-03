package kz.carveo.mvvm.geocoder;

public interface AsyncGeoCoderCallback {
    void onSuccess(String address);
    void onFailed(Throwable t);
}

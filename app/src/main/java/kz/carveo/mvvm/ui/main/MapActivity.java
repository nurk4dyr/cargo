package kz.carveo.mvvm.ui.main;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;

import kz.carveo.mvvm.R;
import kz.carveo.mvvm.databinding.ActivityMainBinding;
import kz.carveo.mvvm.geocoder.AsyncGeoCoder;
import kz.carveo.mvvm.geocoder.AsyncGeoCoderCallback;
import kz.carveo.mvvm.ui.base.BaseActivity;
import kz.carveo.mvvm.utils.Config;
import kz.carveo.mvvm.utils.maploader.IMapLoader;
import kz.carveo.mvvm.utils.maploader.MapLoader;

public class MapActivity extends BaseActivity<ActivityMainBinding, MapViewModel> implements NavigationView.OnNavigationItemSelectedListener {

    private IMapLoader mMapLoader;
    private ShimmerFrameLayout mShimmerFrameLayout;

    private AsyncGeoCoder mAsyncGeoCoder;

    @Override
    protected ActivityMainBinding createViewBinding(LayoutInflater layoutInflater) {
        return ActivityMainBinding.inflate(layoutInflater);
    }

    @Override
    protected MapViewModel createViewModel() {
        return new ViewModelProvider(this).get(MapViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMapLoader = new MapLoader(this, mContext, mBinding.mapView, Config.TILE_SERVER_URLS);
        mMapLoader.load();

        mShimmerFrameLayout = mBinding.bottomSheetTaxi.shimmer;

        mAsyncGeoCoder = new AsyncGeoCoder(this);

        setInsetsOn(mBinding.mainUI);
        setListeners();
    }

    private void setListeners() {

        mBinding.mapZoomIn.setOnClickListener((view) -> mMapLoader.zoomIn());
        mBinding.mapZoomOut.setOnClickListener((view) -> mMapLoader.zoomOut());
        mBinding.location.setOnClickListener((view) -> mMapLoader.animateTo(mMapLoader.getLocation()));

        mMapLoader.onMapDelayedChangedListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                mShimmerFrameLayout.showShimmer(true);
                mShimmerFrameLayout.startShimmer();

                GeoPoint point = mMapLoader.getMapCenter();

                mAsyncGeoCoder.getFromGeoPoint(point, new AsyncGeoCoderCallback() {
                    @Override
                    public void onSuccess(String address) {
                        mShimmerFrameLayout.stopShimmer();
                        mShimmerFrameLayout.hideShimmer();

                        mBinding.bottomSheetTaxi.addressFrom.setText(address);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        Log.e("AsyncGeoCoder Error", t.toString());
                    }
                });

                mMapLoader.getMapView().invalidate();

                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        }, 300));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mMapLoader.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapLoader.getMapView().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapLoader.getMapView().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapLoader.getMapView().onDetach();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //TODO: How to do it better?
        int[] location = new int[2];
        mBinding.pin.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];



        int dx = x + (mBinding.pin.getWidth() / 2);
        int dy = y + mBinding.pin.getHeight();
        mMapLoader.setMapCenter(dx, dy);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
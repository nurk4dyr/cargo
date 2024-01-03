package kz.carveo.mvvm.ui.custom;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kz.carveo.mvvm.R;

public class LocationOverlay extends Overlay implements IMyLocationConsumer {

    private class LocationProvider implements IMyLocationProvider, LocationListener {

        private IMyLocationConsumer mMyLocationConsumer;
        private LocationManager mLocationManager;
        private Location mLocation;
        private final Set<String> mLocationSources = new HashSet<>();

        public LocationProvider(Context context) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            mLocationSources.add(mLocationManager.getBestProvider(criteria, true));
            mLocationSources.add(LocationManager.GPS_PROVIDER);
            mLocationSources.add(LocationManager.NETWORK_PROVIDER);
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            mLocation = location;

            if (mMyLocationConsumer != null) {
                mMyLocationConsumer.onLocationChanged(mLocation, this);
            }
        }

        @Override
        public void onLocationChanged(@NonNull List<Location> locations) {
            LocationListener.super.onLocationChanged(locations);
        }

        @Override
        public void onFlushComplete(int requestCode) {
            LocationListener.super.onFlushComplete(requestCode);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
        }

        @Override
        @SuppressLint("MissingPermission")
        public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
            boolean result = false;

            mMyLocationConsumer = myLocationConsumer;

            for (final String provider : mLocationManager.getProviders(true)) {
                if (mLocationSources.contains(provider)) {

                    try {
                        mLocationManager.requestLocationUpdates(provider, 100,
                                0.0f, this);
                        result = true;
                    } catch (Throwable ex) {
                        Log.e(IMapView.LOGTAG, "Unable to attach listener for location provider " + provider + " check permissions?", ex);
                    }
                }
            }

            return result;
        }

        @Override
        @SuppressLint("MissingPermission")
        public void stopLocationProvider() {
            mMyLocationConsumer = null;
            if (mLocationManager != null) {
                try {
                    mLocationManager.removeUpdates(this);
                } catch (Throwable ex) {
                    Log.w(IMapView.LOGTAG, "Unable to deattach location listener", ex);
                }
            }
        }

        @Override
        public Location getLastKnownLocation() {
            return mLocation;
        }

        @Override
        public void destroy() {
            stopLocationProvider();
            mLocation = null;
            mLocationManager = null;
            mMyLocationConsumer = null;
        }
    }

    private Context mContext;
    private final MapView mMapView;
    private final IMyLocationProvider mMyLocationProvider;
    private Location mLocation;
    private boolean mLocationEnabled;

    private final Handler mHandler;
    private final Object mHandlerToken = new Object();
    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<>();

    private final Projection mProjection;
    private final Point mNewLocationPoint;
    private final Point mLastLocationPoint;
    private final Paint mOuterCirclePaint;
    private final Paint mInnerCirclePaint;

    private ValueAnimator mAnimator;
    private GeoPointInterpolator mGeoPointInterpolator;
    private GeoPoint mStartPosition;
    private GeoPoint mNewPosition;
    
    public LocationOverlay(Context context, MapView mapView) {
        mMyLocationProvider = new LocationProvider(context);
        mMapView = mapView;
        mContext = context;

        mHandler = new Handler(Looper.getMainLooper());

        mGeoPointInterpolator = new GeoPointInterpolator.Spherical();
        mStartPosition = new GeoPoint(0d, 0d);
        mNewPosition = new GeoPoint(0d, 0d);

        mProjection = mMapView.getProjection();
        mNewLocationPoint = new Point();
        mLastLocationPoint = new Point();

        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setStyle(Paint.Style.FILL);
        mOuterCirclePaint.setColor(Color.WHITE);
        mOuterCirclePaint.setAntiAlias(true);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setColor(Color.parseColor("#F5891D"));
        mInnerCirclePaint.setAntiAlias(true);
    }

    public void enableMyLocation() {
        boolean result = mMyLocationProvider.startLocationProvider(this);
        mLocationEnabled = result;

        if (result) {
            Location location = mMyLocationProvider.getLastKnownLocation();
            if (location != null)
                setLocation(location);
        }
    }

    private void drawLocation(Canvas pCanvas, Projection pProjection) {
        if (mLocation != null && mLocationEnabled) {
            pProjection.toPixels(mNewPosition, mNewLocationPoint);

            pCanvas.drawCircle(mNewLocationPoint.x, mNewLocationPoint.y, 20, mOuterCirclePaint);
            pCanvas.drawCircle(mNewLocationPoint.x, mNewLocationPoint.y, 10, mInnerCirclePaint);
        }
    }

    @Override
    public void draw(Canvas pCanvas, Projection pProjection) {
        drawLocation(pCanvas, pProjection);
    }

    @Override
    public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
        drawLocation(pCanvas, pMapView.getProjection());
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        if (location != null && mHandler != null) {

            mHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if (mLocation != null)
                        mStartPosition.setCoords(mLocation.getLatitude(), mLocation.getLongitude());

                    setLocation(location);

                    for (Runnable runnable : mRunOnFirstFix) {
                        Thread t = new Thread(runnable);
                        t.setName(this.getClass().getName() + "#onLocationChanged");
                        t.start();
                    }
                    mRunOnFirstFix.clear();
                }
            }, mHandlerToken, 0);
        }
    }

    private void setLocation(Location location) {
        mLocation = location;
    }

    public void runOnFirstFix(Runnable runnable) {
        if (mMyLocationProvider != null && mLocation != null) {
            Thread t = new Thread(runnable);
            t.setName(this.getClass().getName() + "#runOnFirstFix");
            t.start();
        } else {
            mRunOnFirstFix.addLast(runnable);
        }
    }

    public GeoPoint getMyLocation() {
        if (mLocation != null)
            return new GeoPoint(mLocation);

        return null;
    }
}

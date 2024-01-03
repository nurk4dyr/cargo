package kz.carveo.mvvm.utils.maploader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import kz.carveo.mvvm.R;
import kz.carveo.mvvm.ui.custom.LocationOverlay;
import kz.carveo.mvvm.utils.Config;
import kz.carveo.mvvm.utils.managers.IPermissionManager;

public class MapLoader implements IMapLoader {

    private final Activity mActivity;
    private final Context mContext;
    private final MapView mMapView;
    private final String[] mUrl;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MyLocationNewOverlay mMyLocationOverlay;
    private int mMapOffsetX;
    private int mMapOffsetY;
    private double mMapInitZoom = Config.MAP_INIT_ZOOM;
    private double mMapMaxZoom = Config.MAP_MAX_ZOOM;
    private double mMapMinZoom = Config.MAP_MIN_ZOOM;

    public MapLoader(Activity activity, Context context, MapView mapView, String[] urls) {
        this.mActivity = activity;
        this.mContext = context;
        this.mMapView = mapView;
        this.mUrl = urls;
        this.mMapOffsetX = mapView.getMapCenterOffsetX();
        this.mMapOffsetY = mapView.getMapCenterOffsetY();
    }

    public MapLoader(Activity activity, Context context, MapView mapView, String[] urls, double initZoom, double maxZoom, double minZoom) {
        this(activity, context, mapView, urls);
        this.mMapInitZoom = initZoom;
        this.mMapMaxZoom = maxZoom;
        this.mMapMinZoom = minZoom;
    }

    @Override
    public void load() {
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));
        Configuration.getInstance().setAnimationSpeedShort(200);
        Configuration.getInstance().setAnimationSpeedDefault(200);

        if (IPermissionManager.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableGPSTracker();

            LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastLocation != null)
                animateTo(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));

        } else {
            IPermissionManager.getPermissions(mActivity,
                                              new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                              LOCATION_PERMISSION_REQUEST_CODE);
        }


        mMapView.setMaxZoomLevel(mMapMaxZoom);
        mMapView.setMinZoomLevel(mMapMinZoom);
        mMapView.setTilesScaleFactor(1.5f);
        mMapView.getController().setZoom(mMapInitZoom);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mMapView.setTileSource(new OnlineTileSourceBase("CarveoHost", 0, 20, 256, ".png", mUrl) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex)
                        + "/" + MapTileIndex.getX(pMapTileIndex)
                        + "/" + MapTileIndex.getY(pMapTileIndex)
                        + mImageFilenameEnding;
            }
        });

        int nightModeFlags = mContext.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            mMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
    }

    @Override
    public void enableGPSTracker() {

        GpsMyLocationProvider provider = new GpsMyLocationProvider(mContext);
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
        mMyLocationOverlay = new MyLocationNewOverlay(provider, mMapView);
        mMyLocationOverlay.enableMyLocation();

        Drawable l = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.location, null);
        Bitmap locationIcon = ((BitmapDrawable) l).getBitmap();

        Drawable lr = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.location_arrow, null);
        Bitmap locationArrowIcon = ((BitmapDrawable) lr).getBitmap();

        mMyLocationOverlay.setPersonIcon(locationIcon);
        mMyLocationOverlay.setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        mMyLocationOverlay.setDirectionIcon(locationArrowIcon);
        mMyLocationOverlay.setDirectionAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        mMyLocationOverlay.runOnFirstFix(() -> mMapView.post(() -> animateTo(mMyLocationOverlay.getMyLocation())));

        mMapView.getOverlays().add(mMyLocationOverlay);
    }

    @Override
    public void setMapOffset(int x, int y) {
        mMapOffsetX = x;
        mMapOffsetY = y;
        mMapView.setMapCenterOffset(mMapOffsetX, mMapOffsetY);
    }

    @Override
    public GeoPoint getMapCenter() {
        return getPointFromPixels((mMapView.getWidth() / 2) + mMapOffsetX,
                                  (mMapView.getHeight() / 2) + mMapOffsetY);
    }

    @Override
    public void setMapCenter(int x, int y) {
        int dx = x - (mMapView.getProjection().getWidth() / 2);
        int dy = y - (mMapView.getProjection().getHeight() / 2);

        setMapOffset(dx, dy);
    }

    @Override
    public GeoPoint getPointFromPixels(int x, int y) {
        return (GeoPoint) mMapView.getProjection().fromPixels(x, y);
    }

    @Override
    public GeoPoint getLocation() {
        return mMyLocationOverlay.getMyLocation();
    }

    @Override
    public void animateTo(@NonNull GeoPoint point) {
        mMapView.getController().animateTo(point, mMapMaxZoom, Config.MAP_ANIMATION_SPEED);
    }

    @Override
    public void zoomIn() {
        mMapView.getController().zoomIn(200L);
    }

    @Override
    public void zoomOut() {
        mMapView.getController().zoomOut(200L);
    }

    @Override
    public boolean canZoomIn() {
        return mMapView.canZoomIn();
    }

    @Override
    public boolean canZoomOut() {
        return mMapView.canZoomOut();
    }

    @Override
    public boolean canNextZoomIn() {
        return !(mMapView.getZoomLevelDouble() >= mMapMaxZoom - 1);
    }

    @Override
    public boolean canNextZoomOut() {
        return !(mMapView.getZoomLevelDouble() <= mMapMinZoom + 1);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapTouch(View.OnTouchListener onTouchListener) {
        mMapView.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onMapChangedListener(MapListener mapListener) {
        mMapView.addMapListener(mapListener);
    }

    @Override
    public void onMapDelayedChangedListener(DelayedMapListener delayedMapListener) {
        mMapView.addMapListener(delayedMapListener);
    }

    @Override
    public MapView getMapView() {
        return mMapView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableGPSTracker();
            }
        }
    }
}

package kz.carveo.mvvm.utils.maploader;

import android.view.View;

import androidx.annotation.NonNull;

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import kz.carveo.mvvm.utils.managers.IPermissionManager;

public interface IMapLoader extends IPermissionManager {
    void load();
    void enableGPSTracker();
    void setMapOffset(int x, int y);
    GeoPoint getMapCenter();
    void setMapCenter(int x, int y);
    GeoPoint getPointFromPixels(int x, int y);
    GeoPoint getLocation();
    void animateTo(@NonNull GeoPoint point);
    void zoomIn();
    void zoomOut();
    boolean canZoomIn();
    boolean canZoomOut();
    boolean canNextZoomIn();
    boolean canNextZoomOut();
    void onMapTouch(View.OnTouchListener onTouchListener);
    void onMapChangedListener(MapListener mapListener);
    void onMapDelayedChangedListener(DelayedMapListener delayedMapListener);
    MapView getMapView();
}

package kz.carveo.mvvm.utils.managers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public interface IPermissionManager {

    static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    static void getPermissions(@NonNull Activity activity, @NonNull String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults);

}

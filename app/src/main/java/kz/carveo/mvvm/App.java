package kz.carveo.mvvm;

import android.app.Application;

public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App getInstance() {
        if (sInstance == null) {
            sInstance = new App();
        }
        return sInstance;
    }
}

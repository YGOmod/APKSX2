package kr.co.iefriends.pcsx2;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import java.lang.ref.WeakReference;

public class MainApplication extends Application {
    private static WeakReference<BaseActivity> currentActivityReference;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static void setCurrentActivity(BaseActivity currentActivity) {
        WeakReference<BaseActivity> newActivity = currentActivityReference;
        if (newActivity != null) {
            newActivity.clear();
            currentActivityReference = null;
        }
        if (currentActivity != null) {
            currentActivityReference = new WeakReference<>(currentActivity);
        }
    }
}
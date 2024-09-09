package kr.co.iefriends.pcsx2;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import java.lang.ref.WeakReference;

public class MainApplication extends Application {
    private static WeakReference<BaseActivity> curActivityReference;

    @Override
    public void onCreate() {
        super.onCreate();
        curApplicationContext = getApplicationContext();
    }
    public static BaseActivity getCurrentActivity() {
        WeakReference<BaseActivity> activityReference = curActivityReference;
        return activityReference != null ? activityReference.get() : null;
    }

    public static void setCurrentActivity(BaseActivity newActivity) {
        WeakReference<BaseActivity> activityReference = curActivityReference;
        if (activityReference != null) {
            activityReference.clear();
            curActivityReference = null;
        }
    

        if (newActivity != null) {
            curActivityReference = new WeakReference<>(newActivity);
        }
    }

    public static void onDestroy() {
        WeakReference<BaseActivity> activityReference = curActivityReference;
        if (activityReference != null) {
            activityReference();
            curActivityReference = null;
        }
}
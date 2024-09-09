package kr.co.iefriends.pcsx2;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import java.lang.ref.WeakReference;

public class MainApplication extends Application {
    private static WeakReference<BaseActivity> currentActivityReference;
    private static Context currentApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        currentApplicationContext = getApplicationContext();
    }
   
    public static void setCurrentActivity(BaseActivity newActivity) {
        WeakReference<BaseActivity> _newActivity = currentActivityReference;
        if (_newActivity != null) {
            _newActivity.clear();
            mActivityReference = null;
        }
        if (newActivity != null) {
            currentActivityReference = new WeakReference<>(newActivity);
        }
    }
}
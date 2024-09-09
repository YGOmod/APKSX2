package kr.co.iefriends.pcsx2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;

public class Helpers {
    public static String getLocalPath(String fileName) {
        String localPath = "";
        String pathFormat = TextUtils.isEmpty(fileName) ? "%s/sdmc" : "%s/sdmc/%s";
        try {
            BaseActivity currentActivity = MainApplication.getCurrentActivity();
            if (currentActivity != null) {
                boolean isMounted = Environment.getExternalStorageState().equals("mounted");
                String basePath = isMounted 
                    ? currentActivity.getApplicationContext().getExternalFilesDir(null).toString()
                    : currentActivity.getApplicationContext().getFilesDir().toString();
                localPath = String.format(pathFormat, basePath, fileName);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        if (localPath.endsWith("sdmc")) {
            try {
                File dir = new File(localPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localPath;
    }

    public static String getFullPath(String dirName, String fileName) {
        String localPath = getLocalPath(directoryName);
        return String.format("%s/%s", localPath, fileName);
    }
}
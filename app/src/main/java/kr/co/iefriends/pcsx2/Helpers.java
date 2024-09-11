package kr.co.iefriends.pcsx2;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Helpers {
    public static void copyAssetAll(Context p_context, String srcPath) {
        AssetManager assetMgr = p_context.getAssets();
        String[] assets = null;
        try {
            String destPath = p_context.getExternalFilesDir(null) + File.separator + srcPath;
            assets = assetMgr.list(srcPath);
            if(assets != null) {
                if (assets.length == 0) {
                    copyFile(p_context, srcPath, destPath);
                } else {
                    File dir = new File(destPath);
                    if (!dir.exists())
                        dir.mkdir();
                    for (String element : assets) {
                        copyAssetAll(p_context, srcPath + File.separator + element);
                    }
                }
            }
        }
        catch (IOException ignored) {}
    }

    public static void copyFile(Context p_context, String srcFile, String destFile) {
        AssetManager assetMgr = p_context.getAssets();

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = assetMgr.open(srcFile);
            boolean _exists = new File(destFile).exists();
            if(srcFile.contains("shaders")) {
                _exists = false;
            }
            if(!_exists)
            {
                os = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                is.close();
                os.flush();
                os.close();
            }
        }
        catch (IOException ignored) {}
    }

    public static void sendKeyAction(View p_view, int p_action, int p_keycode) {
    if(p_action == MotionEvent.ACTION_DOWN) {
        p_view.setPressed(true);
        int pad_force = 0;
        if(p_keycode >= 110) {
            float _abs = 90; // Joystic test value
            _abs = Math.min(_abs, 100);
            pad_force = (int) (_abs * 32766.0f / 100);
        }
        NativeApp.setPadButton(p_keycode, pad_force, true);
        } else if(p_action == MotionEvent.ACTION_UP 
               || p_action == MotionEvent.ACTION_CANCEL) {
            p_view.setPressed(false);
            NativeApp.setPadButton(p_keycode, 0, false);
        }
    }
    //formatting
    public static String format(String formatString, Object... arguments) {
        return String.format(Locale.ENGLISH, formatString, arguments);
    }
}
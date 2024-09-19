package kr.co.iefriends.pcsx2;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BaseActivity {

    public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.game_main);

    NativeApp.initializeOnce(getApplicationContext());

    copyAssetAll(getApplicationContext(), "resources");

    // Play Button
    FloatingActionButton btn_play = findViewById(R.id.btn_game_play);
    if(btn_play != null) {
        btn_play.setOnClickListener(v -> {
            // Internal storage
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.setType("*/*");
            startActivityResultLocalFilePlay.launch(intent);
        });
    }
    }

    public final ActivityResultLauncher<Intent> startActivityResultLocalFilePlay = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent _intent = result.getData();
                        if(_intent != null) {
                            String m_szGamefile = _intent.getDataString();
                            if(!TextUtils.isEmpty(m_szGamefile)) {
                                startLocalFile(m_szGamefile);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });

    public synchronized void startLocalFile(String localFile) {
        try {
            Context applicationContext = getApplicationContext();
            copyAssetAll(applicationContext, "bios");
            Intent intent = new Intent(applicationContext, EmulationActivity.class);
            intent.putExtra("GamePath", localFile);
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

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
}
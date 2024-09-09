package kr.co.iefriends.myps2;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class BaseAdapter extends RecyclerView {
    private final String[] supp_ext = {"iso", "cso", "bin", "chd", "mdf", "nrg", "img"};

    public void startGameActivity(String fileName, String dirName) {
        try {
            BaseActivity currentActivity = MainApplication.getCurrentActivity();
            if (currentActivity != null) {
                Context applicationContext = currentActivity.getApplicationContext();
                Intent intent = new Intent(applicationContext, NativeActivity.class);
                intent.putExtra("GamePath", Helpers.getFullPath(fileName, dirName));
                currentActivity.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
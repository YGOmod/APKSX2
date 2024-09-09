package kr.co.iefriends.pcsx2;

import android.os.Bundle;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainApplication.setCurrentActivity(this);
    }

    protected void onResume() {
        super.onResume();
        MainApplication.setCurrentActivity(this);
    }
}
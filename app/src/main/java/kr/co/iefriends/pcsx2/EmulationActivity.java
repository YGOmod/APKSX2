package kr.co.iefriends.pcsx2;

import android.os.Bundle;
import android.content.Intent;

public class EmulationActivity extends BaseActivity {

    private String m_szGamefile = "";

    public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.game_main);

    Intent intent = getIntent();
        if (intent != null) {
            m_szGamefile = intent.getStringExtra("GamePath");
        } else {
            m_szGamefile = "";
        }
    }
}
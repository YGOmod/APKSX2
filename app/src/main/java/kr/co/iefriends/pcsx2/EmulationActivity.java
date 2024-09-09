package kr.co.iefriends.pcsx2;

import android.os.Bundle;
import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmulationActivity extends BaseActivity {

    public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.game_main);

    // Play Button
    FloatingActionButton btn_play = findViewById(R.id.btn_game_play);
    if(btn_play != null) {
        btn_play.setOnClickListener(v -> {
        // TODO
        });
    }
    /*Intent intent = getIntent();
        if (intent != null) {
            m_szGamefile = intent.getStringExtra("GamePath");
        } else {
            m_szGamefile = "";
        }*/
    }
}
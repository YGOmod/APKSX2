package kr.co.iefriends.pcsx2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

public abstract class BaseDialog extends Dialog {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
    }
}
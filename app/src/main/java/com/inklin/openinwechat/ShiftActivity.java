package com.inklin.openinwechat;

import android.app.Activity;
import android.os.Bundle;

public class ShiftActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.finishAndRemoveTask();
    }
}

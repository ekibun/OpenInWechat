package com.inklin.openinwechat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.inklin.openinwechat.utils.PreferencesUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_layout);

        Intent intent = getIntent();
        String action = intent.getAction();

        if(Intent.ACTION_SEND.equals(action)){
            String type = intent.getType();
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null){
                    Matcher matcher = Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]").matcher(sharedText);
                    if (matcher.find()) {
                        openInWechat(matcher.group());
                        return;
                    }
                }
            }
        }
        if(Intent.ACTION_VIEW.equals(action)){
            Uri uri = intent.getData();
            if(uri != null){
                openInWechat(uri.toString());
            }
        }
        this.finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        this.finish();
    }

    private void openInWechat(final String url) {
        if(PreferencesUtils.isAccessibilitySettingsOn(this) && PreferencesUtils.isStorageEnable(this)){
            try {
                Intent intent = new Intent(this, AccessibilityMonitorService.class);
                intent.putExtra("processWechat", url);
                startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        }
    }
}

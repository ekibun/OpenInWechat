package com.inklin.openinwechat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.inklin.openinwechat.utils.PreferencesUtils;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.load_layout);

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
                    }else{
                        openInWechat(sharedText);
                    }
                }
            }
        }
        if(Intent.ACTION_VIEW.equals(action)){
            Uri uri = intent.getData();
            if(uri != null){
                if("qr.alipay.com".equals(uri.getHost().toLowerCase())) {
                    String u = uri.toString().replace("QR.ALIPAY.COM", "qr.alipay.com");
                    Matcher matcher = Pattern.compile("qr.alipay.com/(.+)").matcher(u);
                    if (matcher.find()) {
                        String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                                "qrcode=https%3A%2F%2Fqr.alipay.com%2F" + matcher.group(1) + "#Intent;" +
                                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                        try {
                            Intent intentAli = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
                            startActivity(intentAli);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                } else if("ofo.so".equals(uri.getHost())) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    if(sp.getBoolean("ofo_wechat", false))
                        openInWechat(uri.toString());
                    else{
                        Matcher matcher = Pattern.compile("ofo.so/plate/(.+)").matcher(uri.toString());
                        if (matcher.find()) {
                            String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                                    "clientVersion=3.7.0.0718&qrcode=http%3A%2F%2Fofo.so%2Fplate%2F" + matcher.group(1) + "#Intent;" +
                                    "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                            try {
                                Intent intentAli = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
                                startActivity(intentAli);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else
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

package com.inklin.openinwechat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.inklin.openinwechat.utils.PreferencesUtils;

import java.io.File;
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
        if(!flag_request)
            this.finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(!flag_request)
            this.finish();
    }

    private static final int REQUEST_ACCESS_CODE = 0;
    private static final int REQUEST_STORAGE_CODE = 1;
    private boolean checkPermission(){
        if(!PreferencesUtils.isAccessibilitySettingsOn(this)) {
            flag_request = true;
            startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESS_CODE);
            return false;
        }
        if(Build.VERSION.SDK_INT >= 23 && ! PreferencesUtils.isStorageEnable(this)) {
            flag_request = true;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_CODE);
            return false;
        }
        return true;
    }
    boolean flag_request = false;
    String wechatUrl = "";
    private void openInWechat(final String url) {
        if(url == null || url.isEmpty())
            return;
        wechatUrl = url;
        if (checkPermission()) {
            openInWechat();
        } else {
            Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_SHORT).show();
        }
    }

    private void openInWechat() {
        try {
            Intent intent = new Intent(this, AccessibilityMonitorService.class);
            intent.putExtra("processWechat", wechatUrl);
            startService(intent);
            this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        flag_request = false;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACCESS_CODE && PreferencesUtils.isAccessibilitySettingsOn(this)) {
            openInWechat(wechatUrl);
        }
        if(!flag_request)
            this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        flag_request = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //重新运行
                openInWechat();
            }else{
                Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_SHORT).show();
            }
        }
        this.finish();
    }
}

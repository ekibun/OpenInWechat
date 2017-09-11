package com.inklin.openinwechat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.inklin.openinwechat.utils.PreferencesUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
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
            if(uri != null)
                openInWechat(uri.toString());
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
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v("URL", url);

                try {
                    Class<?> personClazz02 = Class.forName("Person");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                execRootCmdSilent("am start -n com.tencent.mm/com.tencent.mm.plugin.scanner.ui.BaseScanUI -a android.intent.action.VIEW -d '" + url + "'");
                //execRootCmdSilent("am start -S -n com.tencent.mm/com.tencent.mm.plugin.webview.ui.tools.WebViewUI --es rawUrl '" + url + "'");
            }
        }).start();
        */
    }
/*
    // 执行命令但不关注结果输出
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    */
}

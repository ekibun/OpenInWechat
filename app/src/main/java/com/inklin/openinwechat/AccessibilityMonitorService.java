package com.inklin.openinwechat;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.inklin.openinwechat.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by acaoa on 2017/9/7.
 */

public class AccessibilityMonitorService extends AccessibilityService {

    File file;
    int process = 0;
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null && intent.hasExtra("processWechat")){
            try {
                String url = intent.getStringExtra("processWechat");
                Bitmap qrcode = FileUtils.createQRCode(url, 500);
                file = FileUtils.saveBitmapToCache(this, qrcode, url);
                //FileUtils.requestScanFile(this, file.getPath());
                process = openWechatScanUI(this)? 1 : 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean openWechatScanUI(Context context){
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            //Toast.makeText(context, context.getString(R.string.toast_no_wechat), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }

    int sleepTime = 200;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(process <= 0)
            return;
        String className = event.getClassName().toString();
        Log.e("className", className);
        //AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        AccessibilityNodeInfo mAccessibilityNodeInfo = event.getSource();

        if (mAccessibilityNodeInfo == null)
            return;

        List<AccessibilityNodeInfo> infos;
        switch (process) {
            case 1:
                if (!"com.tencent.mm.plugin.scanner.ui.BaseScanUI".equals(className))
                    break;
                infos = mAccessibilityNodeInfo.findAccessibilityNodeInfosByText("更多");
                if (infos != null && infos.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = infos.get(infos.size() - 1);
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    process++;
                }
                break;
            case 2:
                infos = mAccessibilityNodeInfo.findAccessibilityNodeInfosByText("从相册选取二维码");
                if (infos != null && infos.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = infos.get(infos.size() - 1);
                    accessibilityNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    sleepTime = 200;
                    process++;
                }
                break;
            case 3:
                infos = mAccessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eq");
                if (infos != null && infos.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = infos.get(0);
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    process ++;
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startActivity(new Intent(getApplicationContext(), ShiftActivity.class));
                            sleepTime += 100;
                        }
                    }).start();
                }
                break;
            case 4:
                process = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            if(file != null && file.exists())
                                file.delete();
                            file = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }

    @Override
    public void onInterrupt() {
        //辅助服务被关闭 执行此方法

    }
}

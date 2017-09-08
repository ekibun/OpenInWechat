package com.inklin.openinwechat.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

/**
 * Created by acaoa on 2017/9/8.
 */

public class FileUtils {

    public static File saveBitmapToCache(Context context, Bitmap bmp, String fileName){
        File file = null;
        try {
            String path =  getDiskCacheDir(context, "qrcode").getAbsolutePath() + File.separator + "ScreenShots";//getDiskCacheDir(context, uniqueName).getAbsolutePath();
            File fileFolder = new File(path);
            if (fileFolder.exists())
                for(File f:fileFolder.listFiles()){
                    f.delete();
                }
            if (!fileFolder.exists())
                fileFolder.mkdirs();

            file = new File(path, md5(fileName));
            if (!file.exists())
                file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.ImageColumns.TITLE, file.getName());
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.ImageColumns.SIZE, file.length());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xffffffff;
    public static Bitmap createQRCode(String str, int widthAndHeight)
            throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }else{
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}

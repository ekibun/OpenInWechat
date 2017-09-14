package com.inklin.openinwechat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlipayActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_layout);

        Intent intent = getIntent();

        if(Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && "qr.alipay.com".equals(uri.getHost())) {
                Matcher matcher = Pattern.compile("qr.alipay.com/(.+)").matcher(uri.toString());
                if (matcher.find()) {
                    String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F" + matcher.group(1) + "%3F_s" +
                            "%3Dweb-other&_t=1472443966571#Intent;" +
                            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                    try {
                        Intent intentAli = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
                        startActivity(intentAli);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.finish();
    }
}

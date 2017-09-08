package com.inklin.openinwechat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.os.Bundle;

import com.inklin.openinwechat.utils.PreferencesUtils;

import java.net.URISyntaxException;

public class PreferencesActivity extends Activity {
    private static final int REQUEST_STORAGE_CODE = 1;

    public static class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SharedPreferences sp;

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            // 加载xml资源文件
            addPreferencesFromResource(R.xml.preferences);
            sp = getPreferenceManager().getSharedPreferences();
            refreshSummary();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
            //Log.d("onPreferenceTreeClick",preference.getKey());
            if("aces_permit".equals(preference.getKey()))
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            if("default_permit".equals(preference.getKey()))
                PreferencesUtils.openDefaultSettings(getActivity());
            if(Build.VERSION.SDK_INT >= 23 && "save_permit".equals(preference.getKey()) && ! PreferencesUtils.isStorageEnable(getActivity()))
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_CODE);
            if("version_code".equals(preference.getKey()))
                ((PreferencesActivity)getActivity()).showInfo();
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if("hide_launcher".equals(key)){
                PackageManager pkg=getActivity().getPackageManager();
                if(sharedPreferences.getBoolean(key, false)){
                    pkg.setComponentEnabledSetting(new ComponentName(getActivity(), SplashActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }else{
                    pkg.setComponentEnabledSetting(new ComponentName(getActivity(), SplashActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
            }
            refreshSummary();
        }

        @Override
        public void onResume() {
            super.onResume();

            refreshSummary();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }



        public void refreshSummary(){
            Preference savePref = (Preference) findPreference("save_permit");
            savePref.setSummary(getString(PreferencesUtils.isStorageEnable(getActivity())? R.string.pref_enable_permit : R.string.pref_disable_permit));

            Preference acesPref = (Preference) findPreference("aces_permit");
            acesPref.setSummary(getString(PreferencesUtils.isAccessibilitySettingsOn(getActivity())? R.string.pref_enable_permit : R.string.pref_disable_permit));

            //Preference defaultPref = (Preference) findPreference("default_permit");
            //defaultPref.setSummary(getString(PreferencesUtils.isDefaultActivity(getActivity())? R.string.pref_enable_permit : R.string.pref_disable_permit));

            Preference aboutPref = (Preference) findPreference("version_code");
            aboutPref.setSummary(PreferencesUtils.getVersion(getActivity()));
        }
    }

    public void showInfo(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_dialog_title));
        builder.setMessage(getString(R.string.about_dialog_message));
        builder.setNeutralButton(R.string.about_dialog_github, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse("https://github.com/acaoairy/QQNotfAndShare");
                intent.setData(content_url);
                startActivity(Intent.createChooser(intent, null));
            }
        });
        builder.setNegativeButton(R.string.about_dialog_support, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX04432XWNQIFV2UDCR64%3F_s" +
                        "%3Dweb-other&_t=1472443966571#Intent;" +
                        "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                try {
                    Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME );
                    startActivity(intent);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton(R.string.about_dialog_button, null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }
}

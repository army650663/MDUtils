package tw.idv.madmanchen.mdutils;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

import tw.idv.madmanchen.mdutilslib.base.VersionChecker;

public class MainActivity extends AppCompatActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("pkgName", "com.hsinmerit");
        hashMap.put("verName", "1.27.060");
        hashMap.put("verCode", "28");
        String[] urls = {
                "http://public.hsinten.com.tw/appupdate/",
                "http://192.168.42.135",
                "http://pub.mysoqi.com/appupdate/",
        };

        new VersionChecker.Builder()
                .setCheckType(VersionChecker.SERVER)
                .setServerUrl(urls[2])
                .addServerData("pkgName", "com.agenttw")
                .addServerData("verName", "1.27.060")
                .addServerData("verCode", "28")
                .setUpdateView(mContext, "Update", "Need update", "Update", false)
                .build()
                .check(null);

//        new VersionChecker.Builder()
//                .setCheckType(VersionChecker.GOOGLE_PLAY)
//                .addGooglePlayInfo("com.hsinmerit", "1.27.060")
//                .setUpdateView(mContext, "Update", "Have to update", "update", false)
//                .build()
//                .check(null);
    }
}

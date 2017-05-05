package tw.idv.madmanchen.mdutils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import tw.idv.madmanchen.mdutilslib.base.BaseActivity;
import tw.idv.madmanchen.mdutilslib.base.VersionChecker;

public class MainActivity extends BaseActivity {
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("onCreate", "onCreate");
        mContext = this;
        new VersionChecker.Builder()
                .setCheckType(VersionChecker.SERVER)
                .setServerUrl("http://pub.mysoqi.com/appupdate/")
                .setUpdateView(mContext, "", "Need update", "update", false)
                .setLoadingView(mContext, "", "Check version")
                .addServerData("pkgName", "com.agenttw")
                .addServerData("verCode", String.valueOf(BuildConfig.VERSION_CODE))
                .setDownloadPath(getExternalCacheDir().getPath())
                .build()
                .check(new VersionChecker.SubCheck() {
                    @Override
                    public void onChecked(boolean b, AlertDialog.Builder builder) {
                        Log.e("result", b + "");
                        if (b) {

                        } else {
                            builder.show();
                        }
                    }
                });

    }


    @Override
    protected void init() {

    }
}

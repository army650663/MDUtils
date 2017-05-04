package tw.idv.madmanchen.mdutils;

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
                .setServerUrl("http://www.xingmerit.com.cn/appupdate/")
                .setUpdateView(mContext, "", "Info", "Update", false)
                .addServerData("pkgName", "com.hsinmerit")
                .addServerData("verCode", "9")
                .build()
                .check(new VersionChecker.SubCheck() {
                    @Override
                    public void onChecked(Object o) {

                    }
                });

    }


    @Override
    protected void init() {

    }
}

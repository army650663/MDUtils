package tw.idv.madmanchen.mdutils;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import tw.idv.madmanchen.mdutilslib.base.BaseActivity;

public class MainActivity extends BaseActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, new SubReqPermission() {
            @Override
            public void repResult(boolean isGet) {
                Log.e("isGet", isGet + "");
            }
        });
    }

    @Override
    protected void init() {

    }
}

package tw.idv.madmanchen.mdutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import tw.idv.madmanchen.mdutilslib.base.BaseActivity;

public class MainActivity extends BaseActivity {
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("onCreate", "onCreate");
        mContext = this;
        saveValue().putString("acc", "hhhiii");
        saveValue().commit();
        Log.e("test", getValue().getString("acc", "null"));
        SharedPreferences preferences = getSharedPreferences("default", MODE_PRIVATE);
        preferences.edit().putString("acc", "jjjkkk").commit();
        preferences.edit().apply();

    }


    @Override
    protected void init() {

    }
}

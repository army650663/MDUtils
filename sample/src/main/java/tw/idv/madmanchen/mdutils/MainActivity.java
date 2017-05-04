package tw.idv.madmanchen.mdutils;

import android.content.Context;
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


    }


    @Override
    protected void init() {

    }
}

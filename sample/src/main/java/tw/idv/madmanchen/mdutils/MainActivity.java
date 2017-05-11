package tw.idv.madmanchen.mdutils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import tw.idv.madmanchen.mdutilslib.base.BaseActivity;

public class MainActivity extends BaseActivity {
    public static final String TAG = "MainActivity";
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

    }

    @Override
    protected void init() {

    }
}

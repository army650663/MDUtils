package tw.idv.madmanchen.mdutils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;

import tw.idv.JSONException;
import tw.idv.JSONObject;
import tw.idv.madmanchen.mdhttpasynctasklib.MDHttpAsyncTask;
import tw.idv.madmanchen.mdutilslib.base.VersionChecker;

public class MainActivity extends Activity {
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("onCreate", "onCreate");
        mContext = this;
        new VersionChecker.Builder()
                .setServerUrl("http://www.xingmerit.com.cn/appupdate/")
                .setCheckType(VersionChecker.SERVER)
                .addServerData("pkgName", "com.agenttw")
                .addServerData("verCode", "27")
                .setUpdateView(mContext, "", "", "up", false)
                .build()
                .check(new VersionChecker.SubCheck() {
                    @Override
                    public void onChecked(Object result) {
                        Log.e("versionChecker", result.toString());
                    }
                });

        HashMap<String, String> postData = new HashMap<>();
        postData.put("acc", "MADMANCHEN");
        postData.put("psd", "Codeing");
        postData.put("page", "branchsale");
        postData.put("act", "branchsale_getdata");

       new MDHttpAsyncTask.Builder()
                .load("http://pub.mysoqi.com/ht_analy/0028/")
                .addPostData(postData)
                .setLoadingView(mContext, "", "loading")
                .build()
                .startAll(new MDHttpAsyncTask.SubResponse() {
                    @Override
                    public void onResponse(Object o) {
                        if (o != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(o.toString());
                                boolean result = jsonObject.optBoolean("result");
                                String msg = jsonObject.optString("msg");
                                Log.e("json", jsonObject.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }



}

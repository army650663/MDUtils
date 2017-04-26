package tw.idv.madmanchen.mdutilslib.base;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.idv.madmanchen.mdutilslib.utils.FileUtils;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/4/24      chenshaowei         V1.0.0          Create
 * Why & What is modified:
 */

public class VersionChecker extends AsyncTask<String, Void, Object> {
    private static final String CHECK_URL = "http://public.hsinten.com.tw/appupdate/?pkgName=com.agenttw&verCode=28&verName=1.28.060";

    private String mServerUrl;
    private HashMap<String, String> mServerDataMap;
    private HashMap<String, String> mGooglePlayDataMap;
    private HashMap<String, String> mUpdateSettingMap;
    private int mType;
    private AlertDialog.Builder mUpdateView;
    private List<SubCheck> mSubCheckList;

    public static final int GOOGLE_PLAY = 0;
    public static final int SERVER = 1;

    @IntDef({GOOGLE_PLAY, SERVER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public interface SubCheck {
        void onChecked(Object result);
    }

    private VersionChecker(Builder builder) {
        mServerUrl = builder.mServerUrl;
        mServerDataMap = builder.mServerDataMap;
        mGooglePlayDataMap = builder.mGooglePlayDataMap;
        mUpdateSettingMap = builder.mUpdateSettingMap;
        mType = builder.mType;
        mUpdateView = builder.mUpdateView;

        mSubCheckList = new ArrayList<>();
    }

    private String checkGooglePlay() {
        String verName = null;
        try {
            verName = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mGooglePlayDataMap.get("pkgName"))
                    .timeout(10000)
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return verName;
    }

    private Object checkServer() {
        return new MDHttpAsyncTask.Builder()
                .load(mServerUrl)
                .addPostData(mServerDataMap)
                .build().getResult(false);
    }

    public void check(@Nullable SubCheck subCheck) {
        if (subCheck != null) {
            mSubCheckList.add(subCheck);
        }
        executeOnExecutor(MDHttpAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Object doInBackground(String... params) {
        Looper.prepare();
        switch (mType) {
            case GOOGLE_PLAY:
                return checkGooglePlay();

            case SERVER:
                return checkServer();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Object object) {
        super.onPostExecute(object);
        for (SubCheck subCheck : mSubCheckList) {
            subCheck.onChecked(object);
        }
        if (mUpdateView != null) {
            switch (mType) {
                case GOOGLE_PLAY:
                    String verName = object.toString();
                    if (!verName.equals(mGooglePlayDataMap.get("verName"))) {
                        mUpdateView.setNegativeButton(mUpdateSettingMap.get("uBtnText"), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                try {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mGooglePlayDataMap.get("pkgName")));
                                } catch (ActivityNotFoundException e) {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + mGooglePlayDataMap.get("pkgName")));
                                }
                                mUpdateView.getContext().startActivity(intent);
                            }
                        });
                        mUpdateView.show();
                    }
                    break;

                case SERVER:
                    try {
                        JSONObject jsonObject = new JSONObject(object.toString());
                        boolean result = jsonObject.optBoolean("result");
                        if (!result) {
                            String msg = jsonObject.optString("msg");
                            if (msg.equals("err_ver")) {
                                JSONObject infoJObj = jsonObject.optJSONObject("info");
                                final String apkUrl = infoJObj.optString("apkUrl");
                                mUpdateView.setNegativeButton(mUpdateSettingMap.get("uBtnText"), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        new MDHttpAsyncTask.Builder()
                                                .load(apkUrl)
                                                .setLoadingView(mUpdateView.getContext(), "", "")
                                                .setRequestType(MDHttpAsyncTask.FILE)
                                                .setDownloadPath(mUpdateView.getContext().getFilesDir().getPath())
                                                .cancelable(false)
                                                .build()
                                                .startAll(new MDHttpAsyncTask.SubResponse() {
                                                    @Override
                                                    public void onResponse(Object data) {
                                                        if (data != null) {
                                                            File file = (File) data;
                                                            FileUtils.smartOpenFile(mUpdateView.getContext(), file);
                                                        }
                                                    }
                                                });
                                    }
                                });
                                mUpdateView.show();
                            } else {
                                Toast.makeText(mUpdateView.getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

    }

    public static final class Builder {
        private String mServerUrl;
        private HashMap<String, String> mServerDataMap;
        private HashMap<String, String> mGooglePlayDataMap;
        private HashMap<String, String> mUpdateSettingMap;
        private int mType;
        private AlertDialog.Builder mUpdateView;

        public Builder() {
            mServerDataMap = new HashMap<>();
            mGooglePlayDataMap = new HashMap<>();
            mUpdateSettingMap = new HashMap<>();
            mType = 0;
        }

        public Builder addGooglePlayInfo(String pkgName, String verName) {
            mGooglePlayDataMap.put("pkgName", pkgName);
            mGooglePlayDataMap.put("verName", verName);
            return this;
        }

        public Builder setServerUrl(String url) {
            mServerUrl = url;
            return this;
        }

        public Builder addServerData(String key, String value) {
            mServerDataMap.put(key, value);
            return this;
        }

        public Builder addServerData(HashMap<String, String> dataMap) {
            mServerDataMap.putAll(dataMap);
            return this;
        }

        public Builder setCheckType(@Type int type) {
            mType = type;
            return this;
        }

        public Builder setUpdateView(final Context context, String title, String msg, String updateBtnText, boolean cancelable) {
            mUpdateView = new AlertDialog.Builder(context);
            mUpdateView.setTitle(title).setMessage(msg).setCancelable(cancelable);
            mUpdateSettingMap.put("uBtnText", updateBtnText);
            return this;
        }

        public VersionChecker build() {
            return new VersionChecker(this);
        }
    }

}

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

import tw.idv.madmanchen.mdhttpasynctasklib.MDHttpAsyncTask;
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
    private AlertDialog.Builder mUpdateView;

    // 版本檢查網址
    private String mServerUrl;
    // 要傳送給伺服器的資料
    private HashMap<String, String> mServerDataMap;
    // 要比對 Google play 版本資料
    private HashMap<String, String> mGooglePlayDataMap;
    // Update view 設定資料
    private HashMap<String, String> mUpdateSettingMap;
    // 檢查的類別 Google play or Server
    private int mType;
    // 檢查結果回傳
    private List<SubCheck> mSubCheckList;

    // 檢查類型設定 以Android annotation 代替 Enum
    public static final int GOOGLE_PLAY = 0;
    public static final int SERVER = 1;

    @IntDef({GOOGLE_PLAY, SERVER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    // 檢查結果回傳介面
    public interface SubCheck {
        void onChecked(Object result);
    }

    /**
     * 建構子
     *
     * @param builder 以 Builder 型式建構
     */
    private VersionChecker(Builder builder) {
        mServerUrl = builder.mServerUrl;
        mServerDataMap = builder.mServerDataMap;
        mGooglePlayDataMap = builder.mGooglePlayDataMap;
        mUpdateSettingMap = builder.mUpdateSettingMap;
        mType = builder.mType;
        mUpdateView = builder.mUpdateView;
        mSubCheckList = new ArrayList<>();
    }

    /**
     * 檢查 Google play 版本
     * <p>使用 Jsoup 抓取 Google play html 版本名稱 tag</p>
     */
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

    /**
     * 檢查 Server 版本
     * <p>傳入參數由 PHP 取得版本</p>
     */
    private Object checkServer() {
        return new MDHttpAsyncTask.Builder()
                .load(mServerUrl)
                .addPostData(mServerDataMap)
                .build().getResult(false);
    }

    /**
     * 開始檢查版本
     *
     * @param subCheck 回傳介面
     */
    public void check(@Nullable SubCheck subCheck) {
        if (subCheck != null) {
            mSubCheckList.add(subCheck);
        }
        executeOnExecutor(MDHttpAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Object doInBackground(String... params) {
        // Handler looper 初始化
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
    protected void onPostExecute(Object object) {
        super.onPostExecute(object);
        for (SubCheck subCheck : mSubCheckList) {
            subCheck.onChecked(object);
        }
        // 判斷是否顯示更新視窗
        if (mUpdateView != null && object != null) {
            switch (mType) {
                case GOOGLE_PLAY:
                    String verName = object.toString();
                    if (!verName.equals(mGooglePlayDataMap.get("verName"))) {
                        mUpdateView.setNegativeButton(mUpdateSettingMap.get("uBtnText"), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 開啟 Google play 程式
                                Intent intent;
                                try {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mGooglePlayDataMap.get("pkgName")));
                                } catch (ActivityNotFoundException e) {
                                    // 以 Url 型式開啟 Google play 網頁
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
                            // 檢查錯誤原因
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

        /**
         * 建構子
         */
        public Builder() {
            mServerDataMap = new HashMap<>();
            mGooglePlayDataMap = new HashMap<>();
            mUpdateSettingMap = new HashMap<>();
            mType = 0;
        }

        /**
         * 加入 Google play 版本檢查參數
         *
         * @param pkgName 包名
         * @param verName 版本名稱
         */
        public Builder addGooglePlayInfo(String pkgName, String verName) {
            mGooglePlayDataMap.put("pkgName", pkgName);
            mGooglePlayDataMap.put("verName", verName);
            return this;
        }

        /**
         * 設定版本檢查 Server url
         *
         * @param url server url
         */
        public Builder setServerUrl(String url) {
            mServerUrl = url;
            return this;
        }

        /**
         * 加入 Server 檢查參數
         *
         * @param key   鍵值
         * @param value 值
         */
        public Builder addServerData(String key, String value) {
            mServerDataMap.put(key, value);
            return this;
        }

        /**
         * 加入 Server 檢查參數
         *
         * @param dataMap 參數 Map
         */
        public Builder addServerData(HashMap<String, String> dataMap) {
            mServerDataMap.putAll(dataMap);
            return this;
        }

        /**
         * 設定檢查的類型
         *
         * @param type Google play or Server
         */
        public Builder setCheckType(@Type int type) {
            mType = type;
            return this;
        }

        /**
         * 設定更新視窗
         *
         * @param context       context
         * @param title         標題
         * @param msg           訊息
         * @param updateBtnText 上傳按鈕的文字
         * @param cancelable    是否可取消
         */
        public Builder setUpdateView(final Context context, String title, String msg, String updateBtnText, boolean cancelable) {
            mUpdateView = new AlertDialog.Builder(context);
            mUpdateView.setTitle(title).setMessage(msg).setCancelable(cancelable);
            mUpdateSettingMap.put("uBtnText", updateBtnText);
            return this;
        }

        /**
         * 取得 VersionChecker 實體
         */
        public VersionChecker build() {
            return new VersionChecker(this);
        }
    }

}

package tw.idv.madmanchen.mdutilslib.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/5/2      chenshaowei         V1.0.0          Create
 * What is modified:
 */

public class IntentUtils {
    /**
     * 開啟瀏覽器 Intent
     *
     * @param url 網址
     */
    public static Intent openWebViewIntent(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }

    /**
     * 開啟 App 詳細設定頁
     *
     * @param packageName 包名
     */
    public static Intent openAppDetailSettingIntent(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        return intent;
    }

    /**
     * 撥打電話 Intent
     *
     * @param phoneNumber 電話號碼
     */
    public static Intent callIntent(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        return intent;
    }

    /**
     * 發送簡訊
     *
     * @param phoneNumber 電話號碼
     * @param message     文字訊息
     */
    public static Intent sendSmsIntent(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber));  // This ensures only SMS apps respond
        intent.putExtra("sms_body", message);
        return intent;
    }


    /**
     * 開啟檔案 Intent
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param file    要開啟的檔案
     */
    public static Intent openFileIntent(Context context, File file) {
        Intent openIntent = new Intent();
        openIntent.setAction(Intent.ACTION_VIEW);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = HttpURLConnection.guessContentTypeFromName(file.getName());
        openIntent.setDataAndType(getUriFromFile(context, file), type);
        return openIntent;
    }

    public static Intent openAppIntent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * 分享文字 Intent
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param text    文字
     */
    public static Intent shareTextIntent(Context context, String text) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    /**
     * 分享檔案 Intent
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param file    檔案
     */
    public static Intent shareFileIntent(Context context, File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = HttpURLConnection.guessContentTypeFromName(file.getName());
        shareIntent.setDataAndType(getUriFromFile(context, file), type);
        return shareIntent;
    }

    /**
     * 分享圖片 Intent
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param image   圖片檔案
     */
    public static Intent shareImageIntent(Context context, File image) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(getUriFromFile(context, image), "image/*");
        return shareIntent;
    }

    /**
     * 分享多張圖片 Intent
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param images  圖片檔案陣列
     */
    public static Intent shareImageIntent(Context context, File... images) {
        Intent shareIntent = new Intent();
        ArrayList<Uri> fileUris = new ArrayList<>();
        for (File image : images) {
            Uri uri = getUriFromFile(context, image);
            fileUris.add(uri);
        }
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
        shareIntent.setType("image/*");
        return shareIntent;
    }

    /**
     * 依照系統版本取得檔案 Uri
     *
     * @param context 用於 N 版本以上取得 Uri
     * @param file    檔案
     */
    private static Uri getUriFromFile(Context context, File file) {
        return getUriFromFile(context, file, "fileProvider");
    }

    private static Uri getUriFromFile(Context context, File file, String providerName) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + "." + providerName, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 是否有可開啟的 Intent
     *
     * @param context Context
     * @param intent  Intent 類型
     */
    public static boolean haveIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    /**
     * 取得過濾後的分享 Intent
     *
     * @param context          Context
     * @param prototype        Intent 類型
     * @param forbiddenChoices 需要過濾的包名
     */
    private static Intent generateCustomChooserIntent(Context context, String title, Intent prototype, String[] forbiddenChoices) {
        List<Intent> targetedShareIntents = new ArrayList<>();
        List<HashMap<String, String>> intentMetaInfo = new ArrayList<>();
        Intent chooserIntent;

        Intent dummy = new Intent(prototype.getAction());
        dummy.setType(prototype.getType());
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(dummy, 0);

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                if (resolveInfo.activityInfo == null || Arrays.asList(forbiddenChoices).contains(resolveInfo.activityInfo.packageName))
                    continue;

                HashMap<String, String> info = new HashMap<>();
                info.put("packageName", resolveInfo.activityInfo.packageName);
                info.put("className", resolveInfo.activityInfo.name);
                info.put("simpleName", String.valueOf(resolveInfo.activityInfo.loadLabel(context.getPackageManager())));
                intentMetaInfo.add(info);
            }

            if (!intentMetaInfo.isEmpty()) {
                // sorting for nice readability
                Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {
                    @Override
                    public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
                        return map.get("simpleName").compareTo(map2.get("simpleName"));
                    }
                });

                // create the custom intent list
                for (HashMap<String, String> metaInfo : intentMetaInfo) {
                    Intent targetedShareIntent = (Intent) prototype.clone();
                    targetedShareIntent.setPackage(metaInfo.get("packageName"));
                    targetedShareIntent.setClassName(metaInfo.get("packageName"), metaInfo.get("className"));
                    targetedShareIntents.add(targetedShareIntent);
                }

                chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), title);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                return chooserIntent;
            }
        }

        return Intent.createChooser(prototype, title);
    }

}

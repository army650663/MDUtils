package tw.idv.madmanchen.mdutilslib.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Author:      chenshaowei.
 * Version      V1.0
 * Date:        2016/11/14
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2016/11/14      chenshaowei         V1.0            Create
 * Why & What is modified:
 */

public class BaseUtils {

    public static void keepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void showAlert(Context context, String title, String massage, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(massage);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showToast(Context context, String massage) {
        Toast.makeText(context, massage, Toast.LENGTH_SHORT).show();
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }


    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    }

    public static boolean openAppFromPackageName(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void openGooglePlayFromPackageName(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        if (!IntentUtils.haveIntent(context, intent)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
        }
        context.startActivity(intent);
    }
}

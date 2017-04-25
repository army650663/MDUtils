package tw.idv.madmanchen.library.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Author:      chenshaowei.
 * Version      V1.0
 * Date:        2016/11/10
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2016/11/10      chenshaowei         V1.0            Create
 * Why & What is modified:
 */

public class FileUtils {
    /**
     * 儲存字串檔
     *
     * @param file : 儲存檔案位置
     * @param data : 要寫入的字串
     */
    public static void saveStringToFile(File file, String data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 讀取字串檔
     *
     * @param file : 檔案位置
     * @return String
     */
    public static String readStringByFile(File file) {
        String data = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                data += strLine;
            }
            fis.close();
            in.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void smartOpenFile(Context context, File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, HttpURLConnection.guessContentTypeFromName(file.getName()));
        context.startActivity(intent);
    }
}

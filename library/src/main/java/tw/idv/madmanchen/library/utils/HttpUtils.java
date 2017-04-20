package tw.idv.madmanchen.library.utils;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import madmanchen.idv.tw.mmc_lib.R;

/**
 * Author:      chenshaowei.
 * Version      V1.0
 * Date:        2016/11/8
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2016/11/8      chenshaowei         V1.0            Create
 * Why & What is modified:
 */

public class HttpUtils extends AsyncTask<Object, Integer, Object> {

    // 基本屬性
    private Context mContext;
    private String method = "GET";
    private ProgressDialog mProgressDialog;
    private HttpURLConnection connection;
    private String url;
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS;
    private int request;
    private HashMap<String, String> params;
    private Response response;
    private CompleteReceiver completeReceiver;

    // 初始化連線用
    private static final int READ_TIME_OUT = 60000;
    private static final int CONN_TIME_OUT = 60000;

    // 初始化對話框用
    private boolean isShow = true;

    // 請求類型
    public static final int DEFAULT = 0;
    public static final int STRING = 1;
    public static final int DOWNLOAD = 2;
    public static final int MULTI_DOWNLOAD = 3;
    public static final int DOWNLOAD_IMAGE = 4;
    public static final int MULTI_DOWNLOAD_IMAGE = 5;
    public static final int MULTIPART = 6;

    // 用於連線返回數據的介面
    public interface Response {
        void onReceive(Object o);
    }

    // 上傳檔案
    private String[] names;
    private File[] files;

    // 下載多個檔案，可顯示進度
    private String[] urls;
    private boolean isOverwrite = true;

    // 下載圖片專用
    private int maxWidth = 0;
    private int maxHeight = 0;
    private int quality = 100;

    public HttpUtils(Context context, @REQUEST int request) {
        this.mContext = context;
        this.request = request;
        initConnDialog(mContext);
    }

    /**
     * 建構
     *
     * @param context : Context
     * @param url     : 連線位置
     * @param request : 請求類型
     */
    public HttpUtils(Context context, String url, @REQUEST int request) {
        this.mContext = context;
        this.request = request;
        this.url = url;
        initConnDialog(mContext);
    }

    public HttpUtils(String url, @REQUEST int request) {
        this.request = request;
        this.url = url;
    }

    /**
     * 初始化連線數據
     */
    private void initConnection() {
        try {
            /*
             *  判斷Http method 是否為 GET 且參數不為空
             *  true :  將參數值轉換為URL字串
             * */
            if (method.equals("GET") && params != null) {
                url += "?" + paramsToString(params);
            }
            URL fUrl = new URL(url);
            connection = (HttpURLConnection) fUrl.openConnection();
            connection.setReadTimeout(READ_TIME_OUT);
            connection.setConnectTimeout(CONN_TIME_OUT);
        } catch (Exception e) {
            Log.e(url, e.toString());
        }
    }

    /**
     * 初始化連線對話框
     *
     * @param context : Context
     */
    private void initConnDialog(final Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(context.getString(R.string.sysInfo));
        mProgressDialog.setMessage(context.getString(R.string.conning));
        mProgressDialog.setCancelable(false);
        // 判斷請求為下載，顯示下載進度
        if (request == DOWNLOAD || request == MULTI_DOWNLOAD) {
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(context.getString(R.string.downloading));
            if (request == DOWNLOAD) {
                mProgressDialog.setProgressNumberFormat("%d KB / %d KB");
            } else {
                mProgressDialog.setProgressNumberFormat("%d / %d");
            }
        }
    }

    /**
     * 設定連線對話框
     *
     * @param title   : 對話框標題
     * @param message : 對話框訊息
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils setConnDialog(String title, String message) {
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        return this;
    }

    /**
     * 設定Http method
     *
     * @param method : GET POST...
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 設定對話框
     *
     * @param isShow : 是否顯示對話框
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils showConnDialog(boolean isShow) {
        this.isShow = isShow;
        return this;
    }

    /**
     * 設定對話框
     *
     * @param cancelable :
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils cancelable(boolean cancelable) {
        if (cancelable) {
            mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HttpUtils.this.cancel(false);
                }
            });
        }
        return this;
    }

    /**
     * 新增參數
     *
     * @param params : 集成參數 HashMap
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils addParams(HashMap<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * Http上傳檔案
     *
     * @param names : 鍵值名稱
     * @param files : 上傳檔案
     * @return 返回 HttpUtils 主體
     */
    public HttpUtils addMultipartFile(String[] names, File[] files) {
        this.names = names;
        this.files = files;
        return this;
    }

    /**
     * Http下載多個檔案
     *
     * @param urls : url 陣列
     */
    public HttpUtils addDownloadFileUrls(String[] urls, boolean isOverwrite) {
        this.urls = urls;
        this.isOverwrite = isOverwrite;
        return this;
    }

    public HttpUtils setDownloadFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public HttpUtils setBitmap(int maxWidth, int maxHeight, int quality) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.quality = quality;
        return this;
    }


    /**
     * 開始連線
     *
     * @param response : 接收連線所傳回的介面
     */
    public void start(Response response) {
        this.response = response;
        this.execute();
    }

    public void startMulti(Response response) {
        this.response = response;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 請求返回字串
     *
     * @param params : 集成參數 HashMap
     * @return String : 成功, null : 失敗
     */
    private String requestString(Object params) {
        try {
            connection.setRequestMethod(method);
            /*
             * 判斷參數是否為空，請求方法為POST
             * 將參數寫入Body
             * */
            if (params != null && method.equals("POST")) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(paramsToString(params));
                bw.close();
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return convertStreamToString(connection.getInputStream());
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("err", e.toString());
        } finally {
            connection.disconnect();
        }
        return null;
    }

    /**
     * 下載檔案
     *
     * @return file : 成功, null : 失敗
     */
    private File downloadFile() {
        try {
            connection.setRequestMethod(method);
            if (params != null && method.equals("POST")) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(paramsToString(params));
                bw.close();
            }

            // 取得URL檔案名稱
            String fileName = URLUtil.guessFileName(url, null, null);

            File file = new File(filePath, fileName);
            // 取得檔案長度
            int lengthOfFile = connection.getContentLength();

            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(file);

            int downloadSize = 0;
            // 建立緩衝區
            byte[] buffer = new byte[1024];

            int bufferLength;

            while ((bufferLength = bis.read(buffer)) > 0) {
                fos.write(buffer, 0, bufferLength);
                downloadSize += bufferLength;
                // 顯示下載進度
                onProgressUpdate(downloadSize / 1000, lengthOfFile / 1000);
            }
            fos.flush();
            fos.close();
            bis.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    /**
     * 下載多個檔案
     *
     * @return file[] : 成功, null : 失敗
     */
    private File[] downloadFiles() {
        try {
            File[] files = new File[urls.length];
            for (int i = 0; i < urls.length; i++) {
                URL fUrl = new URL(urls[i]);
                connection = (HttpURLConnection) fUrl.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();

                // 取得URL檔案名稱
                String fileName = URLUtil.guessFileName(urls[i], null, null);

                File file = new File(filePath, fileName);

                if (!isOverwrite) {
                    if (file.exists()) {
                        // 顯示下載進度
                        onProgressUpdate(i, urls.length);
                        continue;
                    }
                }
                // 取得檔案長度
                final int lengthOfFile = connection.getContentLength();

                BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fos = new FileOutputStream(file);

                int downloadSize = 0;
                // 建立緩衝區
                byte[] buffer = new byte[1024];

                int bufferLength;

                while ((bufferLength = bis.read(buffer)) > 0) {
                    fos.write(buffer, 0, bufferLength);
                    downloadSize += bufferLength;
                }

                fos.flush();
                fos.close();
                bis.close();
                files[i] = file;

                // 顯示下載進度
                onProgressUpdate(i, urls.length);
                Log.d("file", file.getPath());
            }

            return files;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用DownloadManager下載檔案
     */
    void downloadFileWithManager(Response response) {
        this.response = response;
        String fileName = URLUtil.guessFileName(url, null, null);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        /*
            此方法表示在下載過程中通知欄會一直顯示該下載，在下載完成後仍然會顯示，直到用戶點擊該通知或者消除該通知。
            還有其他參數可供選擇
        */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 設置下載文件存放的路徑
        // setDestinationUri
        // setDestinationInExternalPublicDir
        // setDestinationInExternalFilesDir
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // 設置一些基本顯示信息
        request.setTitle(fileName);
        request.setDescription("");
        request.setMimeType("application/vnd.android.package-archive");

        completeReceiver = new CompleteReceiver();

        // 註冊廣播接收
        mContext.registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId = dm.enqueue(request);
    }

    private Bitmap downloadBitmap() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());

            int width = maxWidth == 0 ? bitmap.getWidth() : maxWidth;
            int height = maxHeight == 0 ? bitmap.getHeight() : maxHeight;

            if (maxWidth != 0 || maxHeight != 0) {
                bitmap = ImageUtils.resizeBitmapKeepRatio(bitmap, width, height);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Http上傳檔案
     */
    private String multipartRequest() {
        try {
            String twoHyphens = "--";
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            String lineEnd = "\r\n";

            String result;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String name = names[i];
                FileInputStream fis = new FileInputStream(file);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"" + lineEnd);
                dos.writeBytes("Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName()) + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                dos.writeBytes(lineEnd);

                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fis.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                fis.close();
            }

            // 將參數寫入Http from-data
            if (params != null) {
                for (String key : params.keySet()) {
                    String value = params.get(key);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: text/plain" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(value);
                    dos.writeBytes(lineEnd);
                }
            }
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // 取得返回字串
            InputStream is = connection.getInputStream();
            result = convertStreamToString(is);

            is.close();
            dos.flush();
            dos.close();

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mProgressDialog != null && !mProgressDialog.isShowing() && isShow) {
            mProgressDialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mProgressDialog.setMax(values[1]);
        mProgressDialog.setProgress(values[0]);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Log.e("ThreadName", Thread.currentThread().getName());
        initConnection();
        switch (request) {
            case DEFAULT:
                // 用於DownloadManager
                return null;

            case STRING:
                return requestString(params);

            case DOWNLOAD:
                return downloadFile();

            case MULTI_DOWNLOAD:
                return downloadFiles();

            case DOWNLOAD_IMAGE:
                return downloadBitmap();

            case MULTI_DOWNLOAD_IMAGE:
                return null;

            case MULTIPART:
                return multipartRequest();
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
            mProgressDialog.dismiss();
        }
        response.onReceive(o);
    }

    /**
     * 串流轉字串
     *
     * @param is : 輸入的串流
     * @return 返回字串
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 參數轉換為URL字串
     *
     * @param params : 參數的Object
     * @return 返回字串
     */
    private String paramsToString(Object params) {
        if (params instanceof HashMap) {
            HashMap<String, String> fParams = (HashMap<String, String>) params;
            StringBuilder sb = new StringBuilder();
            boolean isFront = true;
            for (HashMap.Entry<String, String> entry : fParams.entrySet()) {
                if (isFront) {
                    isFront = false;
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
            return sb.toString();
        } else {
            return params.toString();
        }
    }

    @IntDef({DEFAULT, STRING, DOWNLOAD, MULTI_DOWNLOAD, DOWNLOAD_IMAGE, MULTI_DOWNLOAD_IMAGE, MULTIPART})
    @Retention(RetentionPolicy.SOURCE)
    @interface REQUEST {

    }

    private class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get complete download id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(completeDownloadId);
            Cursor c = dm.query(query);
            if (c != null) {
                if (c.moveToFirst()) {
                    String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                    File file = new File(fileUri);
                    response.onReceive(file);
                }
                mContext.unregisterReceiver(completeReceiver);
                c.close();
            }

        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}


package tw.idv.madmanchen.mdutilslib.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Author:      chenshaowei.
 * Version      V1.0
 * Date:        2017/2/6
 * Description: Support web view can upload img
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/2/6      chenshaowei         V1.0            Create
 * Why & What is modified:
 */

public class BaseWebChromeClient extends WebChromeClient {
    private Activity activity;
    private Context context;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private String mCameraPhotoPath;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    /**
     * Constructor
     *
     * @param context Use activity onResult and cast to Activity
     */
    public BaseWebChromeClient(Context context) {
        this.context = context;
        this.activity = (Activity) context;
    }


    /**
     * WebView fileChooser(Android < 3.0)
     *
     * @param valueCallback Upload callback
     */
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        uploadMessage = null;
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    /**
     * WebView fileChooser(Android  >= 3.0)
     *
     * @param valueCallback Upload callback
     * @param acceptType    MimeType
     */
    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    /**
     * WebView fileChooser(Android  >= 4.1)
     *
     * @param valueCallback Upload callback
     * @param acceptType    MimeType
     * @param capture       Capture
     */
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    /**
     * WebView fileChooser(Android >= 5.0)
     *
     * @param webView WebView
     * @param filePathCallback Upload callback
     * @param fileChooserParams ChooserParams
     * */
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        uploadMessageAboveL = filePathCallback;
        openImageChooserActivity();
        return true;
    }

    /**
     * Create take photo intent and pick photo intent
     * */
    private void openImageChooserActivity() {
        mCameraPhotoPath = null;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePhotoIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePhotoIntent.putExtra("PhotoPath", mCameraPhotoPath);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickPhotoIntent.setType("image/*");

        Intent[] intents = {
                takePhotoIntent
        };
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, pickPhotoIntent);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        try {
            activity.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                onActivityResultAboveL(data);
            } else {
                if (uploadMessage == null) return;

                Uri result = data.getData();
                if (result == null && mCameraPhotoPath != null) {
                    result = Uri.parse(mCameraPhotoPath);
                }
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        } else {
            // Avoid WebView block
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(Intent data) {
        if (uploadMessageAboveL == null) return;
        Uri[] uris = null;
        if (data != null) {
            String dataStr = data.getDataString();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                uris = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    uris[i] = item.getUri();
                }
            } else if (dataStr != null) {
                uris = new Uri[]{Uri.parse(dataStr)};
            }
        } else {
            uris = new Uri[]{Uri.parse(mCameraPhotoPath)};
        }
        uploadMessageAboveL.onReceiveValue(uris);
        uploadMessageAboveL = null;

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

}



package tw.idv.madmanchen.mdutilslib.base;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MadmanChen on 2016/2/16.
 * 自定義 Activity 包含常用方法
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 621;

    public Context mContext = this;
    private SubReqPermission mSubReqPermission;
    private SharedPreferences mDefaultSP;

    public interface SubReqPermission {
        void repResult(boolean isGet);
    }

    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        init();
        initDefaultSP();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        init();
        initDefaultSP();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        init();
        initDefaultSP();
    }

    /**
     * 註冊View及物件
     */
    protected abstract void init();

    private void initDefaultSP() {
        mDefaultSP = getPreferences(MODE_PRIVATE);
    }

    /**
     * 設定螢幕為常開
     */
    public void keepScreenOn() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 隱藏導航列
     */
    @TargetApi(14)
    public void hideNavBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //| View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 隱藏標題列
     */
    public void hideSupActBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * 顯示Toast
     *
     * @param msg 顯示的訊息
     */
    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 顯示普通視窗
     *
     * @param title        視窗標題
     * @param msg          視窗訊息
     * @param cancelBtnTxt 關閉按鈕文字
     */
    public void showAlert(String title, String msg, String cancelBtnTxt) {
        new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(cancelBtnTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    /**
     * Save default value
     *
     * @return SharePreferences editor
     */
    public SharedPreferences.Editor saveValue() {
        return mDefaultSP.edit();
    }

    /**
     * Get default value
     *
     * @return SharePreferences
     */
    public SharedPreferences getValue() {
        return mDefaultSP;
    }


    /**
     * 自定義findViewById
     *
     * @param id ViewId
     */
    public <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    /**
     * 自定義findViewByString
     *
     * @param id ViewIdString
     */
    public <T extends View> T $(String id) {
        int resId = mContext.getResources().getIdentifier(id, "id", getPackageName());
        return (T) findViewById(resId);
    }

    /**
     * 取得定串
     *
     * @param id 字串ID
     * @return String
     */
    public String getString(String id) {
        int resID = mContext.getResources().getIdentifier(id, "string", getPackageName());
        return getString(resID);
    }

    /**
     * 隱藏鍵盤
     */
    public void hideSoftKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    /**
     * 顯示鍵盤
     */
    public void showSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Android 6.0 需向使用者取得權限
     *
     * @param permission       權限名稱
     *                         ex. Manifest.permission.WRITE_EXTERNAL_STORAGE
     * @param subReqPermission 取得權限介面
     */
    public void requestPermission(String permission, SubReqPermission subReqPermission) {
        requestPermissions(new String[]{permission}, subReqPermission);
    }

    /**
     * Android 6.0 需向使用者取得權限
     *
     * @param permissions      權限名稱
     *                         ex. Manifest.permission.WRITE_EXTERNAL_STORAGE
     * @param subReqPermission 取得權限介面
     */
    public void requestPermissions(String[] permissions, SubReqPermission subReqPermission) {
        this.mSubReqPermission = subReqPermission;
        // Check permission
        List<String> list = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
            }
        }
        if (list.size() > 0) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[list.size()]), PERMISSION_CODE);
        } else {
            subReqPermission.repResult(true);
        }
    }

    /**
     * 取得權限後回傳
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                boolean result = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                        break;
                    }
                }
                mSubReqPermission.repResult(result);
                break;
            }
        }
    }
}

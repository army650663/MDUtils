package tw.idv.madmanchen.mdutils;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import tw.idv.madmanchen.mdutilslib.utils.IntentUtils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        File file = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        File myFile = new File(file, "HM9999999999999.jpg");
        startActivity(IntentUtils.shareImageIntent(mContext, myFile, myFile, myFile));

    }
}

package tw.idv.madmanchen.library.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/4/10      chenshaowei         V1.0.0          Create
 * Why & What is modified:
 */

public class BitmapUtils {

    /**
     * 計算 Bitmap inSampleSize
     *
     * @param options   Bitmap 選項
     * @param reqWidth  請求的寬
     * @param reqHeight 請求的高
     * @return inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 縮放圖片
     *
     * @param bitmap    bitmap
     * @param dstWidth  縮放寬
     * @param dstHeight 縮放高
     * @return Bitmap
     */
    private static Bitmap createScaleBitmap(Bitmap bitmap, int dstWidth,
                                            int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
        if (bitmap != dst) {
            bitmap.recycle();
        }
        return dst;
    }

    /**
     * 從 Resources 讀取縮略後的 Bitmap
     *
     * @param res       資源
     * @param resId     資源 ID
     * @param reqWidth  請求寬
     * @param reqHeight 請求高
     * @return Bitmap
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // 不加載 Bitmap 本體 只加載 Bitmap 資訊
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // 計算 inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 加載 Bitmap 本體
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
    }

    /**
     * 從路徑讀取縮略後的 Bitmap
     *
     * @param pathName  檔案路徑
     * @param reqWidth  請求寬
     * @param reqHeight 請求高
     * @return Bitmap
     */
    public static Bitmap decodeSampledBitmapFromFd(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }
}

package tw.idv.madmanchen.mdutilslib.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

/**
 * Author:      MadmanChen.
 * Version      V1.0
 * Date:        2016/6/2
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2016/6/2      MadmanChen         V1.0            Create
 * Why & What is modified:
 *
 */
public class ViewHolder {
    private static final int VIEW_TAG = 1010101010;
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag(VIEW_TAG);
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(VIEW_TAG, viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(Context context, View view, String strID) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag(VIEW_TAG);
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(VIEW_TAG, viewHolder);
        }
        int resID = context.getResources().getIdentifier(strID, "id", context.getPackageName());
        View childView = viewHolder.get(resID);
        if (childView == null) {
            childView = view.findViewById(resID);
            viewHolder.put(resID, childView);
        }
        return (T) childView;
    }
}

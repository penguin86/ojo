package it.danieleverducci.ojo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DpiUtils {
    public static int DpToPixels(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }
}

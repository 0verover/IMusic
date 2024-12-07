package com.realgear.samplemusicplayertest.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DimenRes;

public class ViewsUtil {
    public static int dp2px(Context context, @DimenRes int id) {
        return context.getResources().getDimensionPixelSize(id);
    }

    public static int getStatusBarHeight(Context context) {
        int status_bar_height = 0;

        int status_r_id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(status_r_id > 0)
            status_bar_height = context.getResources().getDimensionPixelSize(status_r_id);

        return  status_bar_height;
    }



    public static int getNavigationBarHeight(Context context) {
        int navigation_bar_height = 0;

        int status_r_id = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if(status_r_id > 0)
            navigation_bar_height = context.getResources().getDimensionPixelSize(status_r_id);

        return  navigation_bar_height;
    }

    public static void setMargins(View view, int left, int top, int right, int bottom, boolean dp2px) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            final float scale = view.getContext().getResources().getDisplayMetrics().density;
            // convert the DP into pixel
            int l =  dp2px ? (int)(left * scale + 0.5f) : left;
            int r =  dp2px ? (int)(right * scale + 0.5f) : right;
            int t =  dp2px ? (int)(top * scale + 0.5f) : top;
            int b =  dp2px ? (int)(bottom * scale + 0.5f) : bottom;

            p.setMargins(l, t, r, b);
            view.requestLayout();
        }
    }
}

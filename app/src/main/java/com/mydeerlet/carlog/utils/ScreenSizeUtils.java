package com.mydeerlet.carlog.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


/**
 * 用到了一个工具类用来计算手机屏幕的宽高
 */
public class ScreenSizeUtils {
    private static ScreenSizeUtils instance = null;
    private static int screenWidth, screenHeight;

    public static ScreenSizeUtils getInstance(Context mContext) {
        if (instance == null) {
            synchronized (ScreenSizeUtils.class) {
                if (instance == null)
                    instance = new ScreenSizeUtils(mContext);
            }
        }
        return instance;
    }

    private ScreenSizeUtils(Context mContext) {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        screenHeight = dm.heightPixels;//获取屏幕分辨率高度
    }

    //获取屏幕宽度
    public static int getScreenWidth() {
        if (screenWidth==0){
            DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
            screenWidth = metrics.widthPixels;
        }
        return screenWidth;
    }

    //获取屏幕高度
    public static int getScreenHeight() {
        if (screenHeight==0){
            DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
            screenHeight = metrics.heightPixels;
        }
        return screenHeight;
    }


    public static int dp2px(int dpValue) {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }
}
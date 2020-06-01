package com.mydeerlet.carlog.utils;

/**
 * @author myDeerlet
 * @date 2020/5/30.
 * email：kuaileniaofei@163.com
 * description：
 */
public class Utils {

    /**
     * 将时长秒 转化为分秒的形式
     * @param second 总时长秒
     * @return "00:00"
     */
    public static String longToString(Long second) {
        Long mMinute = second / 60;
        Long mSecond = second % 60;
        String strMinuter = mMinute > 9 ? "" + mMinute : "0" + mMinute;
        String strSecond = mSecond > 9 ? "" + mSecond : "0" + mSecond;
        return strMinuter + ":" + strSecond;
    }
}

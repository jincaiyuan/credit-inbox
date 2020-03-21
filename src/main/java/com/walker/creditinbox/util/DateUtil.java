package com.walker.creditinbox.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    /**
     * 获取当天日期的字符串
     * @param pattern 格式
     * @return 当天日期的字符串
     */
    public static String getCurrentDateString(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }
}

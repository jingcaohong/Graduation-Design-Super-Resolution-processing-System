package Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jingc on 2018/3/29.
 *
 * 时间工具
 */
public class DateUtil {

    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static String getTimeStamp(){
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        String time = df.format(date);
        return time;
    }
}

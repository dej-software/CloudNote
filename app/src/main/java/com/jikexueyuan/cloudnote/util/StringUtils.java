package com.jikexueyuan.cloudnote.util;

import android.os.Environment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dej on 2016/12/6.
 * 与字符有关的操作
 */

public class StringUtils {

    public static String resPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.CloudNote/Resource/";
    public static String unloginResPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.CloudNote/Resource/unlogin/";
    public static String tmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.CloudNote/Temp/";

    /**
     * 获取相应格式的当前系统时间
     *
     * @param format
     * @return
     */
    public static String getDateTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }

    /**
     * 获取字符串的MD5 与PHP端获取的一致
     *
     * @param srcStr
     * @return
     */
    public static String md5(String srcStr) {
        String md5Str = null;
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(srcStr.getBytes());
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                if ((b & 0xff) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xff));
            }
            md5Str = hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return md5Str;
    }
}

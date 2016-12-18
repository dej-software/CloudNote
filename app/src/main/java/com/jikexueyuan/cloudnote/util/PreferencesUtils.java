package com.jikexueyuan.cloudnote.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dej on 2016/12/12.
 * 用户信息配置相关操作
 */

public class PreferencesUtils {

    public static final String USER = "user";
    public static final String PASS = "pass";
    public static final String IS_LOGIN = "is_login";

    private static String user;
    private static String pass;
    private static boolean isLogin;

    private static SharedPreferences preferences = null;
    private static SharedPreferences.Editor editor = null;

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        PreferencesUtils.user = user;
    }

    public static String getPass() {
        return pass;
    }

    public static void setPass(String pass) {
        PreferencesUtils.pass = pass;
    }

    public static boolean isLogin() {
        return isLogin;
    }

    public static void setIsLogin(boolean isLogin) {
        PreferencesUtils.isLogin = isLogin;
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static SharedPreferences.Editor getEditor() {
        return editor;
    }

    /**
     * 初始化配置数据
     *
     * @param context
     */
    public static void init(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        setIsLogin(preferences.getBoolean(IS_LOGIN, false));
        if (isLogin()) {
            setUser(preferences.getString(USER, "unlogin"));
            setPass(preferences.getString(PASS, ""));
        }
    }

    /**
     * 把配置数据提交
     *
     * @return
     */
    public static boolean commit() {
        editor.putString(USER, getUser());
        editor.putString(PASS, getPass());
        editor.putBoolean(IS_LOGIN, isLogin());
        return editor.commit();
    }
}

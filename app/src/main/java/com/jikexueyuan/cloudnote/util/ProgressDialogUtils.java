package com.jikexueyuan.cloudnote.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by dej on 2016/12/11.
 * 进度对话框操作
 */

public class ProgressDialogUtils {

    private static ProgressDialog progressDialog = null;

    /**
     * 显示进度对话框
     *
     * @param context
     * @param title
     * @param message
     */
    public static void showProgressDialog(Context context, String title, String message) {
        System.out.println("showProgressDialog");
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(context, title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }

        progressDialog.show();
    }

    /**
     * 隐藏进度对话框
     */
    public static void hideProgressDialog() {
        System.out.println("hideProgressDialog");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}

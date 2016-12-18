package com.jikexueyuan.cloudnote.util;

import android.graphics.Bitmap;
import android.util.Base64;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by dej on 2016/12/12.
 * 与服务器的交互
 */

public class ServerUtils {

    public static String ACTION_GET_DATA = "get_data"; // 获取数据
    public static String ACTION_DELETE = "delete_note"; //删除笔记
    public static String ACTION_SYNC = "sync_note"; // 同步笔记

    //    public static String serverAddress = "http://10.0.2.2/wordpress/"; // 安卓虚拟机测试使用地址
        public static String serverAddress = "http://192.168.10.102/wordpress/"; // 真机测试电脑IP或者你的实际服务器
//    public static String serverAddress = "http://www.dejwind.com/"; // 真机测试电脑IP或者你的实际服务器
    public static String loginAddress = serverAddress + "note-login.php";
    public static String resAddress = serverAddress + "file-upload.php";
    public static String noteDbAddress = serverAddress + "note-db.php";
    public static String resDirAddress = serverAddress + "note_res/";


    /**
     * 上传图像到服务器
     *
     * @param bitmap
     * @param imgName
     */
    public static void uploadImage(Bitmap bitmap, String imgName) {

        // 把图像数据转换为Base64字符数据 Base64最大只支持3M
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        String img = new String(Base64.encodeToString(bytes, Base64.DEFAULT));

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("dir", PreferencesUtils.getUser() + "_res");
        params.add("file", imgName);
        params.add("img", img);
        client.post(resAddress, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("sendImage onSuccess");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("sendImage onFailure");
            }

        });
    }

    /**
     * 上传文件到服务器
     *
     * @param fileName
     */
    public static void uploadFile(final String fileName) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String dir = PreferencesUtils.getUser() + "_res";
        File file = new File(StringUtils.resPath + PreferencesUtils.getUser() + "/" + fileName);

        if (!file.exists()) {
            System.out.println("文件不存在");
        }

        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
            e.printStackTrace();
        }
        params.add("dir", PreferencesUtils.getUser() + "_res");

        client.post(resAddress, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("uploadFile " + fileName + " onSuccess");
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("uploadFile onFailure");
            }
        });
    }

    /**
     * 从服务器下载文件
     *
     * @param fileName
     */
    public static void downloadFile(String fileName) {

        AsyncHttpClient client = new AsyncHttpClient();

        // 创建本地目录
        File dir = new File(StringUtils.resPath + PreferencesUtils.getUser() + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(StringUtils.resPath + PreferencesUtils.getUser() + "/" + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        client.get(resDirAddress + PreferencesUtils.getUser() + "_res/" + fileName, new FileAsyncHttpResponseHandler(file) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                System.out.println("downloadFile onFailure");
                System.out.println(file.getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                System.out.println("downloadFile onSuccess");
            }
        });
    }

    /**
     * 同步数据到服务器
     *
     * @param action
     * @param noteData
     */
    public static void syncNoteData(String action, JSONArray noteData) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.add("action", action);
        params.add("table", PreferencesUtils.getUser() + "_notes");
        System.out.println(noteData.toString());
        params.add("note_data", new String(Base64.encodeToString(noteData.toString().getBytes(), Base64.DEFAULT)));
        client.post(noteDbAddress, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("sendNoteData onSuccess: " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("sendNoteData onFailure");
            }
        });
    }
}

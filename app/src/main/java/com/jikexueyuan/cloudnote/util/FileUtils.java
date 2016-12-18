package com.jikexueyuan.cloudnote.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dej on 2016/12/13.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 重命名文件
     *
     * @param dir
     * @param oldName
     * @param newName
     */
    public static void renameFile(String dir, String oldName, String newName) {
        System.out.println("moveFile  dir: " + dir + " oldName: " + oldName + " newName: " + newName);
        File oldFile = new File(dir + oldName);
        if (!oldFile.exists()) {
            Log.i(TAG, "文件不存在");
            return;
        }

        File newFile = new File(dir + newName);
        if (newFile.exists()) {
            System.out.println(newName + " 已经存在");
            return;
        }

        try {
            if (!oldFile.renameTo(newFile)) {
                Log.i(TAG, "文件重命名失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移动文件到指定目录
     *
     * @param file
     * @param dir
     */
    public static void moveFile(String file, String dir) {
        System.out.println("moveFile  " + file + "  " + dir);
        File srcFile = new File(file);
        if (!srcFile.exists()) {
            Log.i(TAG, "文件不存在");
            return;
        }

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            if (dirFile.mkdirs()) {
                Log.i(TAG, "指定目录创建成功");
            }
        }

        try {
            if (!srcFile.renameTo(new File(dir + srcFile.getName()))) {
                Log.i(TAG, "文件移动失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制文件到指定目录
     *
     * @param file
     * @param dir
     * @param newFileName null 或者 "" 表示不使用新名字
     */
    public static void copyFile(String file, String dir, String newFileName) {
        System.out.println("moveFile  " + file + "  " + dir);
        File srcFile = new File(file);
        if (!srcFile.exists()) {
            Log.i(TAG, "文件不存在");
            return;
        }

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            if (dirFile.mkdirs()) {
                Log.i(TAG, "指定目录创建成功");
            }
        }

        File newFile = null;
        if (newFileName == null || "".equals(newFileName)) {
            newFile = new File(dir + srcFile.getName());
        } else {
            newFile = new File(dir + newFileName);
        }

        int bytes = 0;
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];

            while ((bytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytes);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 移动一个目录下的所有文件到指定目录
     *
     * @param srcDir
     * @param destDir
     */
    public static void moveDirAllFile(File srcDir, String destDir) {

        if (!srcDir.isDirectory()) {
            return;
        }

        File[] childFiles = srcDir.listFiles();
        for (File childFile : childFiles) {
            moveFile(childFile.getAbsolutePath(), destDir);
        }
    }

    /**
     * 删除一个目录下的所有文件，保留此目录
     *
     * @param dir
     */
    public static void deleteDirAllFile(File dir) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] childFiles = dir.listFiles();
        for (File childFile : childFiles) {
            deleteFile(childFile);
        }
    }

    /**
     * 删除一个File (文件或目录，删除时包括此目录)
     *
     * @param file
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File childFile : childFiles) {
                if (!deleteFile(childFile)) {
                    return false;
                }
            }
        }

        return file.delete();
    }
}

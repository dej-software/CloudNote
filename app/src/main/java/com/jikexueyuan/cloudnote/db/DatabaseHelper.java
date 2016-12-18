package com.jikexueyuan.cloudnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dej on 2016/12/3.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名
    public static final String DATABASE_NAME = "ulogin@notes";

    public static final String TABLE_NAME = "note";

    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME
            + " (_id bigint(14) primary key,"
            + " title text, date varchar(10), time varchar(10), content text,"
            + " synced boolean default 0, deleted boolean default 0)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        System.out.println("DatabaseHelper");
    }

    public DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, 1);
        System.out.println("DatabaseHelper : " + dbName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("DatabaseHelper onCreate");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

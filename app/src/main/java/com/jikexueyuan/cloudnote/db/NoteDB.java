package com.jikexueyuan.cloudnote.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jikexueyuan.cloudnote.bean.NoteInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dej on 2016/12/3.
 */

public class NoteDB {

    private final DatabaseHelper dbHelper;

    public NoteDB(Context context) {
        super();
        dbHelper = new DatabaseHelper(context);
    }

    public NoteDB(Context context, String userName) {
        super();
        dbHelper = new DatabaseHelper(context, userName + "@notes");
    }

    /**
     * 增加数据
     *
     * @param data
     */
    public void insert(NoteInfo data) {

        String sql = "insert into " + DatabaseHelper.TABLE_NAME;
        sql += "(_id, title, date, time, content, synced, deleted) values(?, ?, ?, ?, ?, ?, ?)";

        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        sqlite.execSQL(sql, new String[]{data.getId() + "",
                data.getTitle(), data.getDate(), data.getTime(),
                data.getContent(), data.isSynced() ? 1 + "" : 0 + "", data.isDeleted() ? 1 + "" : 0 + ""});
        sqlite.close();
    }

    /**
     * 删
     *
     * @param id
     */
    public void delete(long id) {
        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        String sql = ("delete from " + DatabaseHelper.TABLE_NAME + " where _id=?");
        sqlite.execSQL(sql, new Long[]{id});
        sqlite.close();
    }

    /**
     * 改
     *
     * @param data
     */
    public void update(NoteInfo data) {
        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        String sql = ("update " + DatabaseHelper.TABLE_NAME + " set title=?, date=?, time=?, content=?, synced=?, deleted=? where _id=?");
        sqlite.execSQL(sql,
                new String[]{
                        data.getTitle(), data.getDate(),
                        data.getTime(), data.getContent(),
                        data.isSynced() ? 1 + "" : 0 + "",
                        data.isDeleted() ? 1 + "" : 0 + "",
                        data.getId() + ""});
        sqlite.close();
    }

    public List<NoteInfo> query() {
        return query(" ");
    }

    /**
     * 查
     *
     * @param where
     * @return
     */
    public List<NoteInfo> query(String where) {
        SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
        ArrayList<NoteInfo> datas = null;
        datas = new ArrayList<NoteInfo>();
        System.out.println("select * from "
                + DatabaseHelper.TABLE_NAME + where);
        Cursor cursor = sqlite.rawQuery("select * from "
                + DatabaseHelper.TABLE_NAME + where, null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setId(cursor.getLong(0));
            noteInfo.setTitle(cursor.getString(1));
            noteInfo.setDate(cursor.getString(2));
            noteInfo.setTime(cursor.getString(3));
            noteInfo.setContent(cursor.getString(4));
            noteInfo.setSynced(cursor.getInt(5) == 1 ? true : false);
            noteInfo.setDeleted(cursor.getInt(6) == 1 ? true : false);
            datas.add(noteInfo);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();

        return datas;
    }

    /**
     * 重置
     *
     * @param datas
     */
    public void reset(List<NoteInfo> datas) {
        if (datas != null) {
            SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
            // 删除全部
            sqlite.execSQL("delete from " + DatabaseHelper.TABLE_NAME);
            // 重新添加
            for (NoteInfo data : datas) {
                insert(data);
            }
            sqlite.close();
        }
    }

    /**
     * 保存一条数据到本地(若已存在则直接覆盖)
     *
     * @param data
     */
    public void save(NoteInfo data) {
        List<NoteInfo> datas = query(" where _id=" + data.getId());
        if (datas != null && !datas.isEmpty()) {
            System.out.println("save update id: " + data.getId());
            update(data);
        } else {
            System.out.println("save insert id: " + data.getId());
            insert(data);
        }
    }

    public void destroy() {
        dbHelper.close();
    }
}

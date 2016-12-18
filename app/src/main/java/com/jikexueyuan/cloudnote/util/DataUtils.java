package com.jikexueyuan.cloudnote.util;

import com.jikexueyuan.cloudnote.bean.NoteInfo;
import com.jikexueyuan.cloudnote.db.NoteDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by dej on 2016/12/14.
 * 数据相关的处理
 */

public class DataUtils {

    public static final int SERVER_DELETE = 10; // 要在服务器删除的数据
    public static final int LOCAL_DELETE = 11; // 要在本地删除的数据
    public static final int SERVER_SYNC = 12; // 要同步到服务器的数据
    public static final int LOCAL_SYNC = 13; // 要同步到本地的数据

    /**
     * 获取所有正常(没有被标识删除)的笔记数据
     * b并进行排序
     *
     * @param noteDB
     * @return
     */
    public static List<NoteInfo> getNormalData(NoteDB noteDB) {
        List<NoteInfo> noteInfoList = noteDB.query(" where deleted=0");

        Collections.sort(noteInfoList, new Comparator<NoteInfo>() {
                    @Override
                    public int compare(NoteInfo lhs, NoteInfo rhs) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddHH:mm");
                        Date date1 = null;
                        Date date2 = null;
                        try {
                            date1 = format.parse(lhs.getDate() + lhs.getTime());
                            date2 = format.parse(rhs.getDate() + rhs.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (date2.after(date1)) {
                            return 1;
                        }

                        return -1;
                    }
                }
        );

        return noteInfoList;
    }

    /**
     * 获取所有笔记数据
     *
     * @param noteDB
     * @return
     */
    public static List<NoteInfo> getAllData(NoteDB noteDB) {
        return noteDB.query();
    }

    /**
     * 获取未同步的笔记数据
     *
     * @param noteDB
     * @return
     */
    public static List<NoteInfo> getNotSyncData(NoteDB noteDB) {
        return noteDB.query(" where synced=0 and deleted=0");
    }

    /**
     * 获取标识为删除的数据
     *
     * @param noteDB
     * @return
     */
    public static List<NoteInfo> getDeletedData(NoteDB noteDB) {
        return noteDB.query(" where deleted=1");
    }

    /**
     * 把一个NoteInfo列表转换为JSON格式
     *
     * @param data
     * @return
     */
    public static JSONArray convertJson(List<NoteInfo> data) {

        if (data == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for (NoteInfo note : data) {
            JSONObject member = new JSONObject();
            try {
                member.put("_id", note.getId());
                member.put("title", note.getTitle());
                member.put("date", note.getDate());
                member.put("time", note.getTime());
                member.put("content", note.getContent());
                member.put("synced", note.isSynced());
                member.put("deleted", note.isDeleted());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(member);
        }

        return jsonArray;
    }

    /**
     * 把一个JSONArray转为List<NoteInfo>
     *
     * @param data
     * @return
     */
    public static List<NoteInfo> convertList(JSONArray data) {

        if (data == null) {
            return null;
        }

        List<NoteInfo> noteInfoList = new ArrayList<NoteInfo>();
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                noteInfoList.add(new NoteInfo(object.getLong("_id"),
                        object.getString("title"), object.getString("date"),
                        object.getString("time"), object.getString("content"),
                        object.getInt("synced") == 1 ? true : false,
                        object.getInt("deleted") == 1 ? true : false
                ));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return noteInfoList;
    }

    /**
     * 同步数据
     * 同步数据库数据以及资源文件
     *
     * @param actionType
     * @param data
     */
    public static void syncData(int actionType, List<NoteInfo> data, NoteDB noteDB) {
        if (data == null || data.isEmpty()) {
            return;
        }

        switch (actionType) {
            case SERVER_DELETE:
                // 在服务器中删除
                ServerUtils.syncNoteData(ServerUtils.ACTION_DELETE, convertJson(data));
                break;
            case SERVER_SYNC:
                // 在服务器中同步
                ServerUtils.syncNoteData(ServerUtils.ACTION_SYNC, convertJson(data));
                // 把资源文件上传并且把本地设置为已同步
                for (NoteInfo note : data) {

                    Document doc = Jsoup.parse(note.getContent());
                    Elements newElements = doc.select("img");
                    for (Element element : newElements) {
                        String fileName = element.attr("src");
                        ServerUtils.uploadFile(fileName);
                        if (fileName.contains(".mp4")) {
                            System.out.println("上传视频：" + fileName.substring(0, fileName.length() - 4));
                            ServerUtils.uploadFile(fileName.substring(0, fileName.length() - 4));
                        }
                    }

                    note.setSynced(true);
                    noteDB.save(note);
                }
                break;
            case LOCAL_DELETE:
                // 在服务器中为已删除 且在本地中存在
                for (NoteInfo note : data) {
                    deleteNoteFromDB(note, noteDB);
                }
                break;
            case LOCAL_SYNC:
                // 把服务器的更新到本地
                for (NoteInfo note : data) {
                    // TODO 更新资源时需把原先存在本地但服务器数据已经未使用的删除掉
                    // 解析图片相关的信息 下载资源文件
                    Document doc = Jsoup.parse(note.getContent());
                    Elements newElements = doc.select("img");
                    for (Element element : newElements) {
                        String fileName = element.attr("src");
                        ServerUtils.downloadFile(fileName);
                        if (fileName.contains(".mp4")) {
                            ServerUtils.downloadFile(fileName.substring(0, fileName.length() - 4));
                        }
                    }

                    // 保存到本地数据库中
                    noteDB.save(note);
                }
                break;
        }
    }

    /**
     * 比较两个数据列表，获取指定类型的数据
     *
     * @param dataType
     * @param serverData
     * @param localData
     * @return
     */
    public static List<NoteInfo> getData(int dataType, List<NoteInfo> serverData, List<NoteInfo> localData) {
        ArrayList<NoteInfo> data = new ArrayList<NoteInfo>();
        switch (dataType) {
            case SERVER_DELETE:
                // 本地为已删除 在服务器中存在
                for (NoteInfo note : localData) {
                    if (note.isDeleted()) {
                        if (isNoteExistList(note, serverData)) {
                            data.add(note);
                        }
                    }
                }
                break;
            case SERVER_SYNC:
                // 本地为未同步 未删除
                for (NoteInfo note : localData) {
                    if (!note.isSynced() && !note.isDeleted()) {
                        note.setSynced(true);
                        data.add(note);
                    }
                }
                break;
            case LOCAL_DELETE:
                // 在服务器中为已删除 且在本地中存在
                for (NoteInfo note : serverData) {
                    if (note.isDeleted()) {
                        if (isNoteExistList(note, localData)) {
                            data.add(note);
                        }
                    }
                }

                // 本地为已删除，且在服务器中也不存在
                for (NoteInfo note : localData) {
                    if (note.isDeleted()) {
                        if (!isNoteExistList(note, serverData)) {
                            data.add(note);
                        }
                    }
                }
                break;
            case LOCAL_SYNC:
                // 服务器里存在 在本地不存在
                for (NoteInfo note : serverData) {
                    if (note.isSynced() && !note.isDeleted()) {
                        if (!isNoteExistList(note, localData)) {
                            data.add(note);
                        }
                    }
                }

                // 服务器与本地都存在 比较日期
                for (NoteInfo note : serverData) {
                    if (!note.isDeleted() && isNoteExistList(note, localData)) {
                        NoteInfo localNote = getNoteFromList(note.getId(), localData);
                        if (localNote != null && isNewDate(note, localNote)) {
                            data.add(note);
                        }
                    }
                }
                break;
        }
        return data;
    }

    /**
     * 判断一个笔记是否存在于一个列表中
     *
     * @param noteInfo
     * @param noteInfoList
     * @return
     */
    private static boolean isNoteExistList(NoteInfo noteInfo, List<NoteInfo> noteInfoList) {
        for (NoteInfo n : noteInfoList) {
            if (noteInfo.getId() == n.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 根据ID从列表获取笔记
     *
     * @param id
     * @param noteInfoList
     * @return
     */
    private static NoteInfo getNoteFromList(long id, List<NoteInfo> noteInfoList) {
        for (NoteInfo n : noteInfoList) {
            if (id == n.getId()) {
                return n;
            }
        }

        return null;
    }

    /**
     * 比较笔记日期
     *
     * @param lhs
     * @param rhs
     * @return if true, lhs > rhs. if false, lhs < rhs.
     */
    private static boolean isNewDate(NoteInfo lhs, NoteInfo rhs) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddHH:mm");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = format.parse(lhs.getDate() + lhs.getTime());
            date2 = format.parse(rhs.getDate() + rhs.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date1.after(date2)) {
            return true;
        }

        return false;
    }

    /**
     * 把一个笔记从数据库中删除
     *
     * @param note
     * @param noteDB
     */
    public static void deleteNoteFromDB(NoteInfo note, NoteDB noteDB) {
        // 解析图片相关的信息 删除资源文件

        String curResPath;
        if (PreferencesUtils.isLogin()) {
            curResPath = StringUtils.resPath + PreferencesUtils.getUser() + "/";
        } else {
            curResPath = StringUtils.resPath + "unlogin/";
        }

        Document doc = Jsoup.parse(note.getContent());
        Elements newElements = doc.select("img");
        for (Element element : newElements) {
            String fileName = element.attr("src");
            FileUtils.deleteFile(new File(curResPath + fileName));
            if (fileName.contains(".mp4")) {
                FileUtils.deleteFile(new File(curResPath + fileName.substring(0, fileName.length() - 4)));
            }
        }

        // 从本地数据库中删除
        noteDB.delete(note.getId());
    }
}

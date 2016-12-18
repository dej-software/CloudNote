package com.jikexueyuan.cloudnote.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dej on 2016/12/2.
 */

public class NoteInfo implements Parcelable {

    private long id = 0;
    private String title;
    private String date;
    private String time;
    private String content;
    private boolean synced = false;
    private boolean deleted = false;

    public NoteInfo() {
    }

    public NoteInfo(long id, String title, String date, String time, String content, boolean synced, boolean deleted) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.content = content;
        this.synced = synced;
        this.deleted = deleted;
    }

    protected NoteInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        date = in.readString();
        time = in.readString();
        content = in.readString();
        synced = in.readByte() != 0;
        deleted = in.readByte() != 0;
    }

    public static final Creator<NoteInfo> CREATOR = new Creator<NoteInfo>() {
        @Override
        public NoteInfo createFromParcel(Parcel in) {
            return new NoteInfo(in);
        }

        @Override
        public NoteInfo[] newArray(int size) {
            return new NoteInfo[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(content);
        dest.writeByte((byte) (synced ? 1 : 0));
        dest.writeByte((byte) (deleted ? 1 : 0));
    }
}

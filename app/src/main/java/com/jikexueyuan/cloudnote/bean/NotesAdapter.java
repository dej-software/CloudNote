package com.jikexueyuan.cloudnote.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jikexueyuan.cloudnote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dej on 2016/12/2.
 */
public class NotesAdapter extends BaseAdapter {

    private Context context;
    private List<NoteInfo> noteInfoList;

    public NotesAdapter(Context context) {
        this.context = context;
        this.noteInfoList = new ArrayList<>();
    }

    public NotesAdapter(Context context, List<NoteInfo> noteInfoList) {
        this.context = context;
        this.noteInfoList = noteInfoList;
    }

    public List<NoteInfo> getNoteInfoList() {
        return noteInfoList;
    }

    public void setNoteInfoList(List<NoteInfo> noteInfoList) {
        this.noteInfoList = noteInfoList;
    }

    public void addItem(NoteInfo noteInfo) {
        this.noteInfoList.add(noteInfo);
    }

    @Override
    public int getCount() {
        return noteInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return noteInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.doc_cell, null);

            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tv_description);
            viewHolder.imgSync = (ImageView) convertView.findViewById(R.id.not_sync_img);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 设置笔记信息
        final NoteInfo noteInfo = (NoteInfo) getItem(position);

        if (noteInfo.isSynced()) {
            viewHolder.imgSync.setVisibility(View.GONE);
        } else {
            viewHolder.imgSync.setVisibility(View.VISIBLE);
        }

        viewHolder.tvTitle.setText(noteInfo.getTitle());
        viewHolder.tvDate.setText(noteInfo.getDate() + " " + noteInfo.getTime());

        String noteContent = noteInfo.getContent();
        int index = noteContent.indexOf("<img");

        // 开头为图片
        if (index == 0) {
            viewHolder.tvDescription.setText("");
        } else if (index < 0 || index > 10) {
            if (noteContent.length() > 10) {
                // 字符长度大于10 截取显示
                viewHolder.tvDescription.setText(noteContent.substring(0, 10).trim());
            } else {
                viewHolder.tvDescription.setText(noteContent.trim());
            }
        } else {
            viewHolder.tvDescription.setText(noteContent.substring(0, index).trim());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTitle, tvDate, tvDescription;
        ImageView imgSync;
    }
}

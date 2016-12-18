package com.jikexueyuan.cloudnote.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.bean.NoteInfo;
import com.jikexueyuan.cloudnote.bean.NotesAdapter;
import com.jikexueyuan.cloudnote.db.NoteDB;
import com.jikexueyuan.cloudnote.util.DataUtils;
import com.jikexueyuan.cloudnote.util.FileUtils;
import com.jikexueyuan.cloudnote.util.NetWorkUtils;
import com.jikexueyuan.cloudnote.util.PreferencesUtils;
import com.jikexueyuan.cloudnote.util.ProgressDialogUtils;
import com.jikexueyuan.cloudnote.util.ServerUtils;
import com.jikexueyuan.cloudnote.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by dej on 2016/12/1.
 */

public class DocFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "DocFragment";
    private static final int REQUEST_LOGIN = 2000;

    private ImageView toolbarBtn;

    private ListView lvNotes;
    private NotesAdapter notesAdapter;
    private List<NoteInfo> noteInfoList;

    private NoteDB noteDB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        View rootView = inflater.inflate(R.layout.fragment_doc, container, false);
        View rootView = inflater.inflate(R.layout.fragment_doc, null);

        initView(rootView);

        return rootView;
    }

    private void initView(View rootView) {
        Log.i(TAG, "initView");

        // 给当前Fragment设置Toolbar
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).setTitle("");
        setHasOptionsMenu(true);

        FloatingActionButton fabAddNote = (FloatingActionButton) rootView.findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(this);

        // 头像图标点击监听
        toolbarBtn = (ImageView) rootView.findViewById(R.id.toolbar_btn);
        toolbarBtn.setOnClickListener(this);

        // 对默认登录用户进行判断
        PreferencesUtils.init(getContext());
        if (PreferencesUtils.isLogin()) {
            toolbarBtn.setImageResource(R.drawable.login);
            noteDB = new NoteDB(getActivity(), PreferencesUtils.getUser());
        } else {
            noteDB = new NoteDB(getActivity());
        }

        noteInfoList = DataUtils.getNormalData(noteDB);
        notesAdapter = new NotesAdapter(getActivity(), noteInfoList);
        lvNotes = (ListView) rootView.findViewById(R.id.lv_doc);
        lvNotes.setAdapter(notesAdapter);
        // 设置点击、长按多选的监听
        lvNotes.setOnItemClickListener(this);
        lvNotes.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvNotes.setMultiChoiceModeListener(multiChoiceModeListener);
    }

    // ActionMode 列表多选删除时用到
    private ActionMode localMode = null;
    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        List<NoteInfo> deleteNotes = new ArrayList<NoteInfo>();

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (checked) {
                deleteNotes.add(noteInfoList.get(position));
            } else {
                deleteNotes.remove(noteInfoList.get(position));
            }

            mode.setTitle("选择了" + deleteNotes.size() + "项");
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            localMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.delete_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    System.out.println("删除");
                    new AlertDialog.Builder(getContext())
                            .setTitle("删除选中笔记")
                            .setMessage("确定删除选中的笔记？不可恢复！")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (NoteInfo note : deleteNotes) {
                                        if (PreferencesUtils.isLogin()) {
                                            note.setDeleted(true);
                                            noteDB.save(note);
                                        } else {
                                            DataUtils.deleteNoteFromDB(note, noteDB);
                                        }
                                    }
                                    updateListView();
                                    mode.finish();
                                }
                            }).show();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deleteNotes.clear();
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (localMode != null) {
            localMode.finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    /**
     * 更新ListView显示信息
     */
    private void updateListView() {
        noteDB.destroy();
        noteDB = null;
        if (PreferencesUtils.isLogin()) {
            toolbarBtn.setImageResource(R.drawable.login);
            noteDB = new NoteDB(getActivity(), PreferencesUtils.getUser());
        } else {
            toolbarBtn.setImageResource(R.drawable.unlogin);
            noteDB = new NoteDB(getActivity());
        }

        noteInfoList = DataUtils.getNormalData(noteDB);
        notesAdapter.setNoteInfoList(noteInfoList);
        notesAdapter.notifyDataSetChanged();
        lvNotes.invalidateViews();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        updateListView();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab_add_note:
                Log.i(TAG, "Add Note");
                startActivity(new Intent(getActivity(), NoteEditActivity.class));
                break;
            case R.id.toolbar_btn:
                Log.i(TAG, "点击用户图标");
                if (PreferencesUtils.isLogin()) {
                    startActivity(new Intent(getActivity(), AccountInfoActivity.class));
                } else {
                    startActivityForResult(new Intent(getActivity(), UserLoginActivity.class), REQUEST_LOGIN);
                }
                break;
        }
    }

    /**
     * 点击Item时浏览笔记
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 传入被点击的笔记Item
        Intent i = new Intent(getActivity(), NoteDetailActivity.class);
        Bundle b = new Bundle();

        NoteInfo noteInfo = (NoteInfo) notesAdapter.getItem(position);

        if (noteInfo != null) {
            b.putParcelable("note", noteInfo);
            i.putExtras(b);
            startActivity(i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.doc_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sync:
                Log.i(TAG, "同步");
                if (PreferencesUtils.isLogin()) {
                    new SyncNoteAsyncTask(getContext()).execute((Void) null);
                } else {
                    startActivityForResult(new Intent(getActivity(), UserLoginActivity.class), REQUEST_LOGIN);
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 登录成功 返回
            boolean isLogin = data.getBooleanExtra("isLogin", true);
            if (isLogin) {
                toolbarBtn.setImageResource(R.drawable.login);
                PreferencesUtils.setIsLogin(isLogin);
                PreferencesUtils.commit();
                NoteDB unloginDB = new NoteDB(getContext());
                NoteDB loginDB = new NoteDB(getContext(), PreferencesUtils.getUser());
                List<NoteInfo> unloginNotes = unloginDB.query();
                if (!unloginNotes.isEmpty()) {
                    for (NoteInfo note : unloginNotes) {
                        loginDB.save(note);
                        unloginDB.delete(note.getId());
                    }
                    FileUtils.moveDirAllFile(new File(StringUtils.unloginResPath), StringUtils.resPath + PreferencesUtils.getUser() + "/");
                }
            }
        }
    }

    public class SyncNoteAsyncTask extends AsyncTask<Void, Void, String> {

        private static final String SYNC_SUCCESS = "@success";
        private static final String SYNC_NOT_DATA = "@not_data";
        // 网络不可用
        private static final String NET_WORK_NOT_AVAILABLE = "@net_not_available";
        // 连接异常 超时
        private static final String NET_WORK_CONNECT_E = "@net_connect_e";

        private Context context;

        public SyncNoteAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "同步笔记");

            if (!NetWorkUtils.isNetworkAvailable(context)) {
                Log.i(TAG, "网络不可用");
                return NET_WORK_NOT_AVAILABLE;
            }

            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;


            URL url = null;
            try {
                url = new URL(ServerUtils.noteDbAddress);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write("action=" + ServerUtils.ACTION_GET_DATA + "&table=" + PreferencesUtils.getUser() + "_notes");
                bw.flush();

                is = connection.getInputStream();
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);

                String line;
                StringBuilder strBuilder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    strBuilder.append(line);
                }

                br.close();
                isr.close();
                is.close();

                System.out.println(strBuilder.toString());

                return strBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
                return NET_WORK_CONNECT_E;
            } catch (SocketTimeoutException e) {
                return NET_WORK_CONNECT_E;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
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

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialogUtils.showProgressDialog(context, "", "正在同步");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) {
                return;
            }

            switch (s) {
                case NET_WORK_NOT_AVAILABLE:
                    Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
                    ProgressDialogUtils.hideProgressDialog();
                    return;
                case NET_WORK_CONNECT_E:
                    Toast.makeText(context, "网络状态异常", Toast.LENGTH_SHORT).show();
                    ProgressDialogUtils.hideProgressDialog();
                    return;
            }

            JSONArray array = null;
            List<NoteInfo> serverData = new ArrayList<NoteInfo>();
            List<NoteInfo> localData = DataUtils.getAllData(noteDB);

            try {
                array = new JSONArray(s);
                if (array.length() != 0) {
                    serverData = DataUtils.convertList(array);
                }
            } catch (JSONException e) {
                System.out.println(e);
            }


            if (serverData.isEmpty() && localData.isEmpty()) {
                System.out.println("同步数据为空");
            } else {
                System.out.println("开始同步");

                // 同步在服务器删除的数据
                DataUtils.syncData(DataUtils.SERVER_DELETE,
                        DataUtils.getData(DataUtils.SERVER_DELETE, serverData, localData), noteDB);

                // 同步在服务器要更新保存的数据
                DataUtils.syncData(DataUtils.SERVER_SYNC,
                        DataUtils.getData(DataUtils.SERVER_SYNC, serverData, localData), noteDB);

                // 同步在本地要删除的数据
                DataUtils.syncData(DataUtils.LOCAL_DELETE,
                        DataUtils.getData(DataUtils.LOCAL_DELETE, serverData, localData), noteDB);

                // 同步在本地要更新保存的数据
                DataUtils.syncData(DataUtils.LOCAL_SYNC,
                        DataUtils.getData(DataUtils.LOCAL_SYNC, serverData, localData), noteDB);
            }

            Toast.makeText(getContext(), "同步完成", Toast.LENGTH_SHORT).show();
            ProgressDialogUtils.hideProgressDialog();
            updateListView();
        }
    }
}

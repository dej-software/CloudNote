package com.jikexueyuan.cloudnote.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.bean.NoteInfo;
import com.jikexueyuan.cloudnote.db.NoteDB;
import com.jikexueyuan.cloudnote.util.FileUtils;
import com.jikexueyuan.cloudnote.util.PreferencesUtils;
import com.jikexueyuan.cloudnote.util.StringUtils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by dej on 2016/12/1.
 */

public class SettingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingFragment";
    private static final int REQUEST_LOGIN = 2000;

    private LinearLayout layout;
    private ImageView userImage;
    private TextView userName;
    private Button exitLogin;

    private String[] texts = new String[]{
            "功能1", "功能2", "功能3",
            "功能4", "功能5"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        layout = (LinearLayout) rootView.findViewById(R.id.layout);

        userImage = (ImageView) rootView.findViewById(R.id.user_image);
        userImage.setOnClickListener(this);
        userName = (TextView) rootView.findViewById(R.id.user_name);
        userName.setOnClickListener(this);

        exitLogin = (Button) rootView.findViewById(R.id.exit_login);
        exitLogin.setOnClickListener(this);

        initView();

        return rootView;
    }

    private void initView() {
        for (int i = 0; i < texts.length; i++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.setting_list_cell, layout, false);


            TextView tv = (TextView) view.findViewById(R.id.tv_function);
            tv.setText(texts[i]);

//            if (i == 3 || i == 6 || i == 8) {
//                View spaceView = LayoutInflater.from(getActivity()).inflate(R.layout.me_list_space, layout, false);
//                layout.addView(spaceView);
//            }

            layout.addView(view);
        }
//        layout.addView(LayoutInflater.from(getActivity()).inflate(R.layout.me_list_space, layout, false));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.user_image:
            case R.id.user_name:
                if (PreferencesUtils.isLogin()) {
                    startActivity(new Intent(getActivity(), AccountInfoActivity.class));
                } else {
                    startActivityForResult(new Intent(getActivity(), UserLoginActivity.class), REQUEST_LOGIN);
                }
                break;

            case R.id.exit_login:
                PreferencesUtils.setIsLogin(false);
                PreferencesUtils.commit();
                updateView();
                break;
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        updateView();
    }

    /**
     * 更新界面
     */
    private void updateView() {
        if (PreferencesUtils.isLogin()) {
            userImage.setImageResource(R.drawable.login);
            userName.setText(PreferencesUtils.getUser());
            exitLogin.setVisibility(View.VISIBLE);
        } else {
            userImage.setImageResource(R.drawable.unlogin);
            userName.setText(R.string.click_login);
            exitLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 登录成功 返回
            boolean isLogin = data.getBooleanExtra("isLogin", true);
            if (isLogin) {
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
}

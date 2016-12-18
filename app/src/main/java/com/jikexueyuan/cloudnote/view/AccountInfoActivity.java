package com.jikexueyuan.cloudnote.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.util.PreferencesUtils;

public class AccountInfoActivity extends AppCompatActivity {

    private static final String TAG = "AccountInfoActivity";

    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (TextView) findViewById(R.id.user_name);
        userName.setText(PreferencesUtils.getUser());
    }

    public void exitLoginOnClick(View view) {
        Log.i(TAG, "退出登录");
        PreferencesUtils.setIsLogin(false);
        PreferencesUtils.commit();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

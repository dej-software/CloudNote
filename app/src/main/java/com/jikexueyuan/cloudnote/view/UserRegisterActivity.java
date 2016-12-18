package com.jikexueyuan.cloudnote.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.util.NetWorkUtils;
import com.jikexueyuan.cloudnote.util.ProgressDialogUtils;
import com.jikexueyuan.cloudnote.util.ServerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UserRegisterActivity extends AppCompatActivity {

    private static final String TAG = "UserRegisterActivity";

    private UserRegisterTask mAuthTask = null;

    // UI references.
    private TextView mAccountView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAccountView = (TextView) findViewById(R.id.account);
        mPasswordView = (EditText) findViewById(R.id.password);

        // 设置点击键盘输入完成后的处理
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 检查输入及登录
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mAccountView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String account = mAccountView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查密码
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // 检查用户名
        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_field_required));
            focusView = mAccountView;
            cancel = true;
        } else if (!isAccountValid(account)) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserRegisterTask(this, account, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isAccountValid(String account) {
        //长度为6-18，字母开头，只能由 字母+数字+下划线 组成
        return (account.length() > 5) && (account.length() < 19) && !Character.isDigit(account.charAt(0));
    }

    private boolean isPasswordValid(String password) {
        //密码为6-16位
        return (password.length() > 5) && (password.length() < 17);
    }

    /**
     * 按钮监听
     *
     * @param view
     */
    public void registerOnClick(View view) {
        switch (view.getId()) {
            case R.id.register_now:
                Log.i(TAG, "立即注册");
                attemptRegister();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.i(TAG, "返回");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 注册处理
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private final String mAccount;
        private final String mPassword;

        // "@user_exist"         注册：用户名已存在
        // "@register_success"   注册：注册成功
        // "@register_failed"    注册：注册失败（服务器插入数据时出错）
        private static final String REGISTER_SUCCESS = "@register_success";
        private static final String USER_EXIST = "@user_exist";
        private static final String SERVER_ERR = "@register_failed";
        // 网络不可用
        private static final String NET_WORK_NOT_AVAILABLE = "@net_not_available";
        // 连接异常 超时
        private static final String NET_WORK_CONNECT_E = "@net_connect_e";

        UserRegisterTask(Context context, String account, String password) {
            this.context = context;
            mAccount = account;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!NetWorkUtils.isNetworkAvailable(context)) {
                Log.i(TAG, "网络不可用");
                return NET_WORK_NOT_AVAILABLE;
            }

            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                URL url = new URL(ServerUtils.loginAddress + "?action=register&user=" + mAccount + "&pass=" + mPassword);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                is = connection.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                String line;
                StringBuilder strBuilder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    strBuilder.append(line);
                }

                br.close();
                isr.close();
                is.close();

                return strBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
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

            return "";
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            super.onPreExecute();
            ProgressDialogUtils.showProgressDialog(context, "", "正在注册");
        }

        @Override
        protected void onPostExecute(final String resultStr) {
            Log.i(TAG, "onPostExecute : " + resultStr);
            mAuthTask = null;

            // 隐藏进度框
            ProgressDialogUtils.hideProgressDialog();

            switch (resultStr) {
                case NET_WORK_NOT_AVAILABLE:
                    Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
                    break;
                case NET_WORK_CONNECT_E:
                    Toast.makeText(context, "网络状态异常", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_SUCCESS:
                    Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
                    // 把注册成功的用户返回给登录界面
                    Intent i = new Intent();
                    i.putExtra("user", mAccount);
                    setResult(RESULT_OK, i);
                    finish();
                    break;
                case USER_EXIST:
                    mAccountView.setError(getString(R.string.error_exist_account));
                    mAccountView.requestFocus();
                    break;
                case SERVER_ERR:
                    Toast.makeText(context, "注册失败，请检查您的用户名或密码", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

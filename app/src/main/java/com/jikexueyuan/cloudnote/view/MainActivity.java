package com.jikexueyuan.cloudnote.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.jikexueyuan.cloudnote.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tabDoc, tabSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        tabDoc = (TextView) findViewById(R.id.tab_doc);
        tabSetting = (TextView) findViewById(R.id.tab_setting);

        tabDoc.setSelected(true);

        tabDoc.setOnClickListener(this);
        tabSetting.setOnClickListener(this);

        getSupportFragmentManager().beginTransaction().add(R.id.container, new DocFragment()).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_doc:

                tabSetting.setSelected(false);
                tabDoc.setSelected(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DocFragment()).commit();

                break;
            case R.id.tab_setting:
                tabDoc.setSelected(false);
                tabSetting.setSelected(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingFragment()).commit();
                break;
        }
    }
}

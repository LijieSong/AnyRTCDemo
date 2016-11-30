package com.example.user.anyrtcdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.ImageHeadUtils;
import com.example.user.anyrtcdemo.Utils.NameUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_start_live, btn_look_live;
    private ImageView iv_back, iv_camera;
    private TextView tv_title;
    private RelativeLayout titleBar;
    private String header = null;
    private String nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setOnClick();
    }

    private void setOnClick() {
        btn_start_live.setOnClickListener(this);
        btn_look_live.setOnClickListener(this);
    }

    private void initView() {
        btn_start_live = (Button) findViewById(R.id.btn_start_live);
        btn_look_live = (Button) findViewById(R.id.btn_look_live);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        titleBar = (RelativeLayout) findViewById(R.id.title);
        iv_back.setVisibility(View.GONE);
        iv_camera.setVisibility(View.GONE);
        tv_title.setText(R.string.app_name);
        header = ImageHeadUtils.getVavatar();
        nick = NameUtils.getNickName();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("nickname", nick);
        intent.putExtra("headUrl", header);
        switch (v.getId()) {
            case R.id.btn_start_live://开启直播
                intent.setClass(MainActivity.this, AnyLiveStartActivity.class);
                break;
            case R.id.btn_look_live://观看直播
                intent.setClass(MainActivity.this, AnyLiveWatchActivity.class);
                break;
        }
        MainActivity.this.startActivity(intent);
    }
}

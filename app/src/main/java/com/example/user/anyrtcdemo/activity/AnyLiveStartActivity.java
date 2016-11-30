package com.example.user.anyrtcdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.anyrtcdemo.Utils.ImageHeadUtils;
import com.example.user.anyrtcdemo.Utils.NameUtils;
import com.example.user.anyrtcdemo.application.Constant;
import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.GlideUtils.GlideUtils;
import com.example.user.anyrtcdemo.Utils.PermissionsCheckUtil;
import com.example.user.anyrtcdemo.Utils.RTMPCHttpSDK;
import com.zhy.m.permission.MPermissions;

import org.json.JSONException;

/**
 * 项目名称：anyrtcdemo
 * 类描述：AnyLiveStartActivity 描述: 准备直播界面
 * 创建人：songlijie
 * 创建时间：2016/11/9 15:02
 * 邮箱:814326663@qq.com
 */
public class AnyLiveStartActivity extends BaseActivity implements View.OnClickListener {
    private ImageView iv_center_image;
    private TextView tv_nickname;
    private ImageView iv_back, iv_camera;
    private TextView tv_title;
    private RelativeLayout titleBar;
    private String nick;
    private EditText et_theme;
    private Button btn_start;
    private static final int REQUECT_CODE_RECORD = 0;
    private static final int REQUECT_CODE_CAMERA = 1;
    private String header = null;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_anylivestart);
        getDate();
        initView();
        getDevicePermission();
        iniData();
        setOnClick();
    }

    private void getDate() {
//        header = ImageHeadUtils.getVavatar();
//        nick = NameUtils.getNickName();
        nick = getIntent().getExtras().getString("nickname");
        header = getIntent().getExtras().getString("headUrl");
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        titleBar = (RelativeLayout) findViewById(R.id.title);

        iv_center_image = (ImageView) findViewById(R.id.iv_center_image);
        et_theme = (EditText) findViewById(R.id.et_theme);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        btn_start = (Button) findViewById(R.id.btn_start);
    }

    private void iniData() {
        tv_title.setText(R.string.prompt_live_topic);
        iv_camera.setVisibility(View.GONE);
        GlideUtils.downLoadCircleImage(this,header , iv_center_image);
        tv_nickname.setText(nick);
    }

    private void setOnClick() {
        btn_start.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    /**
     * 获取摄像头和录音权限
     */
    private void getDevicePermission() {
        PermissionsCheckUtil.isOpenCarmaPermission(new PermissionsCheckUtil.RequestPermissionListener() {
            @Override
            public void requestPermissionSuccess() {

            }

            @Override
            public void requestPermissionFailed() {
                PermissionsCheckUtil.showMissingPermissionDialog(AnyLiveStartActivity.this, getString(R.string.str_no_camera_permission));
            }

            @Override
            public void requestPermissionThanSDK23() {
                if (ContextCompat.checkSelfPermission(AnyLiveStartActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    MPermissions.requestPermissions(AnyLiveStartActivity.this, REQUECT_CODE_CAMERA, Manifest.permission.CAMERA);
                }
            }
        });


        PermissionsCheckUtil.isOpenRecordAudioPermission(new PermissionsCheckUtil.RequestPermissionListener() {
            @Override
            public void requestPermissionSuccess() {

            }

            @Override
            public void requestPermissionFailed() {
                PermissionsCheckUtil.showMissingPermissionDialog(AnyLiveStartActivity.this, getString(R.string.str_no_audio_record_permission));
            }

            @Override
            public void requestPermissionThanSDK23() {
                if (ContextCompat.checkSelfPermission(AnyLiveStartActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    MPermissions.requestPermissions(AnyLiveStartActivity.this, REQUECT_CODE_RECORD, Manifest.permission.RECORD_AUDIO);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                back(v);
                break;
            case R.id.btn_start:
                String trim = et_theme.getText().toString().trim();
                if (TextUtils.isEmpty(trim) || trim.length() == 0) {
                    Toast.makeText(this, getString(R.string.live_theme_not_null), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    String anyrtcId = RTMPCHttpSDK.getRandomString(12);
                    String hostID = anyrtcId;
                    String rtmpPushUrl = String.format(Constant.RTMP_PUSH_URL, anyrtcId);
                    String rtmpPullUrl = String.format(Constant.RTMP_PULL_URL, anyrtcId);
                    String hlsUrl = String.format(Constant.HLS_URL, anyrtcId);
                    org.json.JSONObject item = new org.json.JSONObject();
                    try {
                        item.put("hosterId", hostID);
                        item.put("rtmp_url", rtmpPullUrl);
                        item.put("hls_url", hlsUrl);
                        item.put("topic", trim);
                        item.put("nickname", nick);
                        item.put("headUrl", header);
                        item.put("anyrtcId", anyrtcId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("hosterId", hostID);
                    bundle.putString("rtmp_url", rtmpPushUrl);
                    bundle.putString("hls_url", hlsUrl);
                    bundle.putString("topic", trim);
                    bundle.putString("headUrl", header);
                    bundle.putString("nickname", nick);
                    bundle.putString("andyrtcId", anyrtcId);
                    bundle.putString("userData", item.toString());
                    Intent intent = new Intent(AnyLiveStartActivity.this, AnyHosterActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    break;
                }
        }
    }
}

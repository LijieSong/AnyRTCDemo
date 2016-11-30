package com.example.user.anyrtcdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.user.anyrtcdemo.bean.LiveItemBean;
import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.ImageHeadUtils;
import com.example.user.anyrtcdemo.Utils.NameUtils;
import com.example.user.anyrtcdemo.Utils.PermissionsCheckUtil;
import com.example.user.anyrtcdemo.Utils.RTMPCHttpSDK;
import com.example.user.anyrtcdemo.Utils.RecyclerViewUtil;
import com.example.user.anyrtcdemo.adapter.LiveHosterAdapter;
import com.example.user.anyrtcdemo.application.Constant;
import com.zhy.m.permission.MPermissions;

import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;

/**
 * 项目名称：anyrtcdemo
 * 类描述：AnyLiveWatchActivity 描述: 直播列表界面
 * 创建人：songlijie
 * 创建时间：2016/11/10 11:05
 * 邮箱:814326663@qq.com
 */
public class AnyLiveWatchActivity extends BaseActivity implements RecyclerViewUtil.RefreshDataListener, RecyclerViewUtil.ScrollingListener, BGAOnItemChildClickListener,View.OnClickListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LiveHosterAdapter mAdapter;
    private RecyclerViewUtil mRecyclerViewUtils;
    private List<LiveItemBean> listLive;
    private ImageView iv_back,iv_camera;
    private TextView tv_title;
    private RelativeLayout titleBar;
    private static final int REQUECT_CODE_RECORD = 0;
    private static final int REQUECT_CODE_CAMERA = 1;
    private String nick,headerUrl = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anylivewatch);
        getDate();
        initView();
        getDevicePermission();
        iniData();
        setOnClick();
    }
    private void getDate() {
        nick = NameUtils.getNickName();
        headerUrl = ImageHeadUtils.getVavatar();
    }
    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        titleBar = (RelativeLayout) findViewById(R.id.title);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe_refresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }
    private void setOnClick() {
        iv_back.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        mAdapter.setOnItemChildClickListener(this);
    }
    private void iniData() {
        tv_title.setText(R.string.watch_live_info);
        iv_camera.setVisibility(View.GONE);
        listLive = new ArrayList<LiveItemBean>();
            /**
             * 获取直播列表
             */
            RTMPCHttpSDK.GetLiveList(AnyLiveWatchActivity.this, RTMPCHybird.Inst().GetHttpAddr(), Constant.DEVELOPERID, Constant.APPID,
                    Constant.APPTOKEN, mRTMPCHttpCallback);
        mAdapter = new LiveHosterAdapter(AnyLiveWatchActivity.this,mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerViewUtils = new RecyclerViewUtil();
        mRecyclerViewUtils.init(AnyLiveWatchActivity.this, mSwipeRefreshLayout, this.mRecyclerView, mAdapter, this);
        mRecyclerViewUtils.beginRefreshing();//第一次自动加载一次
        mRecyclerViewUtils.setScrollingListener(this);
        mRecyclerViewUtils.setPullUpRefreshEnable(false);
    }
    @Override
    public void onItemChildClick(ViewGroup viewGroup, View view, int i) {
        Intent it = new Intent(AnyLiveWatchActivity.this, AnyGuestActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hls_url", listLive.get(i).getmHlsUrl());
        bundle.putString("rtmp_url", listLive.get(i).getmRtmpPullUrl());
        bundle.putString("anyrtcId", listLive.get(i).getmAnyrtcId());
        bundle.putString("userData", new JSONObject().toString());
        bundle.putString("headUrl",headerUrl);
        bundle.putString("nickname",nick);
        bundle.putString("topic", listLive.get(i).getmLiveTopic());
        it.putExtras(bundle);
        startActivity(it);
    }

    @Override
    public void onRefresh() {
        /**
         * 下拉刷新直播列表
         */
        RTMPCHttpSDK.GetLiveList(AnyLiveWatchActivity.this, RTMPCHybird.Inst().GetHttpAddr(), Constant.DEVELOPERID, Constant.APPID,
                Constant.APPTOKEN, mRTMPCHttpCallback);
    }

    @Override
    public boolean loadMore() {
        return false;
    }

    @Override
    public void scroll(boolean scrollState) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.iv_back:
                back(v);
                break;
            case R.id.iv_camera:
                break;
        }
    }


    private RTMPCHttpSDK.RTMPCHttpCallback mRTMPCHttpCallback = new RTMPCHttpSDK.RTMPCHttpCallback() {
        @Override
        public void OnRTMPCHttpOK(String strContent) {
            mRecyclerViewUtils.endRefreshing();
            try {
                listLive.clear();
                JSONObject liveJson = new JSONObject(strContent);
                JSONArray liveList = liveJson.getJSONArray("LiveList");
                JSONArray memberList = liveJson.getJSONArray("LiveMembers");
                for (int i = 0; i < liveList.length(); i++) {
                    LiveItemBean bean = new LiveItemBean();
                    JSONObject itemJson = new JSONObject(liveList.getString(i));
                    bean.setmHosterId(itemJson.getString("hosterId"));
                    bean.setmRtmpPullUrl(itemJson.getString("rtmp_url"));
                    bean.setmHlsUrl(itemJson.getString("hls_url"));
                    bean.setmLiveTopic(itemJson.getString("topic"));
                    bean.setmAnyrtcId(itemJson.getString("anyrtcId"));
                    bean.setmMemNumber(memberList.getInt(i));
                    listLive.add(bean);
                }
                mAdapter.setDatas(listLive);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void OnRTMPCHttpFailed(int code) {
            if (listLive == null){
                mRecyclerView.setBackgroundResource(R.drawable.fx_default_useravatar);
            }
        }
    };
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
                PermissionsCheckUtil.showMissingPermissionDialog(AnyLiveWatchActivity.this, getString(R.string.str_no_camera_permission));
            }

            @Override
            public void requestPermissionThanSDK23() {
                if (ContextCompat.checkSelfPermission(AnyLiveWatchActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    MPermissions.requestPermissions(AnyLiveWatchActivity.this, REQUECT_CODE_CAMERA, Manifest.permission.CAMERA);
                }
            }
        });


        PermissionsCheckUtil.isOpenRecordAudioPermission(new PermissionsCheckUtil.RequestPermissionListener() {
            @Override
            public void requestPermissionSuccess() {

            }

            @Override
            public void requestPermissionFailed() {
                PermissionsCheckUtil.showMissingPermissionDialog(AnyLiveWatchActivity.this, getString(R.string.str_no_audio_record_permission));
            }

            @Override
            public void requestPermissionThanSDK23() {
                if (ContextCompat.checkSelfPermission(AnyLiveWatchActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    MPermissions.requestPermissions(AnyLiveWatchActivity.this, REQUECT_CODE_RECORD, Manifest.permission.RECORD_AUDIO);
                }
            }
        });
    }
}

package com.example.user.anyrtcdemo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.user.anyrtcdemo.adapter.GradViewAdapter;
import com.example.user.anyrtcdemo.bean.ChatMessageBean;
import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.SoftKeyboardUtil;
import com.example.user.anyrtcdemo.Utils.ThreadUtil;
import com.example.user.anyrtcdemo.adapter.LiveChatAdapter;
import com.example.user.anyrtcdemo.weight.ScrollRecycerView;
import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;
import org.anyrtc.rtmpc_hybird.RTMPCAbstractGuest;
import org.anyrtc.rtmpc_hybird.RTMPCGuestKit;
import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.anyrtc.rtmpc_hybird.RTMPCVideoView;
import org.anyrtc.utils.RTMPAudioManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 项目名称：anyrtcdemo
 * 类描述：AnyGuestActivity 描述: 游客观看主播界面
 * 创建人：songlijie
 * 创建时间：2016/11/10 11:51
 * 邮箱:814326663@qq.com
 */
public class AnyGuestActivity extends BaseActivity implements View.OnClickListener,ScrollRecycerView.ScrollPosation {

    private String TAG = AnyGuestActivity.class.getSimpleName();
    private static final int CLOSED = 0;
    private RTMPCGuestKit mGuestKit;
    private RTMPCVideoView mVideoView;
    private boolean mStartLine = false;
    private String mNickname;
    private String mRtmpPullUrl;
    private String mAnyrtcId;
    private String mHlsUrl;
    private String mGuestId;
    private JSONObject mUserData;
    private String mTopic;
    private String headerUrl;
    private SoftKeyboardUtil softKeyboardUtil;
    private int duration = 100;//软键盘延迟打开时间
    private boolean isKeybord = false;
    private CheckBox mCheckBarrage;
    private DanmakuView mDanmakuView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;
    private List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;
    private int maxMessageList = 150; //列表中最大 消息数目
    private RTMPAudioManager mRtmpAudioManager = null;
    private ImageView btnChat;
    private ImageView iv_back, iv_camera;
    private TextView tv_title;
    private RelativeLayout titleBar;
    private GridView grid_button;
    private PopupWindow up;
    private String values = "1";
    private int clickTimes = 1;
    private GradViewAdapter saImageItems;
    private RelativeLayout rl_rtmpc_videos;
    private TextView txt_watcher_number;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLOSED: {
                    mGuestKit.HangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    mStartLine = false;
                    finish();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anyguest);
        getDate();
        initView();
        iniData();
        setOnClick();
    }

    private void getDate() {
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mNickname = getIntent().getExtras().getString("nickname");
        headerUrl = getIntent().getExtras().getString("headUrl");
        mRtmpPullUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("anyrtcId");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mGuestId = mNickname;//getIntent().getExtras().getString("guestId");
        mTopic = getIntent().getExtras().getString("topic");
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        titleBar = (RelativeLayout) findViewById(R.id.title);

        mCheckBarrage = (CheckBox) findViewById(R.id.check_barrage);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        editMessage = (EditText) findViewById(R.id.edit_message);
        vaBottomBar = (ViewAnimator) findViewById(R.id.va_bottom_bar);
        llInputSoft = (LinearLayout) findViewById(R.id.ll_input_soft);
        flChatList = (FrameLayout) findViewById(R.id.fl_chat_list);
        btnChat = (ImageView) findViewById(R.id.iv_host_text);
        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);

        rl_rtmpc_videos = (RelativeLayout) findViewById(R.id.rl_rtmpc_videos);

        txt_watcher_number = (TextView) findViewById(R.id.txt_watcher_number);

    }

    private void iniData() {
        tv_title.setText(mTopic);
        iv_camera.setBackgroundResource(R.drawable.link_mic_numal);
        mChatLiveAdapter = new LiveChatAdapter(mChatMessageList, this);
        rcLiveChat.setLayoutManager(new LinearLayoutManager(this));
        rcLiveChat.setAdapter(mChatLiveAdapter);
        rcLiveChat.addScrollPosation(this);
        setEditTouchListener();
        vaBottomBar.setAnimateFirstView(true);
        //设置流
        setStream();
    }

    private void setStream() {
        //设置横屏模式，也可sdk初始化时进行设置
        //RTMPCHybird.Inst().SetScreenToLandscape();
        mVideoView = new RTMPCVideoView(rl_rtmpc_videos, RTMPCHybird.Inst().Egl(), false);

        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);

        {
            // Create and audio manager that will take care of audio routing,
            // audio modes, audio device enumeration etc.
            mRtmpAudioManager = RTMPAudioManager.create(this, new Runnable() {
                // This method will be called each time the audio state (number
                // and
                // type of devices) has been changed.
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            // Store existing audio settings and change audio mode to
            // MODE_IN_COMMUNICATION for best possible VoIP performance.
            mRtmpAudioManager.init();
        }

        mUserData = new JSONObject();
        try {
            mUserData.put("nickName", mNickname);
            mUserData.put("headUrl",headerUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * 初始化rtmp播放器
         */
        mGuestKit = new RTMPCGuestKit(this, mGuestListener);
        VideoRenderer render = mVideoView.OnRtcOpenLocalRender(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        /**
         * 开始播放rtmp流
         */
        mGuestKit.StartRtmpPlay(mRtmpPullUrl, render.GetRenderPointer());
        /**
         * 开启RTC连线连接
         */
        mGuestKit.JoinRTCLine(mAnyrtcId, mGuestId, mUserData.toString());
    }

    private void setOnClick() {
        iv_camera.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDanmakuView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mStartLine) {
                ShowExitDialog();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
        softKeyboardUtil.removeGlobalOnLayoutListener(this);
        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        /**
         * 销毁rtmp播放器
         */
        if (mGuestKit != null) {
            mGuestKit.Clear();
            mVideoView.OnRtcRemoveLocalRender();
            mGuestKit = null;
        }
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(AnyGuestActivity.this);
        View view = View.inflate(AnyGuestActivity.this, R.layout.layout_dialog_rtc, null);
        TextView tv_delete_title = (TextView) view.findViewById(R.id.tv_delete_title);
        TextView tv_rtc_nick = (TextView) view.findViewById(R.id.tv_rtc_nick);
        ImageView iv_trc_avatar = (ImageView) view.findViewById(R.id.iv_trc_avatar);
        iv_trc_avatar.setVisibility(View.GONE);
        tv_delete_title.setText(getString(R.string.str_exit));
        tv_rtc_nick.setText(getString(R.string.str_line_hangup));
        build.setView(view);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mGuestKit.HangupRTCLine();
                mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                mStartLine = false;
                finish();
            }
        });
        build.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        build.show();
    }

    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_send_message) {
            String message = editMessage.getText().toString();
            editMessage.setText("");
            if (message.equals("")) {
                return;
            }
            if (mCheckBarrage.isChecked()) {
                mGuestKit.SendBarrage(mNickname, headerUrl, message);
                IDanmakuItem item = new DanmakuItem(AnyGuestActivity.this, new SpannableString(mNickname+":"+message), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mGuestKit.SendUserMsg(mNickname, headerUrl, message);
                addChatMessageList(new ChatMessageBean(mNickname, mNickname,headerUrl, message));//TODO 此处弹幕开关未开启时,消息在消息列表显示
            }
//            addChatMessageList(new ChatMessageBean(mNickname, mNickname,headerUrl, message));//TODO 此处是把发送的消息都添加到消息列表中 不论是弹幕还是消息
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(AnyGuestActivity.this, editMessage);
        }  else if (btn.getId() == R.id.iv_host_luw){//TODO 此处为送礼物
            clickTimes = 1;
            values="1";
            final View view = View.inflate(AnyGuestActivity.this, R.layout.layout_live_gride, null);
            RelativeLayout rl_liwu  = (RelativeLayout) view.findViewById(R.id.rl_liwu);
            grid_button = (GridView) view.findViewById(R.id.grid_button);
            final RadioGroup et_values = (RadioGroup) view.findViewById(R.id.rg_button);
            Button btn_send = (Button) view.findViewById(R.id.btn_send);
            Button btn_lianji = (Button) view.findViewById(R.id.btn_lianji);
            et_values.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    //获取变更后的选中项的ID
                    int radioButtonId = group.getCheckedRadioButtonId();
                    //根据ID获取RadioButton的实例
                    RadioButton rb = (RadioButton)view.findViewById(radioButtonId);
                    values = rb.getText().toString();
                    clickTimes = 1;
                }
            });
            //生成动态数组，并且转入数据
            final List<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
            for(int i=0;i<10;i++)
            {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemImage", R.drawable.ic_launcher);//添加图像资源的ID
                map.put("ItemText", "礼物"+String.valueOf(i));//按序号做ItemText
                lstImageItem.add(map);
            }
            saImageItems = new GradViewAdapter(this,lstImageItem);
            //添加并且显示
            grid_button.setAdapter(saImageItems);
            saImageItems.setSeclection(0);
            saImageItems.notifyDataSetChanged();
            up = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            up.setOutsideTouchable(true);
            up.setHeight(700);
            up.showAtLocation(btnChat, Gravity.BOTTOM,0,0);
            up.update();
            grid_button.setOnItemClickListener(new AdapterView.OnItemClickListener() {//上面礼物的选择控制
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    clickTimes=1;
                    saImageItems.setSeclection(position);
                    saImageItems.notifyDataSetChanged();
                }
            });
            btn_send.setOnClickListener(new View.OnClickListener() {//送礼
                @Override
                public void onClick(View v) {
                    //TODO 礼物支付的逻辑及请求要在这里面做  mAnyrtcId 所观看主播的ID  后台处理收益消费问题  传游客ID跟 主播ID及礼物个数/名字 后台计算扣费
                    clickTimes=1;
                    HashMap<String, Object> hashMap = lstImageItem.get(saImageItems.getSeclection());
                    String text = (String) hashMap.get("ItemText");
                    mGuestKit.SendBarrage(mNickname, headerUrl, "送了"+values+"个"+text+"给主播"+mAnyrtcId);
                    IDanmakuItem item = new DanmakuItem(AnyGuestActivity.this, new SpannableString(mNickname+":"+"送了"+values+"个"+text+"给主播"+mAnyrtcId), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
            btn_lianji.setOnClickListener(new View.OnClickListener() {//连击 必须选择10个以上的
                @Override
                public void onClick(View v) {
                    if (values.equals("1")){
                        Toast.makeText(AnyGuestActivity.this, "一组最少为10个", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //TODO 礼物支付的逻辑及请求要在这里面做 mAnyrtcId 所观看主播的ID 后台处理收益消费问题  传游客ID跟 主播ID及礼物个数/名字 后台计算扣费
                    HashMap<String, Object> hashMap = lstImageItem.get(saImageItems.getSeclection());
                    String text = (String) hashMap.get("ItemText");
                    mGuestKit.SendBarrage(mNickname, headerUrl, "送了"+ clickTimes+"组"+"个"+text+"给主播"+mAnyrtcId);
                    IDanmakuItem item = new DanmakuItem(AnyGuestActivity.this, new SpannableString(mNickname+":"+"送了"+ clickTimes+"组"+"个"+text+"给主播"+mAnyrtcId), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                    clickTimes++;
                }
            });
        }
    }

    /**
     * 设置 键盘的监听事件
     */
    private void setEditTouchListener() {
        softKeyboardUtil = new SoftKeyboardUtil();

        softKeyboardUtil.observeSoftKeyboard(AnyGuestActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow) {
                if (isShow) {
                    ThreadUtil.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isKeybord) {
                                isKeybord = true;
                                llInputSoft.animate().translationYBy(-editMessage.getHeight() / 3).setDuration(100).start();
                                flChatList.animate().translationYBy(-editMessage.getHeight() / 3).setDuration(100).start();
                            }

                        }
                    }, duration);
                } else {
                    btnChat.requestFocus();
                    vaBottomBar.setDisplayedChild(0);
                    llInputSoft.animate().translationYBy(editMessage.getHeight() / 3).setDuration(100).start();
                    flChatList.animate().translationYBy(editMessage.getHeight() / 3).setDuration(100).start();
                    isKeybord = false;
                }
            }
        });
    }

    /**
     * 更新列表
     *
     * @param chatMessageBean
     */
    private void addChatMessageList(ChatMessageBean chatMessageBean) {
        // 150 条 修改；

        if (mChatMessageList == null) {
            return;
        }

        if (mChatMessageList.size() < maxMessageList) {
            mChatMessageList.add(chatMessageBean);
        } else {
            mChatMessageList.remove(0);
            mChatMessageList.add(chatMessageBean);
        }
        mChatLiveAdapter.notifyDataSetChanged();
        rcLiveChat.smoothScrollToPosition(mChatMessageList.size() - 1);
    }


    @Override
    public void ScrollButtom() {
    }

    @Override
    public void ScrollNotButtom() {

    }

    /**
     * 连线时小图标的关闭按钮连接
     */
    private RTMPCVideoView.BtnVideoCloseEvent mBtnVideoCloseEvent = new RTMPCVideoView.BtnVideoCloseEvent() {

        @Override
        public void CloseVideoRender(View view, String strPeerId) {
            /**
             * 挂断连线
             */
            mGuestKit.HangupRTCLine();
            mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
            mStartLine = false;
            iv_camera.setBackgroundResource(R.drawable.link_mic_numal);
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 连线时切换游客摄像头
             */
            mGuestKit.SwitchCamera();
        }
    };

    /**
     * 观看直播回调信息接口
     */
    private RTMPCAbstractGuest mGuestListener = new RTMPCAbstractGuest() {

        /**
         * rtmp 连接成功
         */
        @Override
        public void OnRtmplayerOKCallback() {
        }

        /**
         * rtmp 当前播放状态
         * @param cacheTime 当前缓存时间
         * @param curBitrate 当前播放器码流
         */
        @Override
        public void OnRtmplayerStatusCallback(int cacheTime, int curBitrate) {

        }

        /**
         * rtmp 播放缓冲区时长
         * @param time 缓冲时间
         */
        @Override
        public void OnRtmplayerCacheCallback(int time) {

        }

        /**
         * rtmp 播放器关闭
         * @param errcode
         */
        @Override
        public void OnRtmplayerClosedCallback(int errcode) {

        }

        /**
         * 游客RTC 状态回调
         * @param code 回调响应码：0：正常；101：主播未开启直播；
         * @param strReason 原因描述
         */
        @Override
        public void OnRTCJoinLineResultCallback(final int code, String strReason) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {

                    } else if (code == 101) {
                        Toast.makeText(AnyGuestActivity.this, R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                        mHandler.sendEmptyMessageDelayed(CLOSED, 2000);
                    }
                }
            });
        }

        /**
         * 游客申请连线回调
         * @param code 0：申请连线成功；-1：主播拒绝连线
         */
        @Override
        public void OnRTCApplyLineResultCallback(final int code) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("LocalCameraRender", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                        mGuestKit.SetVideoCapturer(render.GetRenderPointer(), true);
                    } else if (code == -1) {
                        Toast.makeText(AnyGuestActivity.this, R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        mStartLine = false;
                        iv_camera.setBackgroundResource(R.drawable.link_mic_numal);
                    }
                }
            });
        }

        /**
         * 当与主播连线成功时，其他用户连线回调
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCOtherLineOpenCallback(String strLivePeerID, String strCustomID, String strUserData) {

        }

        /**
         * 其他用户连线回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOtherLineCloseCallback(String strLivePeerID) {

        }

        /**
         * 挂断连线回调
         */
        @Override
        public void OnRTCHangupLineCallback() {
            //主播连线断开
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestKit.HangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    mStartLine = false;
                    iv_camera.setBackgroundResource(R.drawable.link_mic_numal);
                }
            });
        }

        /**
         * 主播已离开回调
         * @param code
         * @param strReason
         */
        @Override
        public void OnRTCLineLeaveCallback(int code, String strReason) {
            //主播关闭直播
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AnyGuestActivity.this, R.string.str_hoster_leave, Toast.LENGTH_LONG).show();
                    mHandler.sendEmptyMessageDelayed(CLOSED, 2000);
                }
            });
        }

        /**
         * 连线接通后回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOpenVideoRenderCallback(final String strLivePeerID) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerID, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    mGuestKit.SetRTCVideoRender(strLivePeerID, render.GetRenderPointer());
                }
            });
        }

        /**
         * 连线关闭后图像回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestKit.SetRTCVideoRender(strLivePeerID, 0);
                    mVideoView.OnRtcRemoveRemoteRender(strLivePeerID);
                }
            });
        }

        /**
         * 消息回调
         * @param strCustomID 消息的发送者id
         * @param strCustomName 消息的发送者昵称
         * @param strCustomHeader 消息的发送者头像url
         * @param strMessage 消息内容
         */
        @Override
        public void OnRTCUserMessageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strMessage) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, strCustomHeader, strMessage));
                }
            });
        }

        /**
         * 弹幕回调
         * @param strCustomID 弹幕的发送者id
         * @param strCustomName 弹幕的发送者昵称
         * @param strCustomHeader 弹幕的发送者头像url
         * @param strBarrage 弹幕的内容
         */
        @Override
        public void OnRTCUserBarrageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strBarrage) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName,strCustomHeader, strBarrage));//TODO 把获取到的弹幕消息添加到消息列表
                    IDanmakuItem item = new DanmakuItem(AnyGuestActivity.this, new SpannableString(strCustomName+":"+strBarrage), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
        }

        /**
         * 观看直播的总人数回调
         * @param totalMembers 观看直播的总人数
         */
        @Override
        public void OnRTCMemberListWillUpdateCallback(final int totalMembers) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt_watcher_number.setText(String.format(getString(R.string.str_live_watcher_number), totalMembers));
                }
            });
        }

        /**
         * 人员上下线回调
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCMemberCallback(final String strCustomID, final String strUserData) {
            AnyGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject userData = new JSONObject(strUserData);
                        addChatMessageList(new ChatMessageBean(userData.getString("nickName"), "", userData.getString("headUrl"), ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * 直播观看总人数回调结束
         */
        @Override
        public void OnRTCMemberListUpdateDoneCallback() {

        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                if (mStartLine) {
                    ShowExitDialog();
                }else{
                    back(v);
                }
                break;
            case R.id.iv_camera:
                if (!mStartLine) {

                    JSONObject json = new JSONObject();
                    try {//申请连麦的信息
                        json.put("guestId", mNickname);
                        json.put("headUrl", headerUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /**
                     * 向主播申请连线
                     */
                    mGuestKit.ApplyRTCLine(json.toString());
                    mStartLine = true;
                    iv_camera.setBackgroundResource(R.drawable.link_mic_cancel);
                } else {
                    /**
                     * 挂断连线
                     */
                    mGuestKit.HangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    mStartLine = false;
                    iv_camera.setBackgroundResource(R.drawable.link_mic_numal);
                }
                break;
        }
    }
}

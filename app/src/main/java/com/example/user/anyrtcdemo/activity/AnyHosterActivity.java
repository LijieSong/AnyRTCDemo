package com.example.user.anyrtcdemo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.user.anyrtcdemo.Utils.InputTools;
import com.example.user.anyrtcdemo.adapter.GradViewAdapter;
import com.example.user.anyrtcdemo.bean.ChatMessageBean;
import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.AnyRTCUtils;
import com.example.user.anyrtcdemo.Utils.GlideUtils.GlideUtils;
import com.example.user.anyrtcdemo.Utils.RTMPCHttpSDK;
import com.example.user.anyrtcdemo.Utils.SoftKeyboardUtil;
import com.example.user.anyrtcdemo.Utils.ThreadUtil;
import com.example.user.anyrtcdemo.adapter.LiveChatAdapter;
import com.example.user.anyrtcdemo.application.Constant;
import com.example.user.anyrtcdemo.weight.ScrollRecycerView;
import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;

import org.anyrtc.rtmpc_hybird.RTMPCAbstractHoster;
import org.anyrtc.rtmpc_hybird.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.anyrtc.rtmpc_hybird.RTMPCVideoView;
import org.anyrtc.utils.RTMPAudioManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 项目名称：AnyRTCTest
 * 类描述：AnyHosterActivity 描述: 直播界面
 * 创建人：songlijie
 * 创建时间：2016/11/9 12:33
 * 邮箱:814326663@qq.com
 */
public class AnyHosterActivity extends BaseActivity implements View.OnClickListener, ScrollRecycerView.ScrollPosation {
    private String TAG = AnyHosterActivity.class.getSimpleName();
    private String mNickname;
    private String mRtmpPushUrl;
    private String mAnyrtcId;
    private String mHlsUrl;
    private String mGuestId;
    private String mUserData;
    private String mTopic;
    private String mHosterId;
    private String mVodSvrId;
    private String mVodResTag;
    private String header;
    private RTMPCHosterKit mHosterKit;
    private RTMPCVideoView mVideoView;
    private boolean mStartRtmp = false;
    private SoftKeyboardUtil softKeyboardUtil;
    private int duration = 100;//软键盘延迟打开时间
    private CheckBox mCheckBarrage;
    private DanmakuView mDanmakuView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;
    private ImageView btnChat;
    private ImageView iv_back, iv_camera;
    private TextView tv_title;
    private RelativeLayout titleBar;
    private List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;
    private int maxMessageList = 150; //列表中最大 消息数目
    private RelativeLayout rl_rtmpc_videos;
    private GridView grid_button;
    private PopupWindow up;
    private String values = "1";
    private int clickTimes = 1;
    private GradViewAdapter saImageItems;
    private RTMPAudioManager mRtmpAudioManager = null;
    private  TextView txt_watcher_number;
    private  AlertDialog.Builder build;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anyhoster);
        getDate();
        initView();
        iniData();
        setOnClick();
    }

    private void iniData() {
        iv_camera.setImageResource(R.drawable.em_camera_switch_selector);
        tv_title.setText(mTopic);
        titleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
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
        //设置横屏模式 当主播端设置后， 观众端也必须设置为横屏模式，也可在sdk初始化时进行设置
        //RTMPCHybird.Inst().SetScreenToLandscape();
        mVideoView = new RTMPCVideoView(rl_rtmpc_videos, RTMPCHybird.Inst().Egl(), true);
        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
        mHosterKit = new RTMPCHosterKit(this, mHosterListener);
        {
            VideoRenderer render = mVideoView.OnRtcOpenLocalRender(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            mHosterKit.SetVideoCapturer(render.GetRenderPointer(), true);
        }
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
        mStartRtmp = true;
        /**
         * 设置自适应码流
         */
        mHosterKit.SetNetAdjustMode(RTMPCHosterKit.RTMPNetAdjustMode.RTMP_NA_Fast);
        /**
         * 开始推流
         */
        mHosterKit.StartPushRtmpStream(mRtmpPushUrl);
        /**
         * 建立RTC连线连接
         */
        mHosterKit.OpenRTCLine(mAnyrtcId, mHosterId, mUserData);
    }

    private void getDate() {
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mNickname = getIntent().getExtras().getString("nickname");
        mHosterId = getIntent().getExtras().getString("hosterId");
        mRtmpPushUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("andyrtcId");
        mUserData = getIntent().getExtras().getString("userData");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mTopic = getIntent().getExtras().getString("topic");
        header = getIntent().getExtras().getString("headUrl");//主播头像地址
    }

    private void setOnClick() {
        iv_back.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        titleBar = (RelativeLayout) findViewById(R.id.title);

        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        mCheckBarrage = (CheckBox) findViewById(R.id.check_barrage);
        editMessage = (EditText) findViewById(R.id.edit_message);
        vaBottomBar = (ViewAnimator) findViewById(R.id.va_bottom_bar);
        llInputSoft = (LinearLayout) findViewById(R.id.ll_input_soft);
        flChatList = (FrameLayout) findViewById(R.id.fl_chat_list);
        btnChat = (ImageView) findViewById(R.id.iv_host_text);
        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);

        rl_rtmpc_videos = (RelativeLayout) findViewById(R.id.rl_rtmpc_videos);

        txt_watcher_number = (TextView) findViewById(R.id.txt_watcher_number);

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
            ShowExitDialog();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
        softKeyboardUtil.removeGlobalOnLayoutListener(this);

        if (mVodSvrId != null && mVodSvrId.length() > 0 && mVodResTag.length() > 0) {
            //关闭录像
            RTMPCHttpSDK.CloseRecRtmpStream(getApplicationContext(), RTMPCHybird.Inst().GetHttpAddr(), Constant.DEVELOPERID, Constant.APPID,
                    Constant.APPTOKEN, mVodSvrId, mVodResTag);
        }

        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        if (mHosterKit != null) {
            mVideoView.OnRtcRemoveLocalRender();
            mHosterKit.Clear();
            mHosterKit = null;
        }
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_send_message) {
            String message = editMessage.getText().toString();
            editMessage.setText("");
            if (TextUtils.isEmpty(message)) {
                return;
            }
            if (mCheckBarrage.isChecked()) {
                mHosterKit.SendBarrage(mNickname, header, message);
                IDanmakuItem item = new DanmakuItem(AnyHosterActivity.this, new SpannableString(mNickname + ":" + message), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mHosterKit.SendUserMsg(mNickname, header, message);
                addChatMessageList(new ChatMessageBean(mNickname, mNickname, header, message));//TODO 此处弹幕开关未开启时,消息在消息列表显示
            }
//         addChatMessageList(new ChatMessageBean(mNickname, mNickname,header, message));//TODO 此处是把发送的消息都添加到消息列表中 不论是弹幕还是消息
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(AnyHosterActivity.this, editMessage);
        } else if (btn.getId() == R.id.iv_host_luw) {//TODO 此处为送礼物
            clickTimes = 1;
            values = "1";
            final View view = View.inflate(AnyHosterActivity.this, R.layout.layout_live_gride, null);
            RelativeLayout rl_liwu = (RelativeLayout) view.findViewById(R.id.rl_liwu);
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
                    RadioButton rb = (RadioButton) view.findViewById(radioButtonId);
                    values = rb.getText().toString();
                    clickTimes = 1;
                }
            });
            //生成动态数组，并且转入数据
            final List<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < 10; i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemImage", R.drawable.ic_launcher);//添加图像资源的ID
                map.put("ItemText", "礼物" + String.valueOf(i));//按序号做ItemText
                lstImageItem.add(map);
            }
            saImageItems = new GradViewAdapter(this, lstImageItem);
            //添加并且显示
            grid_button.setAdapter(saImageItems);
            saImageItems.setSeclection(0);
            saImageItems.notifyDataSetChanged();
            up = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            up.setOutsideTouchable(true);
            up.setHeight(700);
            up.showAtLocation(btnChat, Gravity.BOTTOM, 0, 0);
            up.update();
            grid_button.setOnItemClickListener(new AdapterView.OnItemClickListener() {//上面礼物的选择控制
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    clickTimes = 1;
                    saImageItems.setSeclection(position);
                    saImageItems.notifyDataSetChanged();
                }
            });
            btn_send.setOnClickListener(new View.OnClickListener() {//送礼
                @Override
                public void onClick(View v) {
                    clickTimes = 1;
                    //TODO 礼物支付的逻辑及请求要在这里面做
                    HashMap<String, Object> hashMap = lstImageItem.get(saImageItems.getSeclection());
                    String text = (String) hashMap.get("ItemText");
                    mHosterKit.SendBarrage(mNickname, header, "送了" + values + "个" + text);
                    IDanmakuItem item = new DanmakuItem(AnyHosterActivity.this, new SpannableString(mNickname + ":" + "送了" + values + "个" + text), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
            btn_lianji.setOnClickListener(new View.OnClickListener() {//连击 必须选择10个以上的
                @Override
                public void onClick(View v) {
                    if (values.equals("1")){
                        Toast.makeText(AnyHosterActivity.this, "一组最少为10个", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //TODO 礼物支付的逻辑及请求要在这里面做
                    HashMap<String, Object> hashMap = lstImageItem.get(saImageItems.getSeclection());
                    String text = (String) hashMap.get("ItemText");
                    mHosterKit.SendBarrage(mNickname, header, "送了" + clickTimes + "组" + values + text);
                    IDanmakuItem item = new DanmakuItem(AnyHosterActivity.this, new SpannableString(mNickname + ":" + "送了" + clickTimes + "组" + values + text), mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                    clickTimes++;
                }
            });
        }
    }


    /**
     * 连线时小图标的关闭连接按钮及切换摄像头按钮
     */
    private RTMPCVideoView.BtnVideoCloseEvent mBtnVideoCloseEvent = new RTMPCVideoView.BtnVideoCloseEvent() {

        @Override
        public void CloseVideoRender(View view, String strPeerId) {
            /**
             * 挂断连线
             */
            mHosterKit.HangupRTCLine(strPeerId);
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 切换摄像头
             */
            mHosterKit.SwitchCamera();
        }
    };

    /**
     * 更细列表
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

    /**
     * 设置 键盘的监听事件
     */
    private void setEditTouchListener() {
        softKeyboardUtil = new SoftKeyboardUtil();

        softKeyboardUtil.observeSoftKeyboard(AnyHosterActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow) {
                if (isShow) {
                    ThreadUtil.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            llInputSoft.animate().translationYBy(-editMessage.getHeight() / 2).setDuration(100).start();
                            flChatList.animate().translationYBy(-editMessage.getHeight() / 2).setDuration(100).start();
                        }
                    }, duration);
                } else {
                    btnChat.requestFocus();
                    vaBottomBar.setDisplayedChild(0);
                    llInputSoft.animate().translationYBy(editMessage.getHeight() / 2).setDuration(100).start();
                    flChatList.animate().translationYBy(editMessage.getHeight() / 2).setDuration(100).start();
                }
            }
        });
    }

    /**
     * 连线弹窗
     *
     * @param context
     * @param strLivePeerID
     * @param strCustomID
     */
    private void ShowDialog(Context context, final String strLivePeerID, final String strCustomID, final String strUserData) {
        build = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.layout_dialog_rtc, null);
        TextView tv_delete_title = (TextView) view.findViewById(R.id.tv_delete_title);
        TextView tv_rtc_nick = (TextView) view.findViewById(R.id.tv_rtc_nick);
        ImageView iv_trc_avatar = (ImageView) view.findViewById(R.id.iv_trc_avatar);
        tv_delete_title.setText(getString(R.string.str_connect_hoster));
        tv_rtc_nick.setText(String.format(getString(R.string.str_apply_connect_line), strCustomID));
        Log.d(TAG,"strUserData:"+strUserData);
        try {
            JSONObject userJson = new JSONObject(strUserData);
            GlideUtils.downLoadCircleImage(context,userJson.getString("headUrl"),iv_trc_avatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        build.setView(view);
        build.setPositiveButton(getString(R.string.str_agree), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播接受连线请求
                 */
                mHosterKit.AcceptRTCLine(strLivePeerID);
            }
        });
        build.setNegativeButton(getString(R.string.str_refused), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播拒绝连线请求
                 */
                mHosterKit.RejectRTCLine(strLivePeerID, true);
            }
        });
        build.setCancelable(false);
        build.show();
    }

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(AnyHosterActivity.this);
        View view = View.inflate(AnyHosterActivity.this, R.layout.layout_dialog_rtc, null);
        TextView tv_delete_title = (TextView) view.findViewById(R.id.tv_delete_title);
        TextView tv_rtc_nick = (TextView) view.findViewById(R.id.tv_rtc_nick);
        ImageView iv_trc_avatar = (ImageView) view.findViewById(R.id.iv_trc_avatar);
        iv_trc_avatar.setVisibility(View.GONE);
        tv_delete_title.setText(getString(R.string.str_exit));
        tv_rtc_nick.setText(R.string.str_live_stop);
        build.setView(view);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // TODO Auto-generated method stub
                mStartRtmp = false;
                mHosterKit.StopRtmpStream();
                finish();
            }
        });
        build.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        build.show();
    }


    @Override
    public void ScrollButtom() {

    }

    @Override
    public void ScrollNotButtom() {

    }

    /**
     * 主播回调信息接口
     */
    private RTMPCAbstractHoster mHosterListener = new RTMPCAbstractHoster() {
        /**
         * rtmp连接成功
         */
        @Override
        public void OnRtmpStreamOKCallback() {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //开始录像
                    RTMPCHttpSDK.RecordRtmpStream(AnyHosterActivity.this, RTMPCHybird.Inst().GetHttpAddr(), Constant.DEVELOPERID, Constant.APPID,
                            Constant.APPTOKEN, mAnyrtcId, mRtmpPushUrl, mAnyrtcId, new RTMPCHttpSDK.RTMPCHttpCallback() {
                                @Override
                                public void OnRTMPCHttpOK(String strContent) {
                                    try {
                                        JSONObject recJson = new JSONObject(strContent);
                                        mVodSvrId = recJson.getString("VodSvrId");
                                        mVodResTag = recJson.getString("VodResTag");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void OnRTMPCHttpFailed(int code) {

                                }
                            });
                }
            });
        }

        /**
         * rtmp 重连次数
         * @param times 重连次数
         */
        @Override
        public void OnRtmpStreamReconnectingCallback(final int times) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * rtmp 推流状态
         * @param delayMs 推流延时
         * @param netBand 推流码流
         */
        @Override
        public void OnRtmpStreamStatusCallback(final int delayMs, final int netBand) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * rtmp推流失败回调
         * @param code
         */
        @Override
        public void OnRtmpStreamFailedCallback(int code) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * rtmp 推流关闭回调
         */
        @Override
        public void OnRtmpStreamClosedCallback() {
            finish();
        }

        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         * @param strErr 原因
         */
        @Override
        public void OnRTCOpenLineResultCallback(final int code, String strErr) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * 游客有申请连线回调
         *
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCApplyToLineCallback(final String strLivePeerID, final String strCustomID, final String strUserData) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowDialog(AnyHosterActivity.this, strLivePeerID, strCustomID, strUserData);
                }
            });
        }

        /**
         * 视频连线超过4人时回调
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCLineFullCallback(final String strLivePeerID, String strCustomID, String strUserData) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AnyHosterActivity.this, getString(R.string.str_connect_full), Toast.LENGTH_LONG).show();
                    mHosterKit.RejectRTCLine(strLivePeerID, true);
                }
            });
        }

        /**
         * 游客挂断连线回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCancelLineCallback(String strLivePeerID) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AnyHosterActivity.this, getString(R.string.str_line_disconnect), Toast.LENGTH_LONG).show();
                }
            });
        }

        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         * @param strReason
         */
        @Override
        public void OnRTCLineClosedCallback(final int code, String strReason) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 207) {
                        Toast.makeText(AnyHosterActivity.this, getString(R.string.str_apply_anyrtc_account), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }

        /**
         * 连线接通时的视频图像回调；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOpenVideoRenderCallback(final String strLivePeerID) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerID, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    if (null != render) {
                        mHosterKit.SetRTCVideoRender(strLivePeerID, render.GetRenderPointer());
                    }
                }
            });
        }

        /**
         * 连线关闭时的视频图像回调；
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHosterKit.SetRTCVideoRender(strLivePeerID, 0);
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
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
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
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, strCustomHeader, strBarrage)); //TODO 把获取到的弹幕消息添加到消息列表
                    IDanmakuItem item = new DanmakuItem(AnyHosterActivity.this,new SpannableString(strCustomName + ":" + strBarrage) , mDanmakuView.getWidth(), 0, R.color.colorAccent, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
        }

        /**
         * 直播观看总人数回调
         * @param totalMembers 观看总人数
         */
        @Override
        public void OnRTCMemberListWillUpdateCallback(final int totalMembers) {
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
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
            AnyHosterActivity.this.runOnUiThread(new Runnable() {
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
        switch (v.getId()) {
            case R.id.iv_back:
                ShowExitDialog();
                break;
            case R.id.iv_camera:
                mHosterKit.SwitchCamera();
                break;
        }
    }
}

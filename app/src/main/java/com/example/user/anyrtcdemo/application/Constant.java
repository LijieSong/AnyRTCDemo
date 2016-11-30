package com.example.user.anyrtcdemo.application;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.user.anyrtcdemo.Utils.ImageHeadUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ustc on 2016/6/27.
 */
public class Constant {
    //AnyRtc的相关 //TODO 此处需要 把这些替换成你自己在anyrtc官网申请的账号信息
    public static final String DEVELOPERID = "teameetingtest";
    public static final String  APPID ="meetingtest";
    public static final String APPKEY = "OPJXF3xnMqW+7MMTA4tRsZd6L41gnvrPcI25h9JCA4M";
    public static final String  APPTOKEN= "c4cd1ab6c34ada58e622e75e41b46d6d";

    //直播相关网址
    public final static String gHttpLiveListUrl = "http://%s/anyapi/V1/livelist?AppID=%s&DeveloperID=%s";
    public final static String gHttpRecordUrl = "http://%s/anyapi/V1/recordrtmp?AppID=%s&DeveloperID=%s&AnyrtcID=%s&Url=%s&ResID=%s";
    public final static String gHttpCloseRecUrl = "http://%s/anyapi/V1/closerecrtmp?AppID=%s&DeveloperID=%s&VodSvrID=%s&VodResTag=%s";
    /**
     * rtmp 推流地址
     */
//    rtmp://live.hkstv.hk.lxdns.com:1935/live/
    public static final String RTMP_PUSH_URL = "rtmp://live.hkstv.hk.lxdns.com:1935/live/%s";
    /**
     * rtmp 拉流地址
     */
    public static final String RTMP_PULL_URL = "rtmp://live.hkstv.hk.lxdns.com:1935/live/%s";
    /**
     * hls 地址
     */
    public static final String HLS_URL = "http://192.169.7.207/live/%s.m3u8";
    /**
     * 分享页面url地址
     */
    public static final String SHARE_WEB_URL = "http://123.59.68.21/rtmpc-demo/?%s";
    /**
     * 测试头像的图片地址
     */
    public static final String URL_AVATAR= ImageHeadUtils.getVavatar();

}

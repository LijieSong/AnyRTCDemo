package com.example.user.anyrtcdemo.application;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.user.anyrtcdemo.Utils.SharePrefUtil;

import org.anyrtc.rtmpc_hybird.RTMPCHybird;

import java.io.File;

/**
 * 项目名称：AnyRTCTest
 * 类描述：MyApplication 描述:
 * 创建人：songlijie
 * 创建时间：2016/11/9 11:42
 * 邮箱:814326663@qq.com
 */
public class MyApplication extends Application {
    private static MyApplication application;
    public static Context appContext;
    private static Handler handler;
    private static int mainThreadId;

    public static MyApplication getApp() {
        if (application != null && application instanceof MyApplication) {
            return (MyApplication) application;
        } else {
            application = new MyApplication();
            application.onCreate();
            return (MyApplication) application;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        application = this;

        //1. 获取Context
        appContext = getApplicationContext();

        //2. 创建handler
        handler = new Handler(Looper.getMainLooper());

        //3. 获取主线程id
        mainThreadId = android.os.Process.myTid();
        SharePrefUtil.init(appContext);
        /**
         * 初始化RTMPC引擎
         */
        RTMPCHybird.Inst().Init(appContext);
        RTMPCHybird.Inst().InitEngineWithAnyrtcInfo(Constant.DEVELOPERID, Constant.APPID,  Constant.APPKEY,  Constant.APPTOKEN);
//        RTMPCHybird.Inst().ConfigServerForPriCloud("192.168.7.207", 9060);
    }
    @Override
    public File getCacheDir() {
        Log.i("getCacheDir", "cache sdcard state: " + Environment.getExternalStorageState());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cacheDir = getExternalCacheDir();
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
                Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());
                return cacheDir;
            }
        }

        File cacheDir = super.getCacheDir();
        Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());

        return cacheDir;
    }

    public static MyApplication getInstance() {
        return application;
    }

    public static Context getContext() {
        return appContext;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static int getMainThreadId() {
        return mainThreadId;
    }
}

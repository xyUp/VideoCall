package com.xy.android.videocall;

import android.app.ActivityManager;
import android.app.Application;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

import java.util.Iterator;
import java.util.List;

/**
 * Created by xy on 2017/12/28.
 */

public class BaseApplication extends Application {
    private CallReceiver callReceiver;
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化环信sdk
        initHyphenate();
    }

    private void initHyphenate() {
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果APP启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的APP会在以包名为默认的process name下运行，如果查到的process name不是APP的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase(this.getPackageName())) {
            L.e("enter the service process!");
            // 则此application::onCreate 是被service 调用的，直接返回
            return;
        }

        // 初始化sdk的一些配置
        EMOptions options = new EMOptions();
        options.setAutoLogin(true);

        // 动态设置appkey，如果清单配置文件设置了 appkey，这里可以不用设置
        // options.setAppKey("yunshangzhijia#yunyue");

        // 设置小米推送 appID 和 appKey
        // options.setMipushConfig("2882303761517595619", "5791759550619");
        // 设置消息是否按照服务器时间排序
        options.setSortMessageByServerTime(false);
        // 设置自动登录
        options.setAutoLogin(true);

        // 初始化环信SDK,一定要先调用init()
        EMClient.getInstance().init(this, options);

        // 开启 debug 模式
        EMClient.getInstance().setDebugMode(false);

        // 设置通话广播监听器
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        //注册通话广播接收者
        registerReceiver(callReceiver, callFilter);

        // 通话管理类的初始化
        CallManager.getInstance().init(this);
    }

    private String getAppName(int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}

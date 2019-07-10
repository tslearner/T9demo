package com.ts.t9demo;

import android.app.Application;
import android.content.Context;

import com.ts.t9demo.utils.GetLocalContactUtils;
import com.ts.t9demo.utils.PinyinUtils;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/9 19:15
 * Description:
 */
public class MyApplication extends Application {
    private static Application mApp;
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        PinyinUtils.init(this);
        GetLocalContactUtils.getInstance().loadLocalContact(this);
    }
    synchronized public static Application getApplication() {
        return mApp;
    }

    synchronized public static Context getAppContext() {
        return mApp.getApplicationContext();
    }

}

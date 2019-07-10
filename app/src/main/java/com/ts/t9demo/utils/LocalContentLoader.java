package com.ts.t9demo.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import com.ts.t9demo.MyApplication;


/**
 * 监听本地通讯录
 */
public abstract class LocalContentLoader {
    private static final String TAG = "LocalContentLoader";

    protected final int MESSAGE_WHAT = 1;

    protected Message mMessage;
    private long DELAY_TIME = 500;// 数据库变动最大间隔时间

    private ContentObserver mContactsObserver = null;
    private Context mContext;



    public LocalContentLoader(){
        mContext = MyApplication.getAppContext();
        registerObserver();

    }

    public void registerObserver(){
        try{
            if(mContactsObserver == null) {
                mContactsObserver = new ContentObserver(new Handler()) {

                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        mMessage = mHandler.obtainMessage(MESSAGE_WHAT);
                        mHandler.sendMessageDelayed(mMessage, DELAY_TIME);
                    }
                };

                //8.0以上无权限崩溃
                mContext.getContentResolver().registerContentObserver(
                        ContactsContract.Contacts.CONTENT_URI, true, mContactsObserver);
            }else{
                //8.0以下 首次安装无权限不崩溃，给权限后需要重新注册
                mContext.getContentResolver().unregisterContentObserver(mContactsObserver);
                mContext.getContentResolver().registerContentObserver(
                        ContactsContract.Contacts.CONTENT_URI, true, mContactsObserver);

            }

        }catch (Exception e){
            Log.d(TAG, "registerObserver: "+e.getMessage());
            mContactsObserver = null;
            e.printStackTrace();
        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_WHAT:
                    callBackContentChange();
                    break;
            }
        }
    };



    protected abstract void callBackContentChange();


}

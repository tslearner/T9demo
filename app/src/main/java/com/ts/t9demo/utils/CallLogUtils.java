package com.ts.t9demo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.ts.t9demo.model.CallLogInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/10 8:58
 * Description:获取本地通话记录 并加权重
 */
public class CallLogUtils {
    public static List<CallLogInfo> getCallLog(Context context) {
        List<CallLogInfo> infos = new ArrayList<CallLogInfo>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[] { CallLog.Calls.NUMBER, CallLog.Calls.DATE,
                CallLog.Calls.TYPE };
        Cursor cursor = cr.query(uri, projection, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String number = cursor.getString(0);
                long date = cursor.getLong(1);
                int type = cursor.getInt(2);
                infos.add(new CallLogInfo(number, date, type));
            }
        }catch (Exception e){
            cursor=null;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }

        //添加权重
        addCallLogWeight(infos);
        return infos;
    }

    private static HashMap<String, Integer> sWeightHashMap = new HashMap<>();

    public static HashMap<String, Integer> getWeightHashMap() {
        return sWeightHashMap;
    }

    public static void addCallLogWeight(List<CallLogInfo> infos){
        sWeightHashMap.clear();
        for(CallLogInfo callLogInfo:infos){
            Integer weight = sWeightHashMap.get(callLogInfo.number);
            if (weight == null) {
                weight = 0;
            }
            if(callLogInfo.type == CallLog.Calls.INCOMING_TYPE
                    || callLogInfo.type == CallLog.Calls.MISSED_TYPE){//来电
                weight += 10;

            }else if(callLogInfo.type == CallLog.Calls.OUTGOING_TYPE){//去电
                weight += 15;
            }
            sWeightHashMap.put(callLogInfo.number, weight);
        }
    }
}

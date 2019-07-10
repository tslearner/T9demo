package com.ts.t9demo.model;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/10 8:57
 * Description:通话记录实体类
 */
public class CallLogInfo {
    public String number;
    public long date;
    public int type;
    public CallLogInfo(String number, long date, int type) {
        super();
        this.number = number;
        this.date = date;
        this.type = type;
    }
    public CallLogInfo() {
        super();
    }
}


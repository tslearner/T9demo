package com.ts.t9demo.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.ts.t9demo.MyApplication;
import com.ts.t9demo.model.PinYin;
import com.ts.t9demo.model.SimpleContact;

import java.util.ArrayList;


/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/9 19:13
 * Description:获取本地通讯录列表数据
 */
public class GetLocalContactUtils extends LocalContentLoader {
    private static final String TAG = "GetLocalContactUtils";

    private volatile static GetLocalContactUtils getLocalContactUtils = null;

    private ArrayList<SimpleContact> mList = new ArrayList<>();


    private GetLocalContactUtils() {
        super();
    }

    public static GetLocalContactUtils getInstance() {
        if (getLocalContactUtils == null) {
            synchronized (GetLocalContactUtils.class) {
                getLocalContactUtils = new GetLocalContactUtils();
            }
        }
        return getLocalContactUtils;
    }

    public ArrayList<SimpleContact> getmList(){
        return mList;
    }

    public void loadLocalContact(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "loadLocalContact: ");
                ArrayList<SimpleContact> list= getLocalContact(context);

                Log.d(TAG, "loadLocalContact: " + list.size());


                synchronized (mList) {
                    mList.clear();
                    mList.addAll(list);
                }


            }
        }).start();

    }


    private ArrayList getLocalContact(Context context) {
        ArrayList<SimpleContact> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,}, null, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " DESC");
            if (cursor != null) {

                //moveToNext方法返回的是一个boolean类型的数据
                while (cursor.moveToNext()) {
                    //读取通讯录的号码
                    String number = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (number == null) {
                        number = "";
                    }

                    //读取通讯录的姓名
                    String name = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (name == null) {
                        name = "";
                    }
                    boolean isExisted = false;
                    for (SimpleContact item : list) {
                        if (item.getNumber().equals(number)) {
                            isExisted = true;
                            break;
                        }
                    }
                    if (!isExisted) {
                        SimpleContact simpleContact = new SimpleContact();

                        simpleContact.setName(name);
                        simpleContact.setNumber(number);
                        PinYin pinYin = PinYin.buildPinYin(simpleContact.getName());
                        simpleContact.setPinyin(pinYin);
                        list.add(simpleContact);
                    }
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    //本地通讯录变动回调
    @Override
    protected void callBackContentChange() {
        Log.d(TAG, "callBackContentChange: ");
        loadLocalContact(MyApplication.getAppContext());
    }
}

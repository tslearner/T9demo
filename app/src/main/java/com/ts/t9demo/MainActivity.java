package com.ts.t9demo;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.ts.t9demo.model.SimpleContact;
import com.ts.t9demo.utils.GetLocalContactUtils;
import com.ts.t9demo.utils.T9SearchUtils;

import java.util.ArrayList;

import static android.Manifest.*;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    public EditText mEt;
    public RecyclerView mRecyclerView;
    private ContactAdapter adapter;
    private String mPreSearchString;

    ArrayList<SimpleContact> mSearchContactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        mEt = (EditText)findViewById(R.id.et);
        mEt.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        mRecyclerView = (RecyclerView)findViewById(R.id.rv) ;

        adapter = new ContactAdapter(mSearchContactList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshAdapter();

            }
        });

    }
    private void refreshAdapter(){
        mSearchContactList = (ArrayList<SimpleContact>) T9SearchUtils.searchT9StringInContacts(mEt.getText().toString(), mPreSearchString, mSearchContactList, GetLocalContactUtils.getInstance().getmList());
        adapter.setData(mSearchContactList,mEt.getText().toString());
        mPreSearchString = mEt.getText().toString();
    }

    public void requestPermission() {
        //申请读取本地联系人和通话记录权限
        if (ContextCompat.checkSelfPermission(this,
                permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission.READ_CONTACTS,permission.READ_CALL_LOG
                                }, 1);
            }
        }else if (ContextCompat.checkSelfPermission(this,
                permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission.READ_CONTACTS
                    }, 1);
        }else if (ContextCompat.checkSelfPermission(this,
                permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission.READ_CALL_LOG
                    }, 1);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                    GetLocalContactUtils.getInstance().loadLocalContact(this);
                } else {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

}

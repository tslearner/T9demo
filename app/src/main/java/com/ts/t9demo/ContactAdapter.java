package com.ts.t9demo;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ts.t9demo.model.SimpleContact;

import java.util.List;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/9 18:31
 * Description:
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{
    private List<SimpleContact> mList;
    private String mKeyword;

    public ContactAdapter(List<SimpleContact> list){
        mList = list;
    }
    public void setData(List<SimpleContact> list,String keyword){
        mList.clear();
        mList.addAll(list);
        mKeyword = keyword;
        notifyDataSetChanged();
    }
    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        ContactViewHolder viewHolder = new ContactViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position) {
        if(mList == null){
            return;
        }
        SimpleContact simpleContact = mList.get(position);
        String name = simpleContact.getName();
        String phone = simpleContact.getNumber();
        holder.tvName.setText(name);
        holder.tvNumber.setText(phone);
        if (!TextUtils.isEmpty(mKeyword)) {
            if (simpleContact.getSearchType() == SimpleContact.SEARCH_TYPE_PINYIN) {
                if (!TextUtils.isEmpty(name)) {
                    List<Integer> hightLights = simpleContact.getWeightLight();
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < name.length(); i++) {
                        boolean isAdd = false;
                        for (Integer hightLight : hightLights) {
                            if (i == hightLight) {
                                tempStr.append("<font color=#fccf54>" + name.substring(i, i + 1) + "</font>");
                                isAdd = true;
                            }
                        }
                        if (!isAdd) {
                            tempStr.append(name.substring(i, i + 1));
                        }
                    }
                    Spanned temp = Html.fromHtml(tempStr.toString());
                    holder.tvName.setText(temp);
                } else {
                    holder.tvName.setText(name);
                }
                holder.tvNumber.setText(phone);
            } else if (simpleContact.getSearchType() == SimpleContact.SEARCH_TYPE_NUMBER) {
                if (!TextUtils.isEmpty(phone)) {
                    int index = phone.indexOf(mKeyword);
                    if (index != -1) {
                        Spanned temp = Html.fromHtml(phone.substring(0, index) + "<font color=#fccf54>"
                                + phone.substring(index, index + mKeyword.length()) + "</font>"
                                + phone.substring(index + mKeyword.length(), phone.length()));
                        holder.tvName.setText(name);
                        holder.tvNumber.setText(temp);
                    } else {
                        holder.tvName.setText(name);
                        holder.tvNumber.setText(phone);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvNumber;
        public ContactViewHolder(final View itemView){
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.name);
            tvNumber = (TextView)itemView.findViewById(R.id.number);
        }
    }
}

package com.altsoft.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.altsoft.loggalapp.R;
import com.altsoft.model.device.AD_DEVICE_MOBILE_LIST;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class LocalBoxListItemAdapter extends BaseAdapter {

    private ArrayList<AD_DEVICE_MOBILE_LIST> listViewItemList = new ArrayList<>() ;
    public LocalBoxListItemAdapter(){
    }
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public AD_DEVICE_MOBILE_LIST getItem(int position) {
        return listViewItemList.get(position) ;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;
        TextView userNameView = (TextView) convertView.findViewById(R.id.textView3) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        AD_DEVICE_MOBILE_LIST listViewItem = listViewItemList.get(position);
        Glide.with(context)
                .load(listViewItem.LOGO_URL)
                .apply(new RequestOptions().override(100, 100))
                .into(iconImageView)
        ;

        titleTextView.setText(listViewItem.TITLE);
        descTextView.setText(listViewItem.SUB_TITLE);
        userNameView.setText(listViewItem.COMPANY_NAME);

        return convertView;
    }

    public Boolean SetDataBind(List<AD_DEVICE_MOBILE_LIST> list) {
        if (listViewItemList.size() == 0) {
            listViewItemList = (ArrayList) list;
            return false;
        }
        for(int i =0; i< list.size(); i++) {
            listViewItemList.add(list.get(i));
        }
        this.notifyDataSetChanged();
        return true;

    }
}

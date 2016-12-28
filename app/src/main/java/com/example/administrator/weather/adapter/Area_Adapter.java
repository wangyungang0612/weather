package com.example.administrator.weather.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.weather.ChooseAreaFragment;
import com.example.administrator.weather.R;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangyungang on 2016/12/28
 */
public class Area_Adapter extends BaseAdapter {
    List <String> dataList =new ArrayList<String>();
    private LayoutInflater mInflater = null;
    public Area_Adapter(List<String> dataList, Activity context) {
        this.dataList = dataList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
       if(convertView == null){
           convertView = mInflater.inflate(R.layout.listview_item,null);
           viewHolder = new ViewHolder();
           viewHolder.area_text = (TextView) convertView.findViewById(R.id.area_text);
           convertView.setTag(viewHolder);
       }else {
           viewHolder = (ViewHolder) convertView.getTag();
       }
        //颜色区分
        if (position % 2 == 0) {
            convertView.setBackgroundResource(R.color.item_1);
        } else {
            convertView.setBackgroundResource(R.color.item_2);
        }

        viewHolder.area_text.setText(dataList.get(position));
        return convertView;
    }

    class ViewHolder {
      TextView area_text;
    }
}

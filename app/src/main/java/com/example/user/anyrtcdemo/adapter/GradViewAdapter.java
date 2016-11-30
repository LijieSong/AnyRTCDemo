package com.example.user.anyrtcdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.user.anyrtcdemo.R;

import java.util.HashMap;
import java.util.List;

/**
 * 项目名称：QianShanDoctor
 * 类描述：GradViewAdapter gradview适配器
 * 创建人：slj
 * 创建时间：2016-6-28 10:49
 * 修改人：slj
 * 修改时间：2016-6-28 10:49
 * 修改备注：
 * 邮箱:slj@bjlingzhuo.com
 */
public class GradViewAdapter extends BaseAdapter {
    private Context context;
    private List<HashMap<String, Object>> list;
    private int clickTemp = -1;
    public GradViewAdapter(Context context, List<HashMap<String, Object>> list) {
        this.context = context;
        this.list = list;
    }
    //标识选择的Item
    public void setSeclection(int position) {
        clickTemp = position;
    }
    //标识选择的Item
    public int getSeclection() {
        return clickTemp;
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_gride,null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.ItemImage);
            holder.textview = (TextView) convertView.findViewById(R.id.ItemText);
            holder.rl_item_bg = (RelativeLayout) convertView.findViewById(R.id.rl_item_bg);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        HashMap<String, Object> map = list.get(position);
        int image = (int) map.get("ItemImage");
        String text = (String) map.get("ItemText");
        holder.image.setBackgroundResource(image);
        holder.textview.setText(text);
        // 点击改变选中listItem的背景色
        if (clickTemp == position) {
            holder.rl_item_bg .setBackgroundResource(R.color.accent_blue);
        } else {
            holder.rl_item_bg .setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }
    private class ViewHolder{
        TextView textview;
        ImageView image;
        RelativeLayout rl_item_bg;
    }
}
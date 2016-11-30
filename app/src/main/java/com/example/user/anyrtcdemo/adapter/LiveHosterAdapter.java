package com.example.user.anyrtcdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.example.user.anyrtcdemo.bean.LiveItemBean;
import com.example.user.anyrtcdemo.R;
import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

public class LiveHosterAdapter extends BGARecyclerViewAdapter<LiveItemBean> {
    private Context mContext;

    public LiveHosterAdapter(Context context, RecyclerView recyclerView) {
        super(recyclerView, R.layout.live_item);
        mContext = context;
    }

    @Override
    public void setItemChildListener(BGAViewHolderHelper viewHolderHelper) {
        viewHolderHelper.setItemChildClickListener(R.id.rlayout_item);
    }

    @Override
    protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, LiveItemBean livesBean) {
        bgaViewHolderHelper.setText(R.id.txt_live_name, livesBean.getmLiveTopic());
        bgaViewHolderHelper.setText(R.id.txt_live_number, livesBean.getmMemNumber() + "");
    }

}
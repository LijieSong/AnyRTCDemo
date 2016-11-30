package com.example.user.anyrtcdemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.anyrtcdemo.bean.ChatMessageBean;
import com.example.user.anyrtcdemo.R;
import com.example.user.anyrtcdemo.Utils.GlideUtils.GlideUtils;

import java.util.List;

public class LiveChatAdapter extends RecyclerView.Adapter<LiveChatAdapter.ChatListHolder> {

    String TAG = this.getClass().getSimpleName();
    List<ChatMessageBean> chatMessageList;
    Context context;


    public LiveChatAdapter(List<ChatMessageBean> chatMessageList, Context context) {
        this.chatMessageList = chatMessageList;
        this.context = context;
    }


    @Override
    public ChatListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_live_chat, parent, false);
        ChatListHolder holder = new ChatListHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatListHolder holder, int position) {
        final ChatMessageBean chatMessageBean = chatMessageList.get(position);
        holder.txtChatName.setText(chatMessageBean.getmCustomName());
        GlideUtils.downLoadCircleImage(context,chatMessageBean.getmCustomHeader(),holder.imgHeader);
        if(chatMessageBean.getmMsgContent().equals("")) {
            holder.txtSpace.setVisibility(View.GONE);
            holder.txtChatMessage.setTextColor(R.color.colorAccent);
            holder.txtChatMessage.setText(chatMessageBean.getmCustomID() + context.getString(R.string.str_online));
        } else {
//            holder.imgHeader.setVisibility(View.GONE);
            holder.txtChatMessage.setText(chatMessageBean.getmMsgContent());
        }
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder build = new AlertDialog.Builder(context);
                View view = View.inflate(context, R.layout.layout_dialog_userinfo, null);
                TextView tv_delete_title = (TextView) view.findViewById(R.id.tv_delete_title);
                TextView tv_rtc_nick = (TextView) view.findViewById(R.id.tv_rtc_nick);
                ImageView iv_trc_avatar = (ImageView) view.findViewById(R.id.iv_trc_avatar);
                tv_delete_title.setText(chatMessageBean.getmMsgContent());
                tv_rtc_nick.setText(chatMessageBean.getmCustomName());
                GlideUtils.downLoadCircleImage(context,chatMessageBean.getmCustomHeader(),iv_trc_avatar);
                build.setView(view);
                build.setNegativeButton("关闭", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                build.setCancelable(true);
                build.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();

    }

    public static class ChatListHolder extends RecyclerView.ViewHolder {
        ImageView imgHeader;
        TextView txtChatName;
        TextView txtSpace;
        TextView txtChatMessage;
        LinearLayout llItem;

        public ChatListHolder(View itemView) {
            super(itemView);
            imgHeader = (ImageView) itemView.findViewById(R.id.img_chat_header);
            txtChatName = (TextView) itemView.findViewById(R.id.txt_chat_name);
            txtSpace =  (TextView) itemView.findViewById(R.id.txt_space);
            txtChatMessage = (TextView) itemView.findViewById(R.id.txt_chat_message);
            llItem = (LinearLayout) itemView.findViewById(R.id.ll_itemt);
        }
    }

}

package com.example.webapptest;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class shareAdapter extends ArrayAdapter {
    private int resourceId;
    private com.example.webapptest.shareCallback callback;

    public shareAdapter(Context context, int resource, List objects, shareCallback mcallback){
        super(context, resource, objects);
        resourceId=resource;
        this.callback=mcallback;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder holder;
        final ShareItem shareitem=(ShareItem) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder=new ViewHolder();
            holder.filename_text=(TextView) view.findViewById(R.id.shareFileName);
            holder.sharename_text=(TextView) view.findViewById(R.id.shareName);
            holder.code_text=(TextView) view.findViewById(R.id.CodeView);
            holder.delete_button=(ImageButton) view.findViewById(R.id.shareDelete);
            view.setTag(holder);
        } else {
            view=convertView;
            holder=(ViewHolder) view.getTag();
        }
        holder.filename_text.setText(shareitem.getFilename());
        if(shareitem.getIsdelete().equals("yes")){
            holder.sharename_text.setTextColor(Color.RED);
            holder.sharename_text.setText("文件已删除!");
            holder.code_text.setText("");
        }else{
            holder.code_text.setText(shareitem.getCode());
        }

        holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.deleteclick(position);
            }
        });

        return view;
    }

    class ViewHolder{
        TextView filename_text;
        TextView sharename_text;
        TextView code_text;
        ImageButton delete_button;
    }
}

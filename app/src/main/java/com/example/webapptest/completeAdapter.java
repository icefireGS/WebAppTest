package com.example.webapptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class completeAdapter extends ArrayAdapter {
    private int resourceId;
    private com.example.webapptest.Callback callback;

    public completeAdapter(Context context, int resource, List objects, Callback mcallback){
        super(context, resource, objects);
        resourceId=resource;
        this.callback=mcallback;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder holder;
        final completeItem comitem=(completeItem) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder=new ViewHolder();
            holder.file_image=(ImageView) view.findViewById(R.id.completeimage);
            holder.filename_text=(TextView) view.findViewById(R.id.completfileName);
            holder.state_text=(TextView) view.findViewById(R.id.state);
            holder.openfile_button=(ImageButton) view.findViewById(R.id.openfile);
            holder.delete_button=(ImageButton) view.findViewById(R.id.deleteButton);
            view.setTag(holder);
        } else {
            view=convertView;
            holder=(ViewHolder) view.getTag();
        }
        holder.file_image.setImageResource(comitem.getFile_image());
        holder.filename_text.setText(comitem.getFilename());
        holder.state_text.setText(comitem.getTranslatecate());
        if(comitem.getTranslatecate().equals("下载成功!")){
            holder.openfile_button.setImageResource(comitem.getOpenfile_iamge());

            holder.delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.deleteclick(position);
                }
            });

            holder.openfile_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.openfileclick(position);
                }
            });
        } else{
            holder.delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.updeleteclick(position);
                }
            });
        }



        return view;
    }

    class ViewHolder{
        ImageView file_image;
        TextView filename_text;
        TextView state_text;
        ImageButton openfile_button;
        ImageButton delete_button;
    }

}

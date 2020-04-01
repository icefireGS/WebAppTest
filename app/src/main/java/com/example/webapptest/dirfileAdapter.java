package com.example.webapptest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.*;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class dirfileAdapter extends ArrayAdapter {
    private int resourceId;

    public dirfileAdapter(Context context,int resource,List objects){
        super(context, resource, objects);
        resourceId=resource;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder holder;
        dirfile diritem=(dirfile) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder=new ViewHolder();
            holder.dirfileImage=(ImageView) view.findViewById(R.id.file_image);
            holder.dirfileName=(TextView) view.findViewById(R.id.file_name);
            view.setTag(holder);
        } else {
            view=convertView;
            holder=(ViewHolder) view.getTag();
        }
        holder.dirfileImage.setImageResource(diritem.getDirfileImage());
        holder.dirfileName.setText(diritem.getDirfileName());
        return view;
    }

    class ViewHolder{
        ImageView dirfileImage;
        TextView dirfileName;
    }
}

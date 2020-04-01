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

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.util.List;

public class upAdapter extends ArrayAdapter {
    private int resourceId;
    private com.example.webapptest.Callback callback;

    public upAdapter(Context context, int resource, List objects, Callback mcallback){
        super(context, resource, objects);
        resourceId=resource;
        this.callback=mcallback;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View view;
        upAdapter.ViewHolder holder;
        final UploadItem upitem=(UploadItem) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder=new upAdapter.ViewHolder();
            holder.translate_image=(ImageView) view.findViewById(R.id.translateImage);
            holder.filename_text=(TextView) view.findViewById(R.id.fileName);
            holder.progressBar=(NumberProgressBar) view.findViewById(R.id.translateProgress);
            holder.currentsize_text=(TextView) view.findViewById(R.id.currentSize);
            holder.totalsize_text=(TextView) view.findViewById(R.id.totalSize);
            holder.speed_text=(TextView) view.findViewById(R.id.speed);
            holder.control_button=(ImageButton) view.findViewById(R.id.controlButton);
            holder.cancel_button=(ImageButton) view.findViewById(R.id.cancelButton);
            view.setTag(holder);
        } else {
            view=convertView;
            holder=(upAdapter.ViewHolder) view.getTag();
        }
        holder.translate_image.setImageResource(upitem.getFile_image());
        holder.filename_text.setText(upitem.getmEntity().getFileName());
        holder.progressBar.setProgress(upitem.getmEntity().getPercent());
        holder.currentsize_text.setTextColor(Color.BLACK);
        holder.currentsize_text.setText(upitem.getCurrentConvert());
        holder.totalsize_text.setText("/"+upitem.getmEntity().getConvertFileSize());
        holder.speed_text.setText(upitem.getmEntity().getConvertSpeed());
        holder.control_button.setImageResource(upitem.getControl_image());
        holder.cancel_button.setImageResource(upitem.getCancel_image());

        switch(upitem.getControl_image()){
            case R.drawable.start:
            case R.drawable.stop:
            case R.drawable.restart:
                holder.control_button.setBackgroundResource(R.drawable.task_press);
                break;
            default:
                holder.control_button.setBackgroundColor(Color.parseColor("#00000000"));
                break;
        }

        if(upitem.isFail()){
            holder.totalsize_text.setText("");
            holder.speed_text.setText("");
            holder.currentsize_text.setTextColor(Color.RED);
            holder.currentsize_text.setText("任务失败!");
        }

        holder.cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.upcancelclick(position);
            }
        });

        holder.control_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int image=upitem.getControl_image();
                switch (image){
                    case R.drawable.start:
                        callback.upstartclick(position);
                        break;
                    case R.drawable.stop:
                        callback.upstopclick(position);
                        break;
                    case R.drawable.restart:
                        callback.uprestartclick(position);
                        break;
                    default:
                        break;
                }

            }
        });

        return view;
    }

    class ViewHolder{
        ImageView translate_image;
        TextView filename_text;
        NumberProgressBar progressBar;
        TextView currentsize_text;
        TextView totalsize_text;
        TextView speed_text;
        ImageButton control_button;
        ImageButton cancel_button;
    }
}

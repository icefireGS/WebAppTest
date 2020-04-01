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

public class downAdapter extends ArrayAdapter {
    private int resourceId;
    private com.example.webapptest.Callback callback;

    public downAdapter(Context context, int resource, List objects, Callback mcallback){
        super(context, resource, objects);
        resourceId=resource;
        this.callback=mcallback;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder holder;
        final DownloadItem downitem=(DownloadItem) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder=new ViewHolder();
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
            holder=(ViewHolder) view.getTag();
        }
        holder.translate_image.setImageResource(downitem.getFile_image());
        holder.filename_text.setText(downitem.getmEntity().getFileName());
        holder.progressBar.setProgress(downitem.getmEntity().getPercent());
        holder.currentsize_text.setTextColor(Color.BLACK);
        holder.currentsize_text.setText(downitem.getCurrentConvert());
        holder.totalsize_text.setText("/"+downitem.getmEntity().getConvertFileSize());
        holder.speed_text.setText(downitem.getmEntity().getConvertSpeed());
        holder.control_button.setImageResource(downitem.getControl_image());
        holder.cancel_button.setImageResource(downitem.getCancel_image());

        switch(downitem.getControl_image()){
            case R.drawable.start:
            case R.drawable.stop:
            case R.drawable.restart:
                holder.control_button.setBackgroundResource(R.drawable.task_press);
                break;
            default:
                holder.control_button.setBackgroundColor(Color.parseColor("#00000000"));
                break;
        }

        if(downitem.isFail()){
            holder.totalsize_text.setText("");
            holder.speed_text.setText("");
            holder.currentsize_text.setTextColor(Color.RED);
            holder.currentsize_text.setText("任务失败!");
        }

        holder.cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.cancelclick(position);
            }
        });

        holder.control_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int image=downitem.getControl_image();
                switch (image){
                    case R.drawable.start:
                        callback.startclick(position);
                        break;
                    case R.drawable.stop:
                        callback.stopclick(position);
                        break;
                    case R.drawable.restart:
                        callback.restartclick(position);
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

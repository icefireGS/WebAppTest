package com.example.webapptest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;

public class TaskList extends AppCompatActivity {
    private MyConn conn;
    private TranslateService.MyBinder myBinder;
    private ListView taskListview;
    private TextView storepath;
    private Button showdownload;
    private Button showcomplete;
    private Button showupload;
    private ImageButton goback;
    public static TaskList sInstance=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        sInstance=this;

        Intent intent=new Intent(this,TranslateService.class);
        conn=new MyConn();
        bindService(intent,conn,BIND_AUTO_CREATE);

        taskListview=findViewById(R.id.tastlistview);
        taskListview.setAdapter(TranslateService.dlAdapter);
        showdownload=findViewById(R.id.downloadlist);
        showcomplete=findViewById(R.id.complete);
        showupload=findViewById(R.id.uploadlist);
        storepath=findViewById(R.id.storepath);
        goback=findViewById(R.id.taskBack);

        storepath.setText("储存目录:"+TranslateService.downloadpath);

        showdownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdownload.setTextColor(Color.parseColor("#37b0e4"));
                showcomplete.setTextColor(Color.BLACK);
                showupload.setTextColor(Color.BLACK);
                taskListview.setAdapter(TranslateService.dlAdapter);
            }
        });

        showcomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdownload.setTextColor(Color.BLACK);
                showupload.setTextColor(Color.BLACK);
                showcomplete.setTextColor(Color.parseColor("#37b0e4"));
                taskListview.setAdapter(TranslateService.cpAdapter);
            }
        });

        showupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdownload.setTextColor(Color.BLACK);
                showupload.setTextColor(Color.parseColor("#37b0e4"));
                showcomplete.setTextColor(Color.BLACK);
                taskListview.setAdapter(TranslateService.ulAdapter);
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void showDeleteDialog(final String url, final int position){
        final AlertDialog.Builder DeleteDialog =
                new AlertDialog.Builder(TaskList.this)
                        .setTitle("删除确定")
                        .setMessage("确定删除文件?")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myBinder.deletefile(url,position);
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
        DeleteDialog.show();
    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder=(TranslateService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onDestroy(){
        unbindService(conn);
        super.onDestroy();
    }
}

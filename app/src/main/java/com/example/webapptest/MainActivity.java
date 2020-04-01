package com.example.webapptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.android.tu.loadingdialog.LoadingDailog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private String userName,psw;  //获取用户名，密码
    private EditText et_user_name,et_psw;   //编辑框
    private long mExitTime;                 //存储系统时间
    private String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AlertDialog dialog;
    private LoadingDailog loadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int i= ContextCompat.checkSelfPermission(this,permissions[0]);
            if(i!= PackageManager.PERMISSION_GRANTED){
                showDialogTipUserRequestPermission();
            }
        }
    }

    //获取页面控件
    private void init() {
        //获取控件id
        TextView tv_register = (TextView) findViewById(R.id.tv_register);
        TextView tv_find_psw = (TextView) findViewById(R.id.tv_find_psw);
        Button btn_login = (Button) findViewById(R.id.btn_login);
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_psw = (EditText) findViewById(R.id.et_psw);

        //注册控件的点击事件
        tv_register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //跳转到注册界面，并实现注册功能
                Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        //找回密码的点击事件(待写)
        tv_find_psw.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showFindPswDialog();
            }
        });

        //登陆按钮的点击事件
        btn_login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //获取用户名和密码
                userName = et_user_name.getText().toString().trim();
                psw = et_psw.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(MainActivity.this,"请输入用户名",Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(psw)) {
                    Toast.makeText(MainActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
                }
                else {
                    LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(MainActivity.this).setMessage("请求中...").setCancelable(false).setCancelOutside(false);
                    loadDialog=loadBuilder.create();
                    loadDialog.show();
                    LoginRequest(userName,psw);
                }
            }
        });
    }

    public void LoginRequest(final String accountNumber,final String password) {
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/LoginServlet";
        String tag = "Login";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                    String result = jsonObject.getString("Result");
                    if(result.equals("success")) {
                        getUserInfoRequest(accountNumber);
                    } else {
                        if(loadDialog!=null){
                            loadDialog.dismiss();
                        }
                        Toast.makeText(MainActivity.this,"密码或账号错误",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    if(loadDialog!=null){
                        loadDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if(loadDialog!=null){
                    loadDialog.dismiss();
                }
                if (error.networkResponse == null) {
                    Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(MainActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("AccountNumber",accountNumber);
                params.put("Password",password);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public void getUserInfoRequest(final String account){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/GetUserInfoServlet";
        String tag = "GetUserInfo";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String email=jsonObject.getString("Email");
                    double pansize=(double)jsonObject.getLong("PanSize");
                    int maxshare=jsonObject.getInt("MaxShare");
                    if(!email.equals("noinfo")) {
                        //跳转操作
                        PATH.initRoot("\\"+account);
                        User.initUsername(account);
                        User.initmaxsize(pansize);
                        User.initeamil(email);
                        User.initmaxshare(maxshare);
                        TranslateService.initdownloadpath(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"webpanDownload"+File.separator+account+File.separator);
                        if(loadDialog!=null){
                            loadDialog.dismiss();
                        }
                        Toast.makeText(MainActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,ShowDirActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        if(loadDialog!=null){
                            loadDialog.dismiss();
                        }
                        Toast.makeText(MainActivity.this,"获取用户信息失败!",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    if(loadDialog!=null){
                        loadDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if(loadDialog!=null){
                    loadDialog.dismiss();
                }
                if (error.networkResponse == null) {
                    Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(MainActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Account",account);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if((System.currentTimeMillis()-mExitTime)>2000) {
                Object mHelperUtils;
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private void showFindPswDialog(){
        final AlertDialog.Builder findpswDialog =
                new AlertDialog.Builder(MainActivity.this);
        findpswDialog.setTitle("请联系管理员");
        findpswDialog.setMessage("管理员邮箱:1196949512@qq.com");
        findpswDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        // 显示
        findpswDialog.show();
    }

    private void showDialogTipUserRequestPermission(){
        new AlertDialog.Builder(this).setTitle("存储权限不可用").setMessage("应用程序需要获取存储空间来为您存储下载文件;\n否则，您将无法正常使用该程序")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setCancelable(false).show();
    }

    private void startRequestPermission(){
        ActivityCompat.requestPermissions(this,permissions,321);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==321){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    boolean b=shouldShowRequestPermissionRationale(permissions[0]);
                    if(!b){
                        showDialogTipUserGoToAppSetting();
                    } else {
                        finish();
                    }
                } else{
                    Toast.makeText(this,"权限获取成功",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showDialogTipUserGoToAppSetting() {
        dialog=new AlertDialog.Builder(this).setTitle("存储权限不可用").setMessage("请在-应用设置-权限-中，允许程序使用存储权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    private void goToAppSetting(){
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri=Uri.fromParts("package",getPackageName(),null);
        intent.setData(uri);
        startActivityForResult(intent,123);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==123){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                int i=ContextCompat.checkSelfPermission(this,permissions[0]);
                if(i!=PackageManager.PERMISSION_GRANTED){
                    showDialogTipUserGoToAppSetting();
                } else{
                    if(dialog!=null&&dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Toast.makeText(this,"获取权限成功",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

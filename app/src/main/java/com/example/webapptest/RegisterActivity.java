package com.example.webapptest;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class RegisterActivity extends AppCompatActivity {

    //用户名，密码，再次输入的密码,邮箱的控件
    private EditText et_user_name, et_psw, et_psw_again,et_email;
    //用户名，密码，再次输入的密码,邮箱的控件的获取值
    private String userName, psw, pswAgain,email;
    private ImageButton goback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,注册界面
        setContentView(R.layout.activity_register);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
        goback=(ImageButton) findViewById(R.id.regBack);

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {

        //从activity_register.xml 页面中获取对应的UI控件
        Button btn_register = (Button) findViewById(R.id.btn_register);
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_psw = (EditText) findViewById(R.id.et_psw);
        et_psw_again = (EditText) findViewById(R.id.et_psw_again);
        et_email=(EditText) findViewById(R.id.et_email);

        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //获取输入框输入值
                userName = et_user_name.getText().toString().trim();
                psw = et_psw.getText().toString().trim();
                pswAgain=et_psw_again.getText().toString().trim();
                email=et_email.getText().toString().trim();

                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(psw)) {
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(pswAgain)) {
                    Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                } else if (!psw.equals(pswAgain)) {
                    Toast.makeText(RegisterActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this,"请输入邮箱",Toast.LENGTH_SHORT).show();
                }else {
                    RegisterRequest(userName,psw,email);
                }
            }

        });
    }


    public void RegisterRequest(final String accountNumber, final String password,final String email) {
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/RegisterServlet";
        String tag = "Resgister";

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
                    if(result.equals("complete")) {
                        shownormalDialog();
                    } else if(result.equals("repeat")){
                        Toast.makeText(RegisterActivity.this,"账号已存在",Toast.LENGTH_SHORT).show();
                    }else if(result.equals("emailrepeat")) {
                        Toast.makeText(RegisterActivity.this,"邮箱已注册",Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(RegisterActivity.this,"数据库异常",Toast.LENGTH_SHORT).show();}
                } catch (JSONException e) {
                    Toast.makeText(RegisterActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }

        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(RegisterActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(RegisterActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("AccountNumber", accountNumber);
                params.put("Password", password);
                params.put("Email",email);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    private void shownormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(RegisterActivity.this);
        normalDialog.setIcon(R.drawable.ic_dialog_right);
        normalDialog.setTitle("注册信息");
        normalDialog.setMessage("注册成功！");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        // 显示
        normalDialog.show();
    }
}


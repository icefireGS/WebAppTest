package com.example.webapptest;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowShareActivity extends AppCompatActivity implements shareCallback {
    private ListView shareListView;
    private shareAdapter adpterShare;
    private List<ShareItem> sharelist=new ArrayList<>();
    private ImageButton goback;
    private TextView nowsharenum;
    private TextView maxsharenum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_share);
        nowsharenum=(TextView) findViewById(R.id.sharenum);
        maxsharenum=(TextView) findViewById(R.id.maxsharenum);
        showShareListQuest(User.username);
        shareListView=(ListView) findViewById(R.id.shareListView);
        adpterShare=new shareAdapter(this,R.layout.share_item,sharelist,this);
        shareListView.setAdapter(adpterShare);

        maxsharenum.setText("/"+String.valueOf(User.maxshare));

        goback=(ImageButton) findViewById(R.id.shareBack);

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void showShareListQuest(final String account){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/ShowShareServlet";
        String tag = "ShowShare";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                sharelist.clear();

                try {
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    int sharenum=jsonObject.getInt("sharenum");
                    JSONArray sharenamelist=jsonObject.getJSONArray("sharelist");
                    JSONArray codelist=jsonObject.getJSONArray("codelist");
                    JSONArray isdeletelist=jsonObject.getJSONArray("isdelete");

                    for(int i=0;i<sharenum;i++){
                        String filename=sharenamelist.getString(i);
                        String code=codelist.getString(i);
                        String isdel=isdeletelist.getString(i);
                        ShareItem item=new ShareItem();
                        item.setFilename(filename);
                        item.setCode(code);
                        item.setIsdelete(isdel);
                        sharelist.add(item);
                    }

                    adpterShare.notifyDataSetChanged();
                    if(nowsharenum!=null){
                        nowsharenum.setText(String.valueOf(sharenum));
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowShareActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowShareActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowShareActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowShareActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
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

    protected void deleteQuest(final String code){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/DeleteShareServlet";
        String tag = "DeleteShare";

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
                    String result=jsonObject.getString("Result");

                    if(result.equals("success")){
                        Toast.makeText(ShowShareActivity.this,"删除成功!",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ShowShareActivity.this,"删除失败!",Toast.LENGTH_SHORT).show();
                    }

                    showShareListQuest(User.username);
                } catch (JSONException e) {
                    Toast.makeText(ShowShareActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowShareActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowShareActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowShareActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Code",code);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);

    }

    @Override
    public void deleteclick(int position) {
        showDeleteDialog(position);
    }

    public void showDeleteDialog(final int position){
        final AlertDialog.Builder deleteDialog =
                new AlertDialog.Builder(ShowShareActivity.this);
        deleteDialog.setTitle("删除确定");
        deleteDialog.setMessage("确定删除分享?");
        deleteDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteQuest(sharelist.get(position).getCode());
                    }
                });
        deleteDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // 显示
        deleteDialog.show();
    }
}

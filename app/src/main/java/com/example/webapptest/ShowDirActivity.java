package com.example.webapptest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowDirActivity extends AppCompatActivity {

    private dirfileAdapter adaptermain,adapterleft;      //列表适配器
    private ListView listview;              //文件列表
    private ListView menuList;              //菜单列表
    private TextView dirview;               //导航栏显示当前目录
    private View contentView;                       //组件
    private Button renamefile,renamedir,downloadfile,deletefile,deletedir,sharefile;       //弹窗按钮
    private Button user,upload,makedir;                         //导航栏按钮
    private PopupWindow mPopupWindow;              //listview弹窗
    private PopupWindow leftPopWindow;             //左侧弹窗
    private int mScreenHeight;                     //屏幕高度
    private long mExitTime;                 //存储系统时间
    private List<dirfile> fileList=new ArrayList<>();     //文件列表
    private List<dirfile> menuarray=new ArrayList<>();    //菜单子项列表
    private PATH Pathstack=new PATH();                    //存储路径信息
    private final int leftpop=3;                     //左侧弹窗类型
    private final int dir=1;                        //列表项目文件夹类型
    private final int file=2;                       //列表项目文件类型
    private final int back=0;                       //列表项目返回类型
    public static double size=0;
    private MyConn conn;
    private TranslateService.MyBinder myBinder;
    public static ShowDirActivity myInstance=null;
    private LoadingDailog loadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dir);
        listview=(ListView) findViewById(R.id.list_view);
        dirview=(TextView) findViewById(R.id.dirtext);
        user=(Button) findViewById(R.id.user);
        makedir=(Button) findViewById(R.id.makedir);
        upload=(Button) findViewById(R.id.upload);
        initDirfile();
        mScreenHeight=getScreenHeight();
        showPansizeQuest(PATH.root);
        myInstance=this;

        File mkfile=new File(TranslateService.downloadpath+"share/");
        if(!mkfile.exists()){
            mkfile.mkdirs();
        }

        Intent intent=new Intent(this,TranslateService.class);
        conn=new MyConn();
        bindService(intent,conn,BIND_AUTO_CREATE);

        //文件列表点击事件
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dirfile clickfile=fileList.get(position);
                if(clickfile.getDirfileImage()==R.drawable.dir){
                    String postpath=Pathstack.getPath()+"\\"+clickfile.getDirfileName();
                    ShowDirRequest(postpath,dir,"\\"+clickfile.getDirfileName(),false);
                } else if(clickfile.getDirfileImage()==R.drawable.back){
                    String postpath=Pathstack.getPrePath();
                    ShowDirRequest(postpath,back,Pathstack.getPreSinglePath(),false);
                }
            }
        });

        //文件列表长按事件
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dirfile clickfile=fileList.get(position);
                if(clickfile.getDirfileImage()==R.drawable.dir) {
                    initPopwindow(dir,clickfile.getDirfileName());
                } else if(clickfile.getDirfileImage()==R.drawable.file){
                    initPopwindow(file,clickfile.getDirfileName());
                } else {
                    return false;
                }

                // 获取被点击项所在位置
                int[] a = new int[2];
                view.getLocationOnScreen(a);
                //adv高度
                int advheight=view.getHeight();

                // 在指定位置显示弹窗, 以底部中间为基准点
                mPopupWindow.showAtLocation(listview, Gravity.BOTTOM | Gravity.CENTER, 0, mScreenHeight - a[1]+advheight);
                return true;
            }
        });

        //左上人物按钮点击事件
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopwindow(leftpop,null);
                leftPopWindow.showAtLocation(v, Gravity.LEFT,0,0);
            }
        });

        //创建目录按钮点击事件
        makedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  showInputDialog();
            }
        });

        //上传按钮点击事件
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path;
            String parenturl=URL.ftproot;
            String rooturl=Pathstack.getPath().replace("\\","/");
            String url=parenturl+rooturl;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                myBinder.startupload(path,url);
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                myBinder.startupload(path,url);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //初始化文件列表
    private void initDirfile() {
        ShowDirRequest(PATH.getRoot(),dir,PATH.getRoot(),false);
        adaptermain=new dirfileAdapter(this,R.layout.file_item,fileList);
        listview.setAdapter(adaptermain);
    }

    //初始化popupwindow(包括测弹框和长按框)
    private void initPopwindow(int filecate,final String dirfilename){
        if(filecate==dir){
            LayoutInflater lf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            contentView = lf.inflate(R.layout.popwindowdir, null);
            mPopupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setAnimationStyle(R.style.scale_animation);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            renamedir=(Button) contentView.findViewById(R.id.renamedir);
            deletedir=(Button) contentView.findViewById(R.id.deletedir);

            renamedir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showRenameDialog("dir",dirfilename);
                    mPopupWindow.dismiss();
                }
            });

            deletedir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(dirfilename);
                    mPopupWindow.dismiss();
                }
            });

        } else if(filecate==file) {
            LayoutInflater lf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            contentView = lf.inflate(R.layout.popwindowfile, null);
            mPopupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setAnimationStyle(R.style.scale_animation);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            renamefile=(Button) contentView.findViewById(R.id.renamefile);
            deletefile=(Button) contentView.findViewById(R.id.deletefile);
            downloadfile=(Button) contentView.findViewById(R.id.downloadfile);
            sharefile=(Button) contentView.findViewById(R.id.share);

            renamefile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showRenameDialog("file",dirfilename);
                    mPopupWindow.dismiss();
                }
            });

            deletefile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(dirfilename);
                    mPopupWindow.dismiss();
                }
            });

            downloadfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String root=URL.httproot+"/webpan";
                    String panpath=Pathstack.getPath().replace("\\","/");
                    String url=root+panpath+"/"+dirfilename;
                    myBinder.startdownload(url);
                    mPopupWindow.dismiss();
                }
            });

            sharefile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path=Pathstack.getPath().replace("\\","/")+File.separator+dirfilename;
                    showShareDialog(dirfilename,path);
                    mPopupWindow.dismiss();
                }
            });

        } else {
            LayoutInflater lf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            contentView = lf.inflate(R.layout.popwindowleft, null);
            leftPopWindow = new PopupWindow(contentView,800, ViewGroup.LayoutParams.MATCH_PARENT,true);
            leftPopWindow.setAnimationStyle(R.style.pop_animation);
            leftPopWindow.setFocusable(true);
            leftPopWindow.setOutsideTouchable(true);
            leftPopWindow.setBackgroundDrawable(new BitmapDrawable());

            initmenu();
            menuList=(ListView) contentView.findViewById(R.id.menulist);
            adapterleft=new dirfileAdapter(ShowDirActivity.this,R.layout.menu_item,menuarray);
            menuList.setAdapter(adapterleft);

            NumberProgressBar progressbar=(NumberProgressBar) contentView.findViewById(R.id.capbar);
            TextView sizetext=(TextView) contentView.findViewById(R.id.panSpace);
            TextView username=(TextView) contentView.findViewById(R.id.userName);
            TextView pansize=(TextView) contentView.findViewById(R.id.maxsize);

            username.setText(User.username);
            double convertsize=User.maxsize/1073741824.0;
            convertsize=((double) Math.round(convertsize * 10)) / 10;
            pansize.setText("/"+String.valueOf(convertsize)+"GB");

            if(size<102.4) {
                int sizeByte=(int)size;
                sizetext.setText(String.valueOf(sizeByte)+"Byte");
                progressbar.setProgress(1);
                if(size==0){
                    progressbar.setProgress(0);
                }
            } else {
                if(size<104857.6) {
                    double sizeKB = size / 1024.0;
                    sizeKB = ((double) Math.round(sizeKB * 10)) / 10;
                    sizetext.setText(String.valueOf(sizeKB) + "KB");
                    progressbar.setProgress(1);
                } else {
                    if(size<107374182.4){
                        double sizeMB=size/1048576.0;
                        sizeMB = ((double) Math.round(sizeMB * 10)) / 10;
                        sizetext.setText(String.valueOf(sizeMB) + "MB");
                        if(size>(User.maxsize/100)){
                            int pecent=(int)(size*100/User.maxsize);
                            progressbar.setProgress(pecent);
                        }else{
                            progressbar.setProgress(1);
                        }
                    } else {
                        double sizeGB=size/1073741824.0;
                        sizeGB=((double) Math.round(sizeGB * 10)) / 10;
                        sizetext.setText(String.valueOf(sizeGB) + "GB");
                        int pecent=(int)(size*100/User.maxsize);
                        progressbar.setProgress(pecent);
                    }
                }
            }

            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch(position) {
                        case 0:
                            Intent intent=new Intent(ShowDirActivity.this,TaskList.class);
                            startActivity(intent);
                            leftPopWindow.dismiss();
                            //任务列表
                            break;
                        case 1:
                            Intent newintent=new Intent(ShowDirActivity.this,ShowShareActivity.class);
                            startActivity(newintent);
                            leftPopWindow.dismiss();
                            //分享列表
                            break;
                        case 2:
                            showGetShareDialog();
                            leftPopWindow.dismiss();
                            //获取分享
                            break;
                        case 3:
                            //退出程序
                            showExitDialog();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    //初始化菜单列表
    public void initmenu(){
        menuarray.clear();
        dirfile item1=new dirfile();
        dirfile item2=new dirfile();
        dirfile item3=new dirfile();
        dirfile item4=new dirfile();
        item1.setDirfileImage(R.drawable.tasklist);
        item1.setDirfileName("任务列表");
        item2.setDirfileImage(R.drawable.share);
        item2.setDirfileName("分享列表");
        item3.setDirfileImage(R.drawable.download);
        item3.setDirfileName("获取分享");
        item4.setDirfileImage(R.drawable.exit);
        item4.setDirfileName("退出程序");
        menuarray.add(item1);
        menuarray.add(item2);
        menuarray.add(item3);
        menuarray.add(item4);
    }

    //显示目录信息请求
    public void ShowDirRequest(final String path, final int filecate, final String nowpath, final boolean onlyUpdateNowpath){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/ShowDirServlet";
        String tag = "ShowDir";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                fileList.clear();

                if(!path.equals(PATH.getRoot())){
                    dirfile backfile=new dirfile();
                    backfile.setDirfileName("...");
                    backfile.setDirfileImage(R.drawable.back);
                    fileList.add(backfile);
                }

                try {
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    int filenum=jsonObject.getInt("filenum");
                    int dirnum=jsonObject.getInt("dirnum");
                    JSONArray filename=jsonObject.getJSONArray("filename");
                    JSONArray dirname=jsonObject.getJSONArray("dirname");

                    for(int i=0;i<dirnum;i++){
                        String name=dirname.getString(i);
                        dirfile df=new dirfile();
                        df.setDirfileName(name);
                        df.setDirfileImage(R.drawable.dir);
                        fileList.add(df);
                    }

                    for(int i=0;i<filenum;i++){
                        String name=filename.getString(i);
                        dirfile df=new dirfile();
                        df.setDirfileName(name);
                        df.setDirfileImage(R.drawable.file);
                        fileList.add(df);
                    }

                    //更新listview适配器
                    adaptermain.notifyDataSetChanged();

                    if(!onlyUpdateNowpath) {
                        //目录存储器更新
                        if (filecate == dir) {
                            Pathstack.addPath(nowpath);
                        } else {
                            Pathstack.removeTopPath();
                        }

                        //dirtext更新
                        dirview.setText(nowpath.substring(1));
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Path",path);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    //创建目录请求
    public void mkdirRequest(final String username,final String parentpath,final String dirname) {
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/CreateDirServlet";
        String tag = "CreateDir";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String result=jsonObject.getString("Result");

                    if(result.equals("success")){
                        ShowDirRequest(parentpath,-1,null,true);
                        Toast.makeText(ShowDirActivity.this, "文件夹创建成功！", Toast.LENGTH_SHORT).show();
                    } else if(result.equals("repeat")){
                        Toast.makeText(ShowDirActivity.this, "该文件夹已存在！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShowDirActivity.this, "文件夹创建失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("AccountNumber",username);
                params.put("Path",parentpath);
                params.put("Name",dirname);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    //重命名文件
    public void renameQuest(final String path,final String nowname,final String rename,final String filecate){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/RenameDirFile";
        String tag = "RenameDirFile";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String result=jsonObject.getString("Result");

                    if(result.equals("success")){
                        ShowDirRequest(path,-1,null,true);
                        Toast.makeText(ShowDirActivity.this, "重命名成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShowDirActivity.this, "重命名失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Path",path);
                params.put("NowName",nowname);
                params.put("ReName",rename);
                params.put("FileCate",filecate);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public void deleteQuest(final String path,final String name){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/DeleteServlet";
        String tag = "deleteDirFile";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String result=jsonObject.getString("Result");

                    if(result.equals("success")){
                        ShowDirRequest(path,-1,null,true);
                        showPansizeQuest(PATH.root);
                        Toast.makeText(ShowDirActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShowDirActivity.this, "删除失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Path",path);
                params.put("Name",name);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public void showPansizeQuest(final String path){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/GetPanSize";
        String tag = "showPanSize";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    size=(double)jsonObject.getLong("Size");
                } catch (JSONException e) {
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Path",path);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public void createShareQuest(final String account,final String path){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/CreateShareServlet";
        String tag = "createShare";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String result=jsonObject.getString("Result");
                    if(result.equals("success")){
                        String code=jsonObject.getString("Code");
                        loadDialog.dismiss();
                        showShareInfoDialog(result,code);
                    }else {
                        loadDialog.dismiss();
                        showShareInfoDialog(result,"");
                    }
                } catch (JSONException e) {
                    loadDialog.dismiss();
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                loadDialog.dismiss();
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
                }
                Log.e("TAG",error.getMessage(),error);
            }
        }) {
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Account",account);
                params.put("Path",path);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }

    public void GetShareQuest(final String code){
        //请求地址
        String url = URL.httproot+"/MyFirstWebApp/ShareDownloadServlet";
        String tag = "GetShareDownload";

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest,定义字符串请求的请求方式为POST
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = (JSONObject) new JSONObject(response);
                    String path=jsonObject.getString("path");
                    String result="";
                    if(path.equals("nofile")||path.equals("nocode")||path.equals("exception")){
                        result=path;
                    }
                    String name=path.substring(path.lastIndexOf("/")+1);
                    String url=URL.httproot+"/webpan"+path;
                    loadDialog.dismiss();
                    GetShareInfoDialog(result,name,url);
                } catch (JSONException e) {
                    loadDialog.dismiss();
                    Toast.makeText(ShowDirActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
                    Log.e("TAG",e.getMessage(),e);
                }
            }
        },new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                loadDialog.dismiss();
                if (error.networkResponse == null) {
                    Toast.makeText(ShowDirActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                } else if (error.networkResponse.statusCode == 408) {
                    Toast.makeText(ShowDirActivity.this, "网络连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowDirActivity.this, "服务器内部错误", Toast.LENGTH_SHORT).show();
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

    //双击退出
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if((System.currentTimeMillis()-mExitTime)>2000) {
                Object mHelperUtils;
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }else{
                if(mPopupWindow!=null) {mPopupWindow.dismiss();}
                if(leftPopWindow!=null) {leftPopWindow.dismiss();}
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    //获取屏幕高度
    private int getScreenHeight() {
        // 获取屏幕实际像素
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = ShowDirActivity.this.getWindowManager().getDefaultDisplay();
        display.getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    //输入创建目录名dialog
    private void showInputDialog() {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(ShowDirActivity.this);
        final AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        inputDialog.setTitle("请输入新建文件夹名字").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String dialogtext=editText.getText().toString();
                        if(dialogtext.isEmpty()){
                            Toast.makeText(ShowDirActivity.this, "文件夹名不允许为空！", Toast.LENGTH_SHORT).show();
                        } else {
                            mkdirRequest(User.username,Pathstack.getPath(),dialogtext);
                        }
                    }
                });
        inputDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // 显示
        inputDialog.show();
    }

    private void showRenameDialog(final String filecate,final String nowname){
        final View dialogview;
        AlertDialog.Builder renameDialog=new AlertDialog.Builder(ShowDirActivity.this);
        if(filecate.equals("dir")){
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.renamedir_dialog,null);
        } else {
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.renamefile_dialog,null);
            TextView renameFiletext=(TextView) dialogview.findViewById(R.id.text_renamefile);
            String extention=nowname.substring(nowname.lastIndexOf("."));
            renameFiletext.setText(extention);
        }
        renameDialog.setTitle("请输入新名字");
        renameDialog.setView(dialogview);
        renameDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(filecate.equals("dir")){
                    EditText edit_newname=(EditText) dialogview.findViewById(R.id.edit_renamedir);
                    renameQuest(Pathstack.getPath(),nowname,edit_newname.getText().toString(),filecate);
                } else {
                    EditText edit_newname=(EditText) dialogview.findViewById(R.id.edit_renamefile);
                    renameQuest(Pathstack.getPath(),nowname,edit_newname.getText().toString()+nowname.substring(nowname.lastIndexOf(".")),filecate);
                }
            }
        });

        renameDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
        });

        //显示
        renameDialog.show();
    }

    //显示share信息Dialog
    private void showShareInfoDialog(final String result,final String code){
        final View dialogview;
        AlertDialog.Builder infoDialog=new AlertDialog.Builder(ShowDirActivity.this);
        if(result.equals("success")){
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.sharecode_dialog,null);
            TextView codeview=(TextView) dialogview.findViewById(R.id.sharecode);
            codeview.setText(code);
        }else if(result.equals("full")){
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.sharefaile_dialog,null);
            TextView shareinfo=(TextView) dialogview.findViewById(R.id.shareinfo);
            shareinfo.setText("分享数量已达上限!");
        }else if(result.equals("repeat")){
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.sharefaile_dialog,null);
            TextView shareinfo=(TextView) dialogview.findViewById(R.id.shareinfo);
            shareinfo.setText("该分享已存在!");
        } else{
            dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.sharefaile_dialog,null);
            TextView shareinfo=(TextView) dialogview.findViewById(R.id.shareinfo);
            shareinfo.setText("分享失败!");
        }

        infoDialog.setTitle("返回信息");
        infoDialog.setView(dialogview);
        infoDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        infoDialog.show();
    }

    //获取分享Dialog
    private void showGetShareDialog(){
        final View dialogview;
        dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.getshare_dialog,null);
        final AlertDialog.Builder getshareDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        getshareDialog.setTitle("请输入分享码");
        getshareDialog.setView(dialogview);
        getshareDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText codeEdit=(EditText) dialogview.findViewById(R.id.getsharecode);
                String code= codeEdit.getText().toString().trim();
                LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(ShowDirActivity.this).setMessage("请求中...").setCancelable(false).setCancelOutside(false);
                loadDialog=loadBuilder.create();
                loadDialog.show();
                GetShareQuest(code);
            }
        });

        getshareDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        getshareDialog.show();
    }

    //获取分享返回信息Dialog
    private void GetShareInfoDialog(final String result,final String filename,final String url){
        final View dialogview;
        dialogview=LayoutInflater.from(ShowDirActivity.this).inflate(R.layout.downloadshare_dialog,null);
        final AlertDialog.Builder returnShareDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        returnShareDialog.setTitle("返回信息");
        if(result.equals("nofile")){
            returnShareDialog.setMessage("分享文件已删除!");
            returnShareDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else if(result.equals("nocode")){
            returnShareDialog.setMessage("分享码不存在!");
            returnShareDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else if(result.equals("exception")){
            returnShareDialog.setMessage("数据库异常!");
            returnShareDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else{
            returnShareDialog.setView(dialogview);
            TextView filename_text=(TextView) dialogview.findViewById(R.id.downloadShareName);
            filename_text.setText(filename+" 吗?");
            returnShareDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myBinder.startsharedownload(url);
                }
            });

            returnShareDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        returnShareDialog.show();
    }

    //删除Dialog
    private  void showDeleteDialog(final String dirfilename){
        final AlertDialog.Builder deleteDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        deleteDialog.setTitle("删除确定");
        deleteDialog.setMessage("确定删除该文件(夹)?");
        deleteDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteQuest(Pathstack.getPath(),dirfilename);
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

    //分享Dialog
    private void showShareDialog(final String filename,final String path) {
        final AlertDialog.Builder shareDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        shareDialog.setTitle("分享确定");
        shareDialog.setMessage("确定分享"+filename+"?");
        shareDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(ShowDirActivity.this).setMessage("请求中...").setCancelable(false).setCancelOutside(false);
                        loadDialog=loadBuilder.create();
                        loadDialog.show();
                        createShareQuest(User.username,path);
                    }
                });
        shareDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // 显示
        shareDialog.show();
    }

    public void showExitDialog(){
        final AlertDialog.Builder exitDialog =
                new AlertDialog.Builder(ShowDirActivity.this);
        exitDialog.setTitle("退出确定");
        exitDialog.setMessage("确定退出程序?");
        exitDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mPopupWindow!=null) {mPopupWindow.dismiss();}
                        if(leftPopWindow!=null) {leftPopWindow.dismiss();}
                        finish();
                    }
                });
        exitDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // 显示
        exitDialog.show();
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
        myBinder.stoptask();
        unbindService(conn);
        super.onDestroy();
    }

    public void notyfylist(){
        ShowDirRequest(Pathstack.getPath(),-1,null,true);
    }
}

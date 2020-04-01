package com.example.webapptest;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.aria.util.ALog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.support.constraint.Constraints.TAG;

public class TranslateService extends Service implements Callback {
    public static downAdapter dlAdapter;
    public static completeAdapter cpAdapter;
    public static upAdapter ulAdapter;
    private List<DownloadItem> downloadList=new ArrayList<>();
    private List<completeItem> completeList=new ArrayList<>();
    private List<UploadItem> uploadList=new ArrayList<>();
    public static String downloadpath;

    public static void initdownloadpath(final String path){
        downloadpath=path;
    }

    public TranslateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Aria.download(this).register();
        Aria.upload(this).register();
        initDownloadList();
        initCompleteList();
        initUploadList();
        cpAdapter=new completeAdapter(this,R.layout.complete_item,completeList,this);
        dlAdapter=new downAdapter(this,R.layout.translate_item,downloadList,this);
        ulAdapter=new upAdapter(this,R.layout.translate_item,uploadList,this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Aria.download(this).unRegister();
        Aria.upload(this).unRegister();
    }

    //下载注解模块
    @Download.onPre public void onPre(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        if(!searchDownloadList(url)&&!searchCompleteList(url)){
            DownloadItem item=new DownloadItem();
            item.setCancel_image(R.drawable.cancel);
            item.setControl_image(R.drawable.loading);
            item.setFile_image(R.drawable.download);
            item.setFail(false);
            item.setmEntity(task.getDownloadEntity());
            downloadList.add(item);
        } else{
            int result=searchDownloadItem(url);
            if(result>=0){
                downloadList.get(result).setmEntity(task.getDownloadEntity());
            }
        }
        dlAdapter.notifyDataSetChanged();
    }

    @Download.onTaskResume public void onTaskResume(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.get(i).setFail(false);
            downloadList.get(i).setControl_image(R.drawable.stop);
            downloadList.get(i).setmEntity(task.getDownloadEntity());
        }
        dlAdapter.notifyDataSetChanged();
    }

    @Download.onTaskStart public void onTaskStart(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.get(i).setFail(false);
            downloadList.get(i).setControl_image(R.drawable.stop);
            downloadList.get(i).setmEntity(task.getDownloadEntity());
        }
        dlAdapter.notifyDataSetChanged();
    }

    @Download.onTaskStop public void onTaskStop(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.get(i).setControl_image(R.drawable.start);
            downloadList.get(i).setmEntity(task.getDownloadEntity());
        }
        dlAdapter.notifyDataSetChanged();
    }

    @Download.onTaskCancel public void onTaskCancel(DownloadTask task){

    }

    @Download.onTaskFail public void onTaskFail(DownloadTask task,Exception e){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.get(i).setFail(true);
            downloadList.get(i).setControl_image(R.drawable.restart);
            downloadList.get(i).setmEntity(task.getDownloadEntity());
        }
        dlAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), task.getDownloadEntity().getFileName()+"下载失败!", Toast.LENGTH_SHORT).show();
        ALog.d(TAG, ALog.getExceptionString(e));
    }

    @Download.onWait public void onWait(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        if(!searchDownloadList(url)&&!searchCompleteList(url)){
            DownloadItem item=new DownloadItem();
            item.setCancel_image(R.drawable.cancel);
            item.setControl_image(R.drawable.wait);
            item.setFile_image(R.drawable.download);
            item.setFail(false);
            item.setmEntity(task.getDownloadEntity());
            downloadList.add(item);
        } else{
            int result=searchDownloadItem(url);
            if(result>=0){
                downloadList.get(result).setControl_image(R.drawable.wait);
                downloadList.get(result).setmEntity(task.getDownloadEntity());
            }
        }
        dlAdapter.notifyDataSetChanged();
    }

    @Download.onTaskComplete public void onTaskComplete(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.remove(i);
            completeItem item=new completeItem();
            item.setFile_image(R.drawable.download);
            item.setFilename(task.getDownloadEntity().getFileName());
            item.setTranslatecate("下载成功!");
            item.setUrl(task.getDownloadEntity().getUrl());
            item.setOpenfile_iamge(R.drawable.openfile);
            completeList.add(item);
        }
        dlAdapter.notifyDataSetChanged();
        cpAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), task.getDownloadEntity().getFileName()+"下载成功!", Toast.LENGTH_SHORT).show();
    }

    @Download.onTaskRunning public void onTaskRunning(DownloadTask task){
        String url=task.getDownloadEntity().getUrl();
        int i=searchDownloadItem(url);
        if(i>=0){
            downloadList.get(i).setmEntity(task.getDownloadEntity());
        }
        dlAdapter.notifyDataSetChanged();
    }

    //上传注解模块
    @Upload.onPre public void onPre(UploadTask task){
        String path=task.getEntity().getFilePath();
        if(!searchUploadList(path)&&!searchCompleteList(path)){
            UploadItem item=new UploadItem();
            item.setCancel_image(R.drawable.cancel);
            item.setControl_image(R.drawable.loading);
            item.setFile_image(R.drawable.upload);
            item.setFail(false);
            item.setmEntity(task.getEntity());
            uploadList.add(item);
        }else{
            int result=searchUploadItem(path);
            if(result>=0){
                uploadList.get(result).setmEntity(task.getEntity());
            }
        }
        ulAdapter.notifyDataSetChanged();
    }

    @Upload.onTaskResume public void onTaskResume(UploadTask task){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>0){
            uploadList.get(i).setFail(false);
            uploadList.get(i).setControl_image(R.drawable.stop);
            uploadList.get(i).setmEntity(task.getEntity());
        }
        ulAdapter.notifyDataSetChanged();
    }

    @Upload.onTaskStart public void onTaskStart(UploadTask task){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>=0){
            uploadList.get(i).setFail(false);
            uploadList.get(i).setControl_image(R.drawable.stop);
            uploadList.get(i).setmEntity(task.getEntity());
        }
        ulAdapter.notifyDataSetChanged();
    }

    @Upload.onTaskStop public void onTaskStop(UploadTask task){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>=0){
            uploadList.get(i).setControl_image(R.drawable.start);
            uploadList.get(i).setmEntity(task.getEntity());
        }
        ulAdapter.notifyDataSetChanged();
    }

    @Upload.onTaskCancel public void onTaskCancel(UploadTask task){

    }

    @Upload.onTaskFail public void onTaskFail(UploadTask task,Exception e){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>=0){
            uploadList.get(i).setFail(true);
            uploadList.get(i).setControl_image(R.drawable.restart);
            uploadList.get(i).setmEntity(task.getEntity());
        }
        ulAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), task.getEntity().getFileName()+"上传失败!", Toast.LENGTH_SHORT).show();
        ALog.d(TAG, ALog.getExceptionString(e));
    }

    @Upload.onWait public void onWait(UploadTask task){
        String path=task.getEntity().getFilePath();
        if(!searchDownloadList(path)&&!searchCompleteList(path)){
            UploadItem item=new UploadItem();
            item.setCancel_image(R.drawable.cancel);
            item.setControl_image(R.drawable.wait);
            item.setFile_image(R.drawable.upload);
            item.setFail(false);
            item.setmEntity(task.getEntity());
            uploadList.add(item);
        } else{
            int result=searchUploadItem(path);
            if(result>=0){
                uploadList.get(result).setControl_image(R.drawable.wait);
                uploadList.get(result).setmEntity(task.getEntity());
            }
        }
        ulAdapter.notifyDataSetChanged();
    }

    @Upload.onTaskComplete public void onTaskComplete(UploadTask task){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>=0){
            uploadList.remove(i);
            completeItem item=new completeItem();
            item.setFile_image(R.drawable.upload);
            item.setFilename(task.getEntity().getFileName());
            item.setTranslatecate("上传成功!");
            item.setUrl(task.getEntity().getFilePath());
            completeList.add(item);
        }
        ulAdapter.notifyDataSetChanged();
        cpAdapter.notifyDataSetChanged();
        ShowDirActivity.myInstance.showPansizeQuest(PATH.root);
        ShowDirActivity.myInstance.notyfylist();
        Toast.makeText(getApplicationContext(), task.getEntity().getFileName()+"上传成功!", Toast.LENGTH_SHORT).show();
    }

    @Upload.onTaskRunning public void onTaskRunning(UploadTask task){
        String path=task.getEntity().getFilePath();
        int i=searchUploadItem(path);
        if(i>=0){
            uploadList.get(i).setmEntity(task.getEntity());
        }
        ulAdapter.notifyDataSetChanged();
    }

    //服务回调模块
    public class MyBinder extends Binder {

        public void startdownload(String url){
            createDownload(url);
        }

        public void startsharedownload(String url){createShareDownload(url);}

        public void startupload(String filepath,String url){
            createUpload(filepath,url);
        }

        public void stoptask(){stopAlltask();}

        public void deletefile(String url,int position){processDelete(url,position);}

    }

    private void initDownloadList(){
        List<DownloadEntity> temp=Aria.download(this).getAllNotCompletTask();
        if(temp!=null&&!temp.isEmpty()){
            for(int i=0;i<temp.size();i++){
                DownloadItem item=new DownloadItem();
                item.setFile_image(R.drawable.download);
                item.setControl_image(R.drawable.start);
                item.setCancel_image(R.drawable.cancel);
                item.setmEntity(temp.get(i));
                if(temp.get(i).getStr().equals(User.username)) {
                    downloadList.add(item);
                }
            }
        }
    }

    private void initUploadList(){
        List<UploadEntity> temp=Aria.upload(this).getAllNotCompletTask();
        if(temp!=null&&!temp.isEmpty()){
            for(int i=0;i<temp.size();i++){
                UploadItem item=new UploadItem();
                item.setFile_image(R.drawable.upload);
                item.setControl_image(R.drawable.start);
                item.setCancel_image(R.drawable.cancel);
                item.setmEntity(temp.get(i));
                if(temp.get(i).getStr().equals(User.username)) {
                    uploadList.add(item);
                }
            }
        }
    }

    private void initCompleteList(){
        List<DownloadEntity> downcomp=Aria.download(this).getAllCompleteTask();
        List<UploadEntity> upcomp=Aria.upload(this).getAllCompleteTask();
        if(downcomp!=null&&!downcomp.isEmpty()){
            for(int i=0;i<downcomp.size();i++){
                completeItem item=new completeItem();
                item.setFile_image(R.drawable.download);
                item.setFilename(downcomp.get(i).getFileName());
                item.setTranslatecate("下载成功!");
                item.setOpenfile_iamge(R.drawable.openfile);
                item.setUrl(downcomp.get(i).getUrl());
                if(downcomp.get(i).getStr().equals(User.username)) {
                    completeList.add(item);
                }
            }
        }
        if(upcomp!=null&&!upcomp.isEmpty()){
            for(int i=0;i<upcomp.size();i++){
                completeItem item=new completeItem();
                item.setFile_image(R.drawable.upload);
                item.setFilename(upcomp.get(i).getFileName());
                item.setTranslatecate("上传成功!");
                item.setUrl(upcomp.get(i).getFilePath());
                if(upcomp.get(i).getStr().equals(User.username)) {
                    completeList.add(item);
                }
            }
        }
    }

    public void createDownload(String url){
        if(!searchDownloadList(url)&&!searchCompleteList(url)){
            String path=downloadpath+url.substring(url.lastIndexOf("/")+1);
            Aria.download(this).load(url).setFilePath(path).setExtendField(User.username).start();
            Toast.makeText(getApplicationContext(), "添加任务成功!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "该任务已存在!", Toast.LENGTH_SHORT).show();
        }
    }

    public void createShareDownload(String url){
        if(!searchDownloadList(url)&&!searchCompleteList(url)){
            String path=downloadpath+"share"+File.separator+url.substring(url.lastIndexOf("/")+1);
            Aria.download(this).load(url).setFilePath(path).setExtendField(User.username).start();
            Toast.makeText(getApplicationContext(), "添加分享任务成功!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "该分享任务已存在!", Toast.LENGTH_SHORT).show();
        }
    }

    public void createUpload(String filepath,String url){
        if(!searchUploadList(filepath)&&!searchCompleteList(filepath)){
            if(Aria.upload(this).loadFtp(filepath).getSize()+ShowDirActivity.size>User.maxsize){
                Toast.makeText(getApplicationContext(),"添加任务失败，空间不足!",Toast.LENGTH_SHORT).show();
            } else {
                Aria.upload(this).loadFtp(filepath).setUploadUrl(url).login("upload","8757988").setExtendField(User.username).start();
                Toast.makeText(getApplicationContext(), "添加任务成功!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "该任务已存在!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean searchDownloadList(String url){
        for(int i=0;i<downloadList.size();i++){
            if(downloadList.get(i).getmEntity().getUrl().equals(url)){
                return true;
            }
        }
        return false;
    }

    private boolean searchUploadList(String filepath){
        for(int i=0;i<uploadList.size();i++){
            if(uploadList.get(i).getmEntity().getFilePath().equals(filepath)){
                return true;
            }
        }
        return false;
    }

    private boolean searchCompleteList(String url){
        for(int i=0;i<completeList.size();i++){
            if(completeList.get(i).getUrl().equals(url)){
                return true;
            }
        }
        return false;
    }

    private int searchDownloadItem(String url){
        for(int i=0;i<downloadList.size();i++){
            if(downloadList.get(i).getmEntity().getUrl().equals(url)){
                return i;
            }
        }
        return -1;
    }

    private int searchUploadItem(String filepath){
        for(int i=0;i<uploadList.size();i++){
            if(uploadList.get(i).getmEntity().getFilePath().equals(filepath)){
                return i;
            }
        }
        return -1;
    }

    private void removeDownLoadItem(int position){
        DownloadEntity temp=downloadList.get(position).getmEntity();
        Aria.download(this).load(temp.getUrl()).cancel();
        downloadList.remove(position);
        dlAdapter.notifyDataSetChanged();
    }

    private  void removeUploadItem(int position){
        UploadEntity temp=uploadList.get(position).getmEntity();
        Aria.upload(this).loadFtp(temp.getFilePath()).cancel();
        uploadList.remove(position);
        ulAdapter.notifyDataSetChanged();
    }

    //取消事件
    @Override
    public void cancelclick(int position) {
         removeDownLoadItem(position);
        Toast.makeText(getApplicationContext(), "删除任务成功!", Toast.LENGTH_SHORT).show();
    }

    //开始事件
    @Override
    public void startclick(int position) {
        DownloadEntity temp=downloadList.get(position).getmEntity();
        String url=temp.getUrl();
        String path=downloadpath+url.substring(url.lastIndexOf("/")+1);
        Aria.download(this).load(url).setFilePath(path).start();
    }

    //暂停事件
    @Override
    public void stopclick(int position) {
        DownloadEntity temp=downloadList.get(position).getmEntity();
        String url=temp.getUrl();
        Aria.download(this).load(url).stop();
    }

    //重新开始事件
    @Override
    public void restartclick(int position) {
        DownloadEntity temp=downloadList.get(position).getmEntity();
        String url=temp.getUrl();
        String path=downloadpath+url.substring(url.lastIndexOf("/")+1);
        Aria.download(this).load(url).setFilePath(path).start();
    }

    //删除文件
    @Override
    public void deleteclick(int position) {
        String url=completeList.get(position).getUrl();
        TaskList.sInstance.showDeleteDialog(url,position);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void openfileclick(int position) {
        String filename=completeList.get(position).getFilename();
        String path=downloadpath+filename;
        File opfile=new File(path);
        if(opfile.exists()){
            open(this,opfile);
        }
    }

    @Override
    public void upcancelclick(int position) {
        removeUploadItem(position);
        Toast.makeText(getApplicationContext(), "删除任务成功!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updeleteclick(int position) {
        String path=completeList.get(position).getUrl();
        Aria.upload(this).loadFtp(path).cancel();
        completeList.remove(position);
        cpAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "删除记录成功!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void upstartclick(int position) {
        UploadEntity temp=uploadList.get(position).getmEntity();
        String url=temp.getUrl();
        String path=temp.getFilePath();
        Aria.upload(this).loadFtp(path).setUploadUrl(url).login("upload","8757988").start();
    }

    @Override
    public void upstopclick(int position) {
        UploadEntity temp=uploadList.get(position).getmEntity();
        String path=temp.getFilePath();
        Aria.upload(this).loadFtp(path).stop();
    }

    @Override
    public void uprestartclick(int position) {
        UploadEntity temp=uploadList.get(position).getmEntity();
        String url=temp.getUrl();
        String path=temp.getFilePath();
        Aria.upload(this).loadFtp(path).setUploadUrl(url).login("upload","8757988").start();
    }

    private void stopAlltask(){
        Aria.download(this).stopAllTask();
        Aria.upload(this).stopAllTask();
    }

    private void processDelete(final String murl, final int position){
        File defile=new File(Aria.download(this).load(murl).getDownloadEntity().getDownloadPath());
        if(defile.exists()){
            defile.delete();
        }
        Aria.download(this).load(murl).cancel();
        completeList.remove(position);
        cpAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "删除文件成功!", Toast.LENGTH_SHORT).show();
    }

    public static void open(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = MapTable.getUri(context,intent,file);
        String mimeType = MapTable.getMIMEType(file);
        intent.setDataAndType(uri,mimeType);
        try {
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,"手机里没有程序可以打开该文件...",Toast.LENGTH_SHORT).show();
        }
    }
}

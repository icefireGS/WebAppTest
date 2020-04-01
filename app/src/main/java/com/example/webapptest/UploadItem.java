package com.example.webapptest;

import com.arialyy.aria.core.upload.UploadEntity;

public class UploadItem {
    private int file_image;
    private int control_image;
    private int cancel_image;
    private boolean isFail=false;
    private UploadEntity mEntity;

    public int getFile_image(){
        return file_image;
    }

    public void setFile_image(int setimage){
        file_image=setimage;
    }

    public int getControl_image(){
        return control_image;
    }

    public void setControl_image(int setimage){
        control_image=setimage;
    }

    public int getCancel_image(){
        return cancel_image;
    }

    public void setCancel_image(int setimage){
        cancel_image=setimage;
    }

    public UploadEntity getmEntity(){
        return mEntity;
    }

    public void setmEntity(UploadEntity setEntity){
        mEntity=setEntity;
    }

    public String getCurrentConvert(){
        long currentsize=mEntity.getCurrentProgress();
        String curentString;
        if(currentsize >1024){
            if(currentsize>1048576){
                if(currentsize>1073741824){
                    double size=currentsize/1073741824.0;
                    size=((double) Math.round(size * 10)) / 10;
                    curentString=String.valueOf(size+"gb");
                } else {
                    double size = currentsize / 1048576.0;
                    size = ((double) Math.round(size * 10)) / 10;
                    curentString = String.valueOf(size + "mb");
                }
            } else {
                double size = currentsize / 1024.0;
                size = ((double) Math.round(size * 10)) / 10;
                curentString = String.valueOf(size + "kb");
            }
        } else{
            curentString=String.valueOf(currentsize+"b");
        }

        return  curentString;
    }

    public void setFail(boolean fail) {
        isFail = fail;
    }

    public boolean isFail(){
        return isFail;
    }
}

package com.example.webapptest;

public class completeItem {
    private String filename;
    private int file_image;
    private String translatecate;
    private int openfile_iamge;
    private String url;

    public void setUrl(String seturl){
        url=seturl;
    }

    public String getUrl(){
        return url;
    }

    public void setOpenfile_iamge(int image){
        openfile_iamge=image;
    }

    public int getOpenfile_iamge(){
        return openfile_iamge;
    }

    public void setFilename(String name){
        filename=name;
    }

    public String getFilename(){
        return filename;
    }

    public void setTranslatecate(String cate){
        translatecate=cate;
    }

    public String getTranslatecate(){
        return translatecate;
    }

    public void setFile_image(int image){ file_image=image; }

    public int getFile_image(){ return file_image; }
}

package com.example.webapptest;

public class ShareItem {
    private String filename;
    private String code;
    private String isdelete;

    public void setFilename(String name){
        filename=name;
    }

    public String getFilename(){
        return filename;
    }

    public void setCode(String setcode){
        code=setcode;
    }

    public String getCode(){
        return code;
    }

    public void setIsdelete(String isdel){
        isdelete=isdel;
    }

    public String getIsdelete(){
        return isdelete;
    }
}

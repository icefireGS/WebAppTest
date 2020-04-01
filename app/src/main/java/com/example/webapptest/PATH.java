package com.example.webapptest;

import java.util.ArrayList;
import java.util.List;

public class PATH {
    private List<String> pathstack;    //目录信息堆栈
    private int pathnum;              //目录数量
    public static String root;        //根目录

    public PATH(){
        pathstack=new ArrayList<>();
        pathstack.clear();
        pathnum=0;
    }

    public static void initRoot(String rootname){
        root=rootname;
    }

    public static String getRoot(){
        return root;
    }

    public void addPath(String newpath){
        pathstack.add(newpath);
        pathnum++;
    }

    public void removeTopPath(){
        if(pathnum>0) {
            pathnum--;
            pathstack.remove(pathnum);
        }
    }

    public String getPath(){
        String postpath=new String("");
        for(int i=0;i<pathnum;i++){
            postpath=postpath+pathstack.get(i);
        }
        return postpath;
    }

    public String getPrePath(){
        String postpath=new String("");
        if(pathnum<=1){
            postpath=root;
        }
        for(int i=0;i<pathnum-1;i++){
            postpath=postpath+pathstack.get(i);
        }
        return postpath;
    }

    public String getPreSinglePath(){
        if(pathnum>1) {
            String postpath = pathstack.get(pathnum - 2);
            return postpath;
        }
        return "dirtext";
    }
}

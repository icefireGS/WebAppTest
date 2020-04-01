package com.example.webapptest;

public class User {
    public static String username;
    public static double maxsize;
    public static String email;
    public static int maxshare;

    public static void initUsername(String loginuser){
        username=loginuser;
    }
    public static void initmaxsize(double pansize){maxsize=pansize;}
    public static void initeamil(String setemail){email=setemail;}
    public static void initmaxshare(int setmaxshare){maxshare=setmaxshare;}
}

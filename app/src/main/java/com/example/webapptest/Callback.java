package com.example.webapptest;

public interface Callback {
    void cancelclick(int position);
    void startclick(int position);
    void stopclick(int position);
    void restartclick(int position);
    void deleteclick(int position);
    void openfileclick(int position);
    void upcancelclick(int position);
    void updeleteclick(int position);
    void upstartclick(int position);
    void upstopclick(int position);
    void uprestartclick(int position);
}

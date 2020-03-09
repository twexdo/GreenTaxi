package com.adrian.sofergt.objects;

public class sms {

    public String arrived;
    public String to;
    public String from;
    public String content;
    public Double x, y;
    public Integer time;
    public Integer type;
    public Boolean clientConfirm;
    public String nume = null;

    public sms(String to, String from, String content, double x, double y) {
        this.to = to;
        this.from = from;
        this.content = content;
        this.x = x;
        this.y = y;
        type = 0;
        clientConfirm = null;
        arrived = null;
    }

    public sms(String to, String from, int time, String nume) {
        type = 1;
        this.nume = nume;
        this.to = to;
        this.from = from;
        this.time = time;
        this.x = null;
        this.y = null;
        clientConfirm = null;
        arrived = null;
    }

    public sms(String to, String from, boolean b) {
        type = 2;
        this.to = to;
        this.from = from;
        this.time = null;
        this.x = null;
        this.y = null;
        clientConfirm = b;
        arrived = null;
    }

    public sms(String myid, String from, String s) {
        this.from = myid;
        this.to = from;
        x = null;
        y = null;
        clientConfirm = null;
        time = null;
        type = 3;
        arrived = s;
    }
}

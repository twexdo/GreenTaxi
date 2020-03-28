package com.adrian.sofergt;

import android.util.Log;

public class CounterClass {
    private static int counter = 0;

    public static void Add() {
        counter++;
        log();
    }

    public static void Remove() {
        counter--;
        log();
    }

    private static void log() {
        Log.d("CounterClass", "Process count :" + counter);
    }

}

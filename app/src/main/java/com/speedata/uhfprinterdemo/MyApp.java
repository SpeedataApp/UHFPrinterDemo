package com.speedata.uhfprinterdemo;

import android.app.Application;
import android.content.Intent;
import android.os.SystemClock;
import android.serialport.DeviceControlSpd;
import android.util.Log;

import java.io.IOException;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //上电
        try {
            DeviceControlSpd deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 71, 55, 57);
            deviceControl.PowerOnDevice();
            Log.d("zzc", "上电");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

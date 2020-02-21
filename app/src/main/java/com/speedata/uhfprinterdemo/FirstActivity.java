package com.speedata.uhfprinterdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.serialport.DeviceControlSpd;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //上电
        try {
            DeviceControlSpd deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 71, 55, 57);
            deviceControl.PowerOnDevice();
            Log.d("zzc", "上电");
            SystemClock.sleep(3000);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.speedata.uhfprinterdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.DeviceControlSpd;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qr.QRCommand;
import com.qr.USB_Port;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.DataForSendToPrinterTSC;
import net.posprinter.utils.PosPrinterDev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author zzc
 */
public class QrPrinterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvName, tvCategory, tvCount1, tvCount2;
    private Button readBtn, printLoopTest, btnConnect;
    private ListView listView;
    private IUHFService iuhfService;
    private List<String> listBean = new ArrayList<>();
    private ArrayAdapter adapter;
    private SoundPool soundPool;
    private int soundId;
    private boolean isScan = false;
    private boolean isPrint = false;
    private boolean isOpen = false;
    private USB_Port usb_port;
    private Handler myHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initView();
        initSoundPool();
        initData();

    }

    private void initView() {
        findViewById(R.id.btn_print).setOnClickListener(this);
        readBtn = findViewById(R.id.btn_read);
        readBtn.setOnClickListener(this);
        btnConnect = findViewById(R.id.btn_content);
        btnConnect.setOnClickListener(this);
        findViewById(R.id.btn_discontent).setOnClickListener(this);
        findViewById(R.id.btn_usb).setOnClickListener(this);
        printLoopTest = findViewById(R.id.btn_print_test);
        printLoopTest.setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvCategory = findViewById(R.id.tv_category);
        tvCount1 = findViewById(R.id.tv_count1);
        tvCount2 = findViewById(R.id.tv_count2);
        listView = findViewById(R.id.lv_uhf);
    }

    private void initData() {
        //初始化UHF
        try {
            iuhfService = UHFManager.getUHFService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter = new ArrayAdapter<>(QrPrinterActivity.this, android.R.layout.simple_list_item_1, listBean);
        listView.setAdapter(adapter);
        if (iuhfService != null) {
            iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getInventoryData(SpdInventoryData var1) {
//                    if (!listBean.contains(var1.getEpc())) {
//                        soundPool.play(soundId, 1, 1, 0, 0, 1);
//                        listBean.add(var1.getEpc());
//                    }
                    listBean.add(var1.getEpc());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            soundPool.play(soundId, 1, 1, 0, 0, 1);
                            adapter.notifyDataSetChanged();
                            tvCount1.setText(listBean.size() + "件");
                            tvCount2.setText(listBean.size() + "");
                        }
                    });
                }

                @Override
                public void onInventoryStatus(int status) {
                    startUhf();
                    Toast.makeText(QrPrinterActivity.this, "status:" + status, Toast.LENGTH_SHORT).show();
                }
            });
        }
        usb_port = new USB_Port(myHandler, this);
        usb_port.register_USB();
    }

    public void initSoundPool() {
        soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        openDev();
    }

    private class MyHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USB_Port.OPEN:
                    btnConnect.setText("断开连接");
                    showMessage("USB已打开！");
                    isOpen = true;
                    break;
                case USB_Port.ATTACHED:
                    showMessage("监测到设备！");
                    if (!isOpen) {
                        usb_port.openUsb();
                    }
                    break;
                case USB_Port.DETACHED:
                    showMessage("设备已移除！");
                    btnConnect.setText("连接");
                    isOpen = false;
                    break;
                default:
                    break;
            }
        }

    }

    private void showMessage(final String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        if (iuhfService != null) {
            iuhfService.closeDev();
        }
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_print:
                print();
                break;
            case R.id.btn_read:
                if (!isScan) {
                    startUhf();
                } else {
                    stopUhf();
                }
                break;
            case R.id.btn_content:
                if (isOpen) {
                    btnConnect.setText("连接");
                    usb_port.closeUsb();
                    isOpen = false;
                } else {
                    usb_port.openUsb();
                }
                break;
            case R.id.btn_print_test:
                if (!isLoop) {
                    printLoopTest.setText("停止");
                    isLoop = true;
//                    PrintTestThread printTestThread = new PrintTestThread();
//                    printTestThread.start();
                    TestThread testThread = new TestThread();
                    testThread.start();
                } else {
                    printLoopTest.setText("疲劳测试");
                    isLoop = false;
                    stopUhf();
                }
                break;
            default:
                break;
        }
    }

    private void startUhf() {
        isScan = true;
        listBean.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iuhfService.inventoryStart();
                readBtn.setText("停止");
            }
        });
    }

    private void stopUhf() {
        isScan = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iuhfService.inventoryStop();
                readBtn.setText("读取");
            }
        });
    }

    private Bitmap createBitmap() {
        CanvasPrint cp = new CanvasPrint();
        cp.init(480);
        FontProperty fp = new FontProperty();
        fp.setFont(false, false, false, false, 20, null);
        cp.setFontProperty(fp);

        cp.drawText("weewfe");
        cp.drawText(100, 50, "0760-122312455");
        cp.drawText(100, 150, "中山市沙朗肉联厂A15卡");
        cp.drawText(0, 200, "\n");
        return cp.getCanvasImage();
    }

    private void print() {
        final String strName = tvName.getText().toString().trim();
        final String strCategory = tvCategory.getText().toString().trim();
        final String strCount1 = tvCount1.getText().toString().trim();
        if (usb_port != null && usb_port.isOpened()) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test);

            ArrayList<byte[]> data = new ArrayList<byte[]>();
            byte[] wakeup = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            QRCommand printer = new QRCommand(usb_port);
            data.add(wakeup);
            data.add(printer.QiRui_CreatePage(100, 175));
            // 设置打印方向
            data.add(printer.QiRui_Direction(0, 0));
            // 始能切刀，没打完一张自动切纸
            data.add(printer.QiRui_Cut(true));
            // 设置缝隙定位
            data.add(printer.QiRui_SetGap(true));
            // 设置速度3
            data.add(printer.QiRui_Speed(6));
            // 设置浓度
            data.add(printer.QiRui_Density(5));
            // 清除页面缓冲区
            data.add(printer.QiRui_Cls());

            data.add(printer.QiRui_DrawPic(10, 10, bitmap));

            data.add(printer.QiRui_PrintPage(1));
            usb_port.send_usb(data);
        }
    }

    private volatile boolean isLoop = false;

    private class TestThread extends Thread {
        @Override
        public void run() {
            startUhf();
            while (!isInterrupted() && isLoop) {
                SystemClock.sleep(1000 * 60 * 5);
                if (isLoop) {
                    print();
                }
            }
            stopUhf();
        }
    }

    private void openDev() {
        if (iuhfService != null) {
            if (iuhfService.openDev() == 0) {
                Log.d("zzc", "上电成功");
            } else {
                Log.d("zzc", "上电失败");
            }
        } else {
            Log.d("zzc", "uhf初始化失败");
        }
    }

    @Override
    protected void onDestroy() {
        isLoop = false;
        isOpen = false;
        soundPool.release();
        if (usb_port != null) {
            usb_port.unregister_USB();
        }
        //下电
        try {
            DeviceControlSpd deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 71, 55, 57);
            deviceControl.PowerOffDevice();
            Log.d("zzc", "下电");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}

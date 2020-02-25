package com.speedata.uhfprinterdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
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
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> usbList;
    public static IMyBinder binder;
    public static boolean isConnect = false;
    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            //绑定成功
            binder = (IMyBinder) service;
        }
    };
    private TextView tvName, tvCategory, tvCount1, tvCount2;
    private Button readBtn;
    private ListView listView;
    private IUHFService iuhfService;
    private List<String> listBean = new ArrayList<>();
    private ArrayAdapter adapter;
    private SoundPool soundPool;
    private int soundId;
    private View dialogView3;
    private ListView lv_usb;
    private TextView tv_usb;
    private ArrayAdapter<String> adapter3;
    public String usbDev = "";
    private boolean isScan = false;
    private boolean isPrint = false;

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
        findViewById(R.id.btn_content).setOnClickListener(this);
        findViewById(R.id.btn_discontent).setOnClickListener(this);
        findViewById(R.id.btn_usb).setOnClickListener(this);
        findViewById(R.id.btn_print_test).setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvCategory = findViewById(R.id.tv_category);
        tvCount1 = findViewById(R.id.tv_count1);
        tvCount2 = findViewById(R.id.tv_count2);
        listView = findViewById(R.id.lv_uhf);
    }

    private void initData() {
        //绑定service，获取ImyBinder对象
        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        //初始化UHF
        try {
            iuhfService = UHFManager.getUHFService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listBean);
        listView.setAdapter(adapter);
        if (iuhfService != null) {
            iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getInventoryData(SpdInventoryData var1) {
                    if (!listBean.contains(var1.getEpc())) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                        listBean.add(var1.getEpc());
                    }
                    adapter.notifyDataSetChanged();
                    tvCount1.setText(listBean.size() + "件");
                    tvCount2.setText(listBean.size() + "");
                }

                @Override
                public void onInventoryStatus(int status) {

                }
            });
        }
    }

    public void initSoundPool() {
        soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
    }

    protected void setUsb() {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(this);
        dialogView3 = inflater.inflate(R.layout.usb_link, null);
        tv_usb = (TextView) dialogView3.findViewById(R.id.textView1);
        lv_usb = (ListView) dialogView3.findViewById(R.id.listView1);


        usbList = PosPrinterDev.GetUsbPathNames(this);
        if (usbList == null) {
            usbList = new ArrayList<String>();
        }
        tv_usb.setText("检测到的设备：" + usbList.size());
        adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usbList);
        lv_usb.setAdapter(adapter3);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView3)
                .create();
        dialog.show();
        set_lv_usb_listener(dialog);

    }

    private void set_lv_usb_listener(final AlertDialog dialog) {
        // TODO Auto-generated method stub
        lv_usb.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                usbDev = usbList.get(arg2);
                binder.connectUsbPort(getApplicationContext(), usbDev, new UiExecute() {

                    @Override
                    public void onsucess() {

                    }

                    @Override
                    public void onfailed() {

                    }
                });
                dialog.cancel();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        openDev();
    }

    @Override
    protected void onStop() {
        if (iuhfService != null) {
            iuhfService.closeDev();
        }
        super.onStop();
    }

    private void connect() {
        if (binder != null) {
            binder.connectUsbPort(getApplicationContext(), usbDev, new UiExecute() {
                @Override
                public void onsucess() {
                    isConnect = true;
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onfailed() {
                    isConnect = false;
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void disconnect() {
        if (binder != null && isConnect) {
            binder.disconnectCurrentPort(new UiExecute() {
                @Override
                public void onsucess() {
                    isConnect = false;
                    Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onfailed() {

                }
            });
        }
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
                if (!TextUtils.isEmpty(usbDev)) {
                    connect();
                } else {
                    Toast.makeText(MainActivity.this, "没有检测到设备", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_discontent:
                disconnect();
                break;
            case R.id.btn_usb:
                setUsb();
                break;
            case R.id.btn_print_test:
                if (!isLoop){
                    isLoop = true;
                    PrintTestThread printTestThread = new PrintTestThread();
                    printTestThread.start();
                }else {
                    isLoop = false;
                }
                break;
            default:
                break;
        }
    }

    private void startUhf() {
        isScan = true;
        listBean.clear();
        iuhfService.inventoryStart();
        readBtn.setText("停止");
    }

    private void stopUhf() {
        isScan = false;
        iuhfService.inventoryStop();
        readBtn.setText("读取");
    }

    private void print() {
        final String strName = tvName.getText().toString().trim();
        final String strCategory = tvCategory.getText().toString().trim();
        final String strCount1 = tvCount1.getText().toString().trim();
        if (isConnect) {
            binder.writeDataByYouself(new UiExecute() {
                @Override
                public void onsucess() {

                }

                @Override
                public void onfailed() {

                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    DataForSendToPrinterTSC.setCharsetName("gbk");
                    //初始化一个list
                    ArrayList<byte[]> list = new ArrayList<byte[]>();
                    //通过工具类得到一个指令的byte[]数据,以文本为例
                    //首先得设置size标签尺寸,宽60mm,高30mm,也可以调用以dot或inch为单位的方法具体换算参考编程手册
                    byte[] data0 = DataForSendToPrinterTSC.sizeBymm(100, 60);
                    list.add(data0);
                    //设置Gap,同上
                    list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
                    //清除缓存
                    list.add(DataForSendToPrinterTSC.cls());

                    byte[] data1 = DataForSendToPrinterTSC.block(70, 10, 500, 60, "TSS24.BF2", 0, 2, 2, 0, 2,
                            "XXX商城");
                    byte[] line1 = DataForSendToPrinterTSC.bar(70, 70, 500, 3);
                    byte[] data2 = DataForSendToPrinterTSC.text(70, 80, "TSS24.BF2", 0, 1, 1,
                            "订单号      384279857092u938u2");
                    byte[] data3 = DataForSendToPrinterTSC.text(70, 120, "TSS24.BF2", 0, 1, 1,
                            "时间          2019-07-21 17:20");
                    byte[] line2 = DataForSendToPrinterTSC.bar(70, 150, 500, 3);
                    byte[] data4 = DataForSendToPrinterTSC.text(70, 160, "TSS24.BF2", 0, 1, 1,
                            "商品  |  重量  |  价格  |  金额");
                    byte[] line3 = DataForSendToPrinterTSC.bar(70, 190, 500, 3);
                    byte[] data5 = DataForSendToPrinterTSC.text(70, 200, "TSS24.BF2", 0, 1, 1,
                            "猪类1   20公斤    50     10000");
                    byte[] data6 = DataForSendToPrinterTSC.text(70, 230, "TSS24.BF2", 0, 1, 1,
                            "猪类1   20公斤    50     10000");
                    byte[] data7 = DataForSendToPrinterTSC.text(70, 260, "TSS24.BF2", 0, 1, 1,
                            "猪类1   20公斤    50     10000");
                    byte[] line4 = DataForSendToPrinterTSC.bar(70, 290, 500, 3);
                    byte[] data8 = DataForSendToPrinterTSC.text(70, 300, "TSS16.BF2", 0, 2, 2,
                            "总金额                   30000");
                    byte[] shuxian = DataForSendToPrinterTSC.bar(150, 190, 3, 100);
                    byte[] end = DataForSendToPrinterTSC.block(170, 360, 300, 60, "TSS24.BF2", 0, 1, 1, 0, 2,
                            "该技术由xxx公司提供");
                    list.add(data1);
                    list.add(data2);
                    list.add(data3);
                    list.add(data4);
                    list.add(data5);
                    list.add(data6);
                    list.add(data7);
                    list.add(data8);
                    list.add(line1);
                    list.add(line2);
                    list.add(line3);
                    list.add(line4);
                    list.add(shuxian);
                    list.add(end);

                    //打印
                    list.add(DataForSendToPrinterTSC.print(1));
//                    ArrayList<byte[]> list = new ArrayList<>();
//                    byte[] data0 = DataForSendToPrinterTSC.sizeBymm(100, 60);
//                    list.add(data0);
//                    list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
//                    list.add(DataForSendToPrinterTSC.cls());
//                    byte[] data5 = DataForSendToPrinterTSC.text(100, 10, "TSS16.BF2",
//                            0, 2, 2, "出库数目：");
//                    byte[] data6 = DataForSendToPrinterTSC.text(400, 10, "TSS16.BF2",
//                            0, 2, 2, strCount1);
//                    byte[] line3 = DataForSendToPrinterTSC.bar(100, 70, 600, 3);
//                    list.add(data5);
//                    list.add(data6);
//                    list.add(line3);
//                    int i = 0;
//                    for (String str : listBean) {
//                        list.add(DataForSendToPrinterTSC.text(100, 80 + i * 40, "TSS16.BF2", 0, 2, 2, str));
//                        i++;
//                        if (i * 40 >= 160) {
//                            break;
//                        }
//                    }
//                    //打印
//                    list.add(DataForSendToPrinterTSC.print(1));
                    return list;
                }
            });
        }
    }

    private volatile boolean isLoop = false;

    private class PrintTestThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted() && isLoop) {
                print();
                SystemClock.sleep(2000);
            }
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
//        unregisterReceiver(mReceiver);
        soundPool.release();
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

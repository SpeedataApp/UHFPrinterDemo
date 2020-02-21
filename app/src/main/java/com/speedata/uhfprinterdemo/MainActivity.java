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
import android.serialport.DeviceControlSpd;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    //    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            usbList = PosPrinterDev.GetUsbPathNames(MainActivity.this);
//            if (usbList != null) {
//                connect();
//            } else {
//                disconnect();
//            }
//        }
//    };
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initView();
        initReceiver();
        initSoundPool();
        initData();

    }

    private void initView() {
        findViewById(R.id.btn_print).setOnClickListener(this);
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.btn_content).setOnClickListener(this);
        findViewById(R.id.btn_discontent).setOnClickListener(this);
        findViewById(R.id.btn_usb).setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvCategory = findViewById(R.id.tv_category);
        tvCount1 = findViewById(R.id.tv_count1);
        tvCount2 = findViewById(R.id.tv_count2);
        listView = findViewById(R.id.lv_uhf);
    }

    private void initReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mReceiver, filter);
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
                    soundPool.play(soundId, 1, 1, 0, 0, 1);
                    listBean.add(var1.getEpc());
                    adapter.notifyDataSetChanged();
                    tvCount1.setText(listBean.size() + "件");
                    tvCount2.setText(listBean.size() + "");
                    iuhfService.inventoryStop();
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
    protected void onResume() {
        super.onResume();
        openDev();
//        usbList = PosPrinterDev.GetUsbPathNames(this);
//        if (usbList != null) {
//            connect();
//        }
    }

    @Override
    protected void onPause() {
//        disconnect();
        if (iuhfService != null) {
            iuhfService.closeDev();
        }
        super.onPause();
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
                startUhf();
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
            default:
                break;
        }
    }

    private void startUhf() {
        listBean.clear();
        iuhfService.inventoryStart();
    }

    private void print() {
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
                    ArrayList<byte[]> list = new ArrayList<>();
                    byte[] data0 = DataForSendToPrinterTSC.sizeBymm(100, 60);
                    list.add(data0);
                    list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
                    list.add(DataForSendToPrinterTSC.cls());

                    byte[] data1 = DataForSendToPrinterTSC.block(100, 10, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
                            "名称：");
                    byte[] data2 = DataForSendToPrinterTSC.block(400, 10, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
                            tvName.getText().toString());
                    byte[] line1 = DataForSendToPrinterTSC.bar(100, 70, 600, 3);

                    byte[] data3 = DataForSendToPrinterTSC.block(100, 80, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
                            "品类：");
                    byte[] data4 = DataForSendToPrinterTSC.block(400, 80, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
                            tvCategory.getText().toString());
                    byte[] line2 = DataForSendToPrinterTSC.bar(100, 140, 600, 3);

                    byte[] data5 = DataForSendToPrinterTSC.block(100, 150, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
                            "出库数目：");
                    byte[] data6 = DataForSendToPrinterTSC.block(400, 150, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
                            tvCount1.getText().toString());
                    byte[] line3 = DataForSendToPrinterTSC.bar(100, 210, 600, 3);

                    byte[] data7 = DataForSendToPrinterTSC.block(100, 220, 600, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
                            "订单号：");
                    list.add(data1);
                    list.add(data2);
                    list.add(data3);
                    list.add(data4);
                    list.add(data5);
                    list.add(data6);
                    list.add(data7);
                    list.add(line1);
                    list.add(line2);
                    list.add(line3);
                    int i = 0;
                    for (String str : listBean) {
                        list.add(DataForSendToPrinterTSC.block(100, 220 + i * 40, 600, 40, "TSS16.BF2", 0, 2, 2, 0, 3, str));
                        i++;
                        if (i * 40 >= 260) {
                            break;
                        }
                    }
                    //打印
                    list.add(DataForSendToPrinterTSC.print(1));
                    return list;
                }
            });
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

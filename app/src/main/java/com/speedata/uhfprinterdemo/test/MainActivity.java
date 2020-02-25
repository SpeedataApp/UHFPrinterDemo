package com.speedata.uhfprinterdemo.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.serialport.DeviceControlSpd;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.uhfprinterdemo.R;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.DataForSendToPrinterTSC;
import net.posprinter.utils.PosPrinterDev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity {

    public static IMyBinder binder;
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
    public static boolean isConnect = false;
    Button btn0, btn1, btn3, btnSb, btnList, btnPost;
    EditText et;
    private ListView lvUsb;
    public String usbDev = "";
    TextView tvUsb;
    private List<String> usbList;

    private IUHFService iuhfService;
    private SoundPool soundPool;
    private int soundId;
    private ListView listView;
    private List<String> listBean = new ArrayList<>();
    private ArrayAdapter adapter;
    private TextView tvName, tvCategory, tvCount1, tvCount2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //上电
        try {
            DeviceControlSpd deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 71, 55, 57);
            deviceControl.PowerOnDevice();
            Log.d("zzc", "上电");
            SystemClock.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main_test);
        //绑定service，获取ImyBinder对象
        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        initSoundPool();
        initUhf();
        //初始化控件
        btn0 = findViewById(R.id.button0);
        btn1 = findViewById(R.id.button1);
        btn3 = findViewById(R.id.button3);
        btnSb = findViewById(R.id.button7);
        et = findViewById(R.id.editText1);
        btnList = findViewById(R.id.btn_list);
        btnPost = findViewById(R.id.btn_post);

        et.setText("");
        et.setHint(getString(R.string.usbselect));
        et.setEnabled(false);
        btnSb.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.lv_uhf);
        tvName = findViewById(R.id.tv_name);
        tvCategory = findViewById(R.id.tv_category);
        tvCount1 = findViewById(R.id.tv_count1);
        tvCount2 = findViewById(R.id.tv_count2);
        //给控件添加监听事件
        addListener();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listBean);
        listView.setAdapter(adapter);
    }

    public void initSoundPool() {
        soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
    }

    public void initUhf() {
        try {
            iuhfService = UHFManager.getUHFService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        openDev();
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

    private void addListener() {
        // TODO Auto-generated method stub
        btnSb.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setUsb();
            }
        });

        //点击连接按钮，连接打印机
        btn0.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                connectUSB();
            }
        });
        //断开按钮btn1的监听事件
        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                btn0.setText(getString(R.string.connect));
                // TODO Auto-generated method stub
                if (isConnect) {//如果是连接状态才执行断开操作
                    binder.disconnectCurrentPort(new UiExecute() {

                        @Override
                        public void onsucess() {
                            // TODO Auto-generated method stub
                            Toast.makeText(getApplicationContext(), getString(R.string.toast_discon_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onfailed() {
                            // TODO Auto-generated method stub
                            Toast.makeText(getApplicationContext(), getString(R.string.toast_discon_faile), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_present_con), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //单独打印文本
        btn3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //此处用binder里的另外一个发生数据的方法,同样，也要按照编程手册上的示例一样，先设置标签大小
                //如果数据处理较为复杂，请勿选择此方法
                //上面的发送方法的数据处理是在工作线程中完成的，不会阻塞UI线程
//                byte[] data0 = DataForSendToPrinterTSC.sizeBydot(800, 480);
//                byte[] data4 = DataForSendToPrinterTSC.gapBydot(16, 0);
//                byte[] data1 = DataForSendToPrinterTSC.cls();
//
//                byte[] data2 = DataForSendToPrinterTSC.text(10, 10, "5", 0, 2, 2, "12345678");
//                byte[] data3 = DataForSendToPrinterTSC.print(1);
//                byte[] data = byteMerger(byteMerger(byteMerger(byteMerger(data0, data4), data1), data2), data3);
                byte[] data0 = DataForSendToPrinterTSC.sizeBymm(100, 60);
                byte[] data11 = DataForSendToPrinterTSC.gapBymm(2, 0);
                byte[] data12 = DataForSendToPrinterTSC.cls();
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
                byte[] dataPrint = DataForSendToPrinterTSC.print(1);
                byte[] data = byteMergerAll(data0, data11, data12, data1, data2, line1, data3, data4, line2, data5, data6, line3, data7, dataPrint);
                Toast.makeText(getApplicationContext(), Arrays.toString(data), Toast.LENGTH_SHORT)
                        .show();
                if (isConnect) {
                    binder.write(data, new UiExecute() {

                        @Override
                        public void onsucess() {
//                            Toast.makeText(getApplicationContext(), getString(R.string.send_success), Toast.LENGTH_SHORT)
//                                    .show();
                        }

                        @Override
                        public void onfailed() {
                            Toast.makeText(getApplicationContext(), getString(R.string.send_failed), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_con_printer), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnect) {
                    // TODO Auto-generated method stub
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
                            ArrayList<byte[]> list = new ArrayList<>();
                            byte[] data0 = DataForSendToPrinterTSC.sizeBymm(100, 60);
                            list.add(data0);
                            list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
                            list.add(DataForSendToPrinterTSC.cls());

                            byte[] data1 = DataForSendToPrinterTSC.block(100, 10, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
                                    "名称：");
                            byte[] data2 = DataForSendToPrinterTSC.block(400, 10, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
                                    tvName.getText().toString());
//                            byte[] line1 = DataForSendToPrinterTSC.bar(100, 70, 600, 3);
//
//                            byte[] data3 = DataForSendToPrinterTSC.block(100, 80, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
//                                    "品类：");
//                            byte[] data4 = DataForSendToPrinterTSC.block(400, 80, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
//                                    tvCategory.getText().toString());
//                            byte[] line2 = DataForSendToPrinterTSC.bar(100, 140, 600, 3);
//
//                            byte[] data5 = DataForSendToPrinterTSC.block(100, 150, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
//                                    "出库数目：");
//                            byte[] data6 = DataForSendToPrinterTSC.block(400, 150, 300, 60, "TSS16.BF2", 0, 2, 2, 0, 3,
//                                    tvCount1.getText().toString());
//                            byte[] line3 = DataForSendToPrinterTSC.bar(100, 210, 600, 3);
//
//                            byte[] data7 = DataForSendToPrinterTSC.block(100, 220, 600, 60, "TSS16.BF2", 0, 2, 2, 0, 1,
//                                    "订单号：");
                            list.add(data1);
                            list.add(data2);
//                            list.add(data3);
//                            list.add(data4);
//                            list.add(data5);
//                            list.add(data6);
//                            list.add(data7);
//                            list.add(line1);
//                            list.add(line2);
//                            list.add(line3);
//                            int i = 0;
//                            for (String str : listBean) {
//                                list.add(DataForSendToPrinterTSC.block(100, 220 + i * 40, 600, 40, "TSS16.BF2", 0, 2, 2, 0, 3, str));
//                                i++;
//                                if (i * 40 >= 260) {
//                                    break;
//                                }
//                            }
                            //打印
                            list.add(DataForSendToPrinterTSC.print(1));
                            return list;
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_con_printer), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnPost.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iuhfService != null) {
                    iuhfService.inventoryStart();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    protected void setUsb() {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View dialogView3 = inflater.inflate(R.layout.usb_link, null);
        tvUsb = dialogView3.findViewById(R.id.textView1);
        lvUsb = dialogView3.findViewById(R.id.listView1);

        usbList = PosPrinterDev.GetUsbPathNames(this);
        if (usbList == null) {
            usbList = new ArrayList<>();
        }
        tvUsb.setText(getString(R.string.usb_pre_con) + usbList.size());
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usbList);
        lvUsb.setAdapter(adapter3);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView3)
                .create();
        dialog.show();
        set_lv_usb_listener(dialog);

    }

    private void set_lv_usb_listener(final AlertDialog dialog) {
        // TODO Auto-generated method stub
        lvUsb.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                usbDev = usbList.get(arg2);
                et.setText(usbDev);
                binder.connectUsbPort(getApplicationContext(), usbDev, new UiExecute() {

                    @Override
                    public void onsucess() {

                    }

                    @Override
                    public void onfailed() {

                    }
                });
                dialog.cancel();
                Log.i("TAG", usbDev);
            }
        });
    }

    protected void connectUSB() {
        // TODO Auto-generated method stub
        binder.connectUsbPort(getApplicationContext(), et.getText().toString(), new UiExecute() {

            @Override
            public void onsucess() {
                //连接成功后在UI线程中的执行
                isConnect = true;
                Toast.makeText(getApplicationContext(), getString(R.string.con_success), Toast.LENGTH_SHORT).show();
                btn0.setText(getString(R.string.con_success));
            }

            @Override
            public void onfailed() {
                // TODO Auto-generated method stub
                //连接失败后在UI线程中的执行
                isConnect = false;
                Toast.makeText(getApplicationContext(), getString(R.string.con_failed), Toast.LENGTH_SHORT).show();
                btn0.setText(getString(R.string.con_failed));
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        binder.disconnectCurrentPort(new UiExecute() {

            @Override
            public void onsucess() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onfailed() {
                // TODO Auto-generated method stub

            }
        });
        unbindService(conn);
        //下电
        try {
            DeviceControlSpd deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 71, 55, 57);
            deviceControl.PowerOffDevice();
            Log.d("zzc", "下电");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * byte数组拼接
     */
    private byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private byte[] byteMergerAll(byte[]... values) {
        int lengthByte = 0;
        for (byte[] value : values) {
            lengthByte += value.length;
        }
        byte[] allByte = new byte[lengthByte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, allByte, countLength, b.length);
            countLength += b.length;
        }
        return allByte;
    }

}

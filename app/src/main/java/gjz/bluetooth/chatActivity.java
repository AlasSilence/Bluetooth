package gjz.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import gjz.bluetooth.Bluetooth.ServerOrCilent;

public class chatActivity extends Activity implements OnItemClickListener, OnClickListener {
    /* 一些常量，代表服务器的名称 */
    public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
    public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
    public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
    deviceListAdapter mAdapter;
    Context mContext;
    /**
     * Called when the activity is first created.
     */

//	private ListView mListView;
    private ArrayList<deviceListItem> list;
    private Button sendButton;
    private Button disconnectButton;
    private EditText editMsgView;
    private double lon;
    private double lat;
    private double wd;
    private double qy;
    private double[] Times = new double[50240];//50240
    private int count = 0;
    public Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
            if (msg.what == 1) {
                //	list.add(new deviceListItem((String)msg.obj, true));
                String c = (String) msg.obj;
                char tempchar[] = c.toCharArray();
                char[] v1 = new char[20];
                char[] v2 = new char[20];
                char[] v3 = new char[20];
                char[] v4 = new char[20];
                int i = 0;
                int k = 0;
                int flag = 1;
                for (i = 1; ; i++) {
                    if (tempchar[i] == 'b') {
                        if (i == 1) {
                            v1[k++] = '0';
                            flag = 0;
                        }
                        break;
                    }
                    v1[k++] = tempchar[i];
                }
                k = 0;
                i++;
                for (; ; i++) {
                    if (tempchar[i] == 'c') {
                        if (flag == 1)
                            v2[k++] = '0';
                        break;
                    }
                    v2[k++] = tempchar[i];
                }
                k = 0;
                i++;
                for (; tempchar[i] != 'd'; i++) {
                    v3[k++] = tempchar[i];
                }
                k = 0;
                i++;
                for (; tempchar[i] != 'E'; i++) {
                    v4[k++] = tempchar[i];
                }
                k = 0;
                i++;
                String temps1 = new String(v1);
                TextView tv1 = (TextView) findViewById(R.id.textView2);
                if (flag == 1)
                    tv1.setText(temps1);
                else
                    tv1.setText("Eorr");

                String temps2 = new String(v2);
                TextView tv2 = (TextView) findViewById(R.id.textView4);
                if (flag == 1)
                    tv2.setText(temps2);
                else
                    tv2.setText("Eorr");


                final String temps3 = new String(v3);
                TextView tv3 = (TextView) findViewById(R.id.textView6);
                tv3.setText(temps3 + "  *0.1deg c");

                final String temps4 = new String(v4);
                TextView tv4 = (TextView) findViewById(R.id.textView8);
                tv4.setText(temps4 + "  Pa");


                if (flag == 1) {

                    lon = Double.valueOf(temps1).doubleValue();
                    lat = Double.valueOf(temps2).doubleValue();
                } else {
                    lon = 0;
                    lat = 0;
                }

                wd = Double.valueOf(temps3).doubleValue();
                Times[count] = wd;
                count++;
//				 qy=Double.valueOf(temps4).doubleValue();


                findViewById(R.id.google).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Intent intentSimle = new Intent();
                        intentSimle.setClass(chatActivity.this, MapsActivity.class);
                        Bundle bundleSimple = new Bundle();
                        bundleSimple.putDouble("lon", lon);
                        bundleSimple.putDouble("lat", lat);
                        bundleSimple.putString("wd", temps3);
                        bundleSimple.putString("qy", temps4);
                        intentSimle.putExtras(bundleSimple);
                        startActivity(intentSimle);
                    }

                });
                //cavers

                findViewById(R.id.cavers).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intentSimle = new Intent();
                        intentSimle.setClass(chatActivity.this, XYChartBuilder.class);
                        Bundle bundleSimple = new Bundle();
                        bundleSimple.putDoubleArray("data", Times);
                        bundleSimple.putInt("Times", count);
                        intentSimle.putExtras(bundleSimple);
                        startActivity(intentSimle);

                    }

                });
            } else {
//				list.add(new deviceListItem((String)msg.obj, false));
            }
            mAdapter.notifyDataSetChanged();
//			mListView.setSelection(list.size() - 1);
        }

    };
    private BluetoothServerSocket mserverSocket = null;
    private ServerThread startServerThread = null;
    private clientThread clientConnectThread = null;
    private BluetoothSocket socket = null;
    private BluetoothDevice device = null;
    private readThread mreadThread = null;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        mContext = this;
        init();
    }

    private void init() {
        list = new ArrayList<deviceListItem>();
        mAdapter = new deviceListAdapter(this, list);
//		mListView = (ListView) findViewById(R.id.list);
//		mListView.setAdapter(mAdapter);
//		mListView.setOnItemClickListener(this);
//		mListView.setFastScrollEnabled(true);


        disconnectButton = (Button) findViewById(R.id.btn_disconnect);
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (Bluetooth.serviceOrCilent == ServerOrCilent.CILENT) {
                    shutdownClient();
                } else if (Bluetooth.serviceOrCilent == ServerOrCilent.SERVICE) {
                    shutdownServer();
                }
                Bluetooth.isOpen = false;
                Bluetooth.serviceOrCilent = ServerOrCilent.NONE;
                Toast.makeText(mContext, "已断开连接！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (Bluetooth.isOpen) {
            Toast.makeText(mContext, "连接已经打开，可以通信。如果要再建立连接，请先断开！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Bluetooth.serviceOrCilent == ServerOrCilent.CILENT) {
            String address = Bluetooth.BlueToothAddress;
            if (!address.equals("null")) {
                device = mBluetoothAdapter.getRemoteDevice(address);
                clientConnectThread = new clientThread();
                clientConnectThread.start();
                Bluetooth.isOpen = true;
            } else {
                Toast.makeText(mContext, "address is null !", Toast.LENGTH_SHORT).show();
            }
        } else if (Bluetooth.serviceOrCilent == ServerOrCilent.SERVICE) {
            startServerThread = new ServerThread();
            startServerThread.start();
            Bluetooth.isOpen = true;
        }
    }

    /* 停止服务器 */
    private void shutdownServer() {
        new Thread() {
            public void run() {
                if (startServerThread != null) {
                    startServerThread.interrupt();
                    startServerThread = null;
                }
                if (mreadThread != null) {
                    mreadThread.interrupt();
                    mreadThread = null;
                }
                try {
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                    if (mserverSocket != null) {
                        mserverSocket.close();/* 关闭服务器 */
                        mserverSocket = null;
                    }
                } catch (IOException e) {
                    Log.e("server", "mserverSocket.close()", e);
                }
            }
        }.start();
    }

    /* 停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            public void run() {
                if (clientConnectThread != null) {
                    clientConnectThread.interrupt();
                    clientConnectThread = null;
                }
                if (mreadThread != null) {
                    mreadThread.interrupt();
                    mreadThread = null;
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    socket = null;
                }
            }
        }.start();
    }

    //发送数据
    private void sendMessageHandle(String msg) {
        if (socket == null) {
            Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = socket.getOutputStream();
            os.write(msg.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		list.add(new deviceListItem(msg, false));
        mAdapter.notifyDataSetChanged();
//		mListView.setSelection(list.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Bluetooth.serviceOrCilent == ServerOrCilent.CILENT) {
            shutdownClient();
        } else if (Bluetooth.serviceOrCilent == ServerOrCilent.SERVICE) {
            shutdownServer();
        }
        Bluetooth.isOpen = false;
        Bluetooth.serviceOrCilent = ServerOrCilent.NONE;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
    }

    //开启客户端
    private class clientThread extends Thread {
        public void run() {
            try {
                //创建一个Socket连接：只需要服务器在注册时的UUID号
                // socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                //连接
                Message msg2 = new Message();
                msg2.obj = "请稍候，正在连接服务器:" + Bluetooth.BlueToothAddress;
                msg2.what = 0;
                LinkDetectedHandler.sendMessage(msg2);

                socket.connect();

                Message msg = new Message();
                msg.obj = "已经连接上服务端！可以发送信息。";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);
                //启动接受数据
                mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                Log.e("connect", "", e);
                Message msg = new Message();
                msg.obj = "连接服务端异常！断开连接重新试一试。";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);
            }
        }
    }

    //开启服务器
    private class ServerThread extends Thread {
        public void run() {

            try {
                /* 创建一个蓝牙服务器
                 * 参数分别：服务器名称、UUID	 */
                mserverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                Log.d("server", "wait cilent connect...");

                Message msg = new Message();
                msg.obj = "请稍候，正在等待客户端的连接...";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);

				/* 接受客户端的连接请求 */
                socket = mserverSocket.accept();
                Log.d("server", "accept success !");

                Message msg2 = new Message();
                String info = "客户端已经连接上！可以发送信息。";
                msg2.obj = info;
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg2);
                //启动接受数据
                mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //读取数据
    private class readThread extends Thread {
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = socket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        int k = 0;
                        for (int i = 0; i < bytes; i++) {
                            //新的修改位置
                            if (buffer[i] == 'F' && buffer[i + 1] == 'F')
                                continue;
                            buf_data[k++] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        if (buffer[0] == 'F' && buffer[bytes - 1] == 'E') {
                            msg.obj = s;
                            msg.what = 1;
                            LinkDetectedHandler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public class SiriListItem {
        String message;
        boolean isSiri;

        public SiriListItem(String msg, boolean siri) {
            message = msg;
            isSiri = siri;
        }
    }

    public class deviceListItem {
        String message;
        boolean isSiri;

        public deviceListItem(String msg, boolean siri) {
            message = msg;
            isSiri = siri;
        }
    }
}

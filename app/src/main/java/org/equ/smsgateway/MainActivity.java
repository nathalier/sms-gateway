package org.equ.smsgateway;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiManager;


public class MainActivity extends Activity {
    private static final String SEND_SMS_INTENT = "org.equ.send_sms";
    private static final String HTTPD_SERVER_TAG = "Httpd";
    private static final int PORT = 6717;
    private final IntentFilter intentFilter = new IntentFilter(SEND_SMS_INTENT);
    private final QueuePositionReceiver receiver = new QueuePositionReceiver();
    private String mySimNum;

    private LocalBroadcastManager mBroadcastMgr;  //TEMP
    private WebServer server;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        mBroadcastMgr = LocalBroadcastManager                   //TEMP
                .getInstance(context);
        mBroadcastMgr.registerReceiver(receiver, intentFilter); //TEMP
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);     //TEMP
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBroadcastMgr.sendBroadcast(new Intent(SEND_SMS_INTENT));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // registerReceiver(receiver, intentFilter);  //TODO

        //getting this phone SIMnumber - NOT USED
/*        TelephonyManager telephMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mySimNum = telephMng.getLine1Number();
        if (mySimNum == null) mySimNum = telephMng.getSimSerialNumber();*/

        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);

        // Getting WiFi device IP
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        // Getting device IP
        /*final String formatedIpAddress = "0000";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        formatedIpAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }*/

        textIpaddr.setText("Please access http://" + formatedIpAddress + ":" + PORT);

        server = new WebServer(context, PORT);
        try {
            server.start();
        }
        catch(IOException ioe) {
            Log.w(HTTPD_SERVER_TAG, "The server could not start.");
        }
        Log.w(HTTPD_SERVER_TAG, "Web server initialized.");
    }

    @Override
    protected void onDestroy() {
        mBroadcastMgr.unregisterReceiver(receiver);
        if (server != null) server.stop();
        super.onDestroy();
    }

}

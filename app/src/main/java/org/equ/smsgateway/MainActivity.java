package org.equ.smsgateway;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.net.wifi.WifiManager;


public class MainActivity extends Activity {
    private static final String HTTPD_SERVER_TAG = "Httpd";
    private static final int THIS_PHONE_PORT = 6717;

    private WebServer server;
    private Context context;
    private PowerManager.WakeLock wakeLock;
    private ComponentName receiverComp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        /*mBroadcastMgr = LocalBroadcastManager                   //TEMP
                .getInstance(context);
        mBroadcastMgr.registerReceiver(receiver, intentFilter); //TEMP*/

        receiverComp = new ComponentName(context, SmsReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiverComp,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

        if (savedInstanceState == null) {
            TelephonyManager telephMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mySimNum = telephMng.getLine1Number();
            if (mySimNum != null && !mySimNum.equals(""))
                Globals.thisPhoneNum = mySimNum;
        }

        final EditText mySimNumET = (EditText) findViewById(R.id.editTextThisTelephNum);
//        TextView mySimNumTV = (TextView) findViewById(R.id.textViewThisSimNumValue);
        mySimNumET.setText(Globals.thisPhoneNum);
        mySimNumET.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                Globals.thisPhoneNum = mySimNumET.getText().toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        // Getting WiFi device IP
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
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

        textIpaddr.setText("Please access http://" + formatedIpAddress + ":" + THIS_PHONE_PORT);

        final EditText regServerHost = (EditText) findViewById(R.id.editTextRegServerHostValue);
        regServerHost.setText(Globals.regServerHost);
        regServerHost.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                    Globals.regServerHost = regServerHost.getText().toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        final EditText regServerPort = (EditText) findViewById(R.id.editTextRegServerPortValue);
        regServerPort.setText(Globals.regServerPort);
        regServerPort.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                Globals.regServerPort = regServerPort.getText().toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (server == null) {
            server = new WebServer(context, THIS_PHONE_PORT);
            try {
                server.start();
            } catch (IOException e) {
                Log.w(HTTPD_SERVER_TAG, "The server could not start." + e);
            }
            Log.w(HTTPD_SERVER_TAG, "Web server initialized.");
            // registerReceiver(receiver, intentFilter);  //TODO

            //getting this phone SIMnumber
        }

    }

    @Override
    protected void onDestroy() {
//        mBroadcastMgr.unregisterReceiver(receiver);
        if (server != null) server.stop();
        wakeLock.release();
        context.getPackageManager().setComponentEnabledSetting(receiverComp,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


}

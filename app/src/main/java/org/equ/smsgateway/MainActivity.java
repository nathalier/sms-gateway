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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.wifi.WifiManager;


public class MainActivity extends Activity {
//    private static final String SEND_SMS_INTENT = "org.equ.send_sms";
    private static final String HTTPD_SERVER_TAG = "Httpd";
    private static final int PORT = 6717;

    private LocalBroadcastManager mBroadcastMgr;  //TEMP
    private WebServer server;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        /*mBroadcastMgr = LocalBroadcastManager                   //TEMP
                .getInstance(context);
        mBroadcastMgr.registerReceiver(receiver, intentFilter); //TEMP


        Button button = (Button) findViewById(R.id.button);     //TEMP
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBroadcastMgr.sendBroadcast(new Intent(SEND_SMS_INTENT));
            }
        });*/
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
    protected void onResume() {
        super.onResume();
        // registerReceiver(receiver, intentFilter);  //TODO

        //getting this phone SIMnumber

    }

    @Override
    protected void onDestroy() {
//        mBroadcastMgr.unregisterReceiver(receiver);
        if (server != null) server.stop();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO:
        // Save state information with a collection of key-value pairs
        // 4 lines of code, one for every count variable
        savedInstanceState.putString("simNum", Globals.thisPhoneNum);

    }

}

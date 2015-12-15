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
    private static final String PKGMNG_TAG = "PkgMng";
    private static final int THIS_PHONE_PORT = 6717;

    private WebServer server;
    private Context context;
    private PowerManager.WakeLock wakeLock;
    private ComponentName receiverComp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        prevent phone from sleep and web-server stop
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

//        setting UI elements
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

//        enabling smsRececiver BroadcastReceiver
        receiverComp = new ComponentName(context, SmsReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiverComp,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

//        getting the phone number
        if (savedInstanceState == null) {
            TelephonyManager telephMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mySimNum = telephMng.getLine1Number();
            if (mySimNum != null && !mySimNum.equals(""))
                Globals.thisPhoneNum = mySimNum;
        }

//        setting listener for changing the phone number when it's defined incorrectly
        final EditText mySimNumET = (EditText) findViewById(R.id.editTextThisTelephNum);
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

//        defining current Wi-Fi IP-address and displaying it in ipaddr TextView
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);

        // Getting WiFi device IP
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

//        displaying WiFI IP-address in ipaddr TextView
        textIpaddr.setText("Please access http://" + formatedIpAddress + ":" + THIS_PHONE_PORT);

//        setting listener for chnaging Registration server host
        final EditText regServerHost = (EditText) findViewById(R.id.editTextRegServerHostValue);
        regServerHost.setText(Globals.regServerHost);
        regServerHost.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                    Globals.regServerHost = regServerHost.getText().toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        //        setting listener for chnaging Registration server port
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

//        Starting the webserver on the phone to get requests from Registration Server
        if (server == null) {
            server = new WebServer(context, THIS_PHONE_PORT);
            try {
                server.start();
            } catch (IOException e) {
                Log.w(HTTPD_SERVER_TAG, "The server could not start." + e);
            }
            Log.w(HTTPD_SERVER_TAG, "Web server initialized.");
        }

    }

    @Override
    protected void onDestroy() {
//        stop Web-server
        if (server != null) server.stop();

//        allow phone to sleep
        wakeLock.release();

//        disabling smsReceiver BroadcastReceiver
        try {
        context.getPackageManager().setComponentEnabledSetting(receiverComp,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            Log.w(PKGMNG_TAG, e);
        }

        super.onDestroy();
    }


}

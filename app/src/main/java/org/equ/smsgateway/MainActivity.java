package org.equ.smsgateway;



import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View.OnClickListener;
import android.widget.Button;

//import org.equ.myapplication.R;

public class MainActivity extends Activity {
    private static final String SEND_SMS_INTENT = "org.equ.aggr.send_sms";
    private final IntentFilter intentFilter = new IntentFilter(SEND_SMS_INTENT);
    private final QueuePositionReceiver receiver = new QueuePositionReceiver();

    private LocalBroadcastManager mBroadcastMgr;  //TEMP
    private WebServer server;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadcastMgr = LocalBroadcastManager                   //TEMP
                .getInstance(getApplicationContext());
        mBroadcastMgr.registerReceiver(receiver, intentFilter); //TEMP
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);     //TEMP
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBroadcastMgr.sendBroadcast(new Intent(SEND_SMS_INTENT));
            }
        });

        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (server != null) server.stop();
        super.onDestroy();
    }
}

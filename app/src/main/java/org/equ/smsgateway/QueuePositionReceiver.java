package org.equ.smsgateway;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class QueuePositionReceiver extends BroadcastReceiver{
    private final String TAG = "Receiver";
    SmsManager sms;
    PendingIntent pi;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "SEND SMS INTENT RECEIVED");

        String msg = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
        PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(msg), 0);

        sms = SmsManager.getDefault();
        sms.sendTextMessage("5556", null, "This is sample test message", piSent, null);
        Toast.makeText(context, "SMS_SENT", Toast.LENGTH_LONG).show();

    }
}

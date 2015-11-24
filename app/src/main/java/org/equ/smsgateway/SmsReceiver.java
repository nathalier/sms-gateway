package org.equ.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsMessage;
import android.os.Bundle;



public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "INTENT RECEIVED by SmsReceiver");

        Toast.makeText(context, "SMS RECEIVED by SmsReceiver", Toast.LENGTH_LONG).show();

        Bundle bundle = intent.getExtras();
        SmsMessage[] receivedMsgs = retrieveMsgs(bundle);

        for (SmsMessage msg: receivedMsgs) {
            RegAttemptsHandler postThread;
            if (validate(msg)) {
                postThread = new RegAttemptsHandler("postThread");
                Runnable task = new RegAttempts(context, msg);
                postThread.start();
                postThread.prepareHandler();
                postThread.postTask(task);
            }
        }
    }

    private SmsMessage[] retrieveMsgs (Bundle bundle){
        SmsMessage[] receivedMsgs = null;
        if (bundle != null)
        {
            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                receivedMsgs = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++)
                    receivedMsgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);//, FORMAT_3GPP);
            } catch (Exception e) {
                Log.i(TAG, "SMS reading exception: " + e.toString());
            }
        }
        return receivedMsgs;
    }

    private boolean validate(SmsMessage msg){
        if (msg!= null
                && msg.getOriginatingAddress() != null
                && msg.getOriginatingAddress().matches("^\\+?[0-9]+$"))
//                && msg.getMessageBody().matches("[0-9]+"))
            return true;
        return false;
    }

}
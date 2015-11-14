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
    private final String FORMAT_3GPP = "3gpp";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "INTENT RECEIVED by SmsReceiver");

        Toast.makeText(context, "SMS RECEIVED by SmsReceiver", Toast.LENGTH_LONG).show();

        Bundle bundle = intent.getExtras();
        SmsMessage[] receivedMsgs = retrieveMsgs(bundle);

        for (SmsMessage msg: receivedMsgs) {
            if (validate(msg)) {
                HttpPostRequest postReq = new HttpPostRequest(msg);
                postReq.execute();
            }
        }

    }

    private SmsMessage[] retrieveMsgs (Bundle bundle){
        SmsMessage[] receivedMsgs = null;
        if (bundle != null)
        {

            Object[] pdus = (Object[]) bundle.get("pdus");
            receivedMsgs = new SmsMessage[pdus.length];
            for (int i=0; i< pdus.length; i++)
                receivedMsgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);//, FORMAT_3GPP);
        }
        return receivedMsgs;
    }

    private boolean validate(SmsMessage msg){
        if (msg!= null && msg.getOriginatingAddress() != null)
//                && msg.getMessageBody().matches("[0-9]+"))
            return true;
        return false;
    }

}
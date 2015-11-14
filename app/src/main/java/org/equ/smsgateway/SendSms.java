package org.equ.smsgateway;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SendSms {

    public SendSms(Context context, String text, String userNum) {
        //send sms

        String intent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
        PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(intent), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(userNum, null, text, piSent, null);
    }
}

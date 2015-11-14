package org.equ.smsgateway;

import android.content.Context;
import android.telephony.SmsMessage;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

public class RegAttempts implements Runnable {
    private final int TRIES_LIMIT = 30;
    private final int TRIES_INTERVAL = 10000;
    private final int STATUS_OK = 200;
    private final int REQ_TIMEOUT = 5000;
    private final String FAIL_TO_POST = "Queue is not accessible";
    private final String TAG = "POST";
    private final String PAID_CONST = "1";
    Boolean success = false;
    Context context;
    SmsMessage msg;
    int tries;
    boolean withError = false;

    public RegAttempts(Context context, SmsMessage msg) {
        this.msg = msg;
        this.tries = 1;
        this.context = context;
    }

    @Override
    public void run() {
        String origAddr = msg.getOriginatingAddress();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://" + Globals.regServerHost + ":"
                + Globals.regServerPort);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("num", origAddr));
        params.add(new BasicNameValuePair("sn", Globals.thisPhoneNum));
        params.add(new BasicNameValuePair("num", Integer.toString(msg.getIndexOnIcc())));
        params.add(new BasicNameValuePair("msg", msg.getMessageBody()));
        params.add(new BasicNameValuePair("paid", PAID_CONST));
        request.getParams().setParameter("http.connection.timeout", REQ_TIMEOUT);
        request.getParams().setParameter("http.socket.timeout", REQ_TIMEOUT);
        try {
            UrlEncodedFormEntity encoded = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            request.setEntity(encoded);
            Log.i(TAG, "Formed");
        }
        catch (Exception e) {
            Log.i(TAG, "POST exception: " + e.toString());
            withError = true;
        }
        if (!withError)
            while (!success && tries <= TRIES_LIMIT) {
                try {
                    HttpResponse responsePOST = httpClient.execute(request);
                    Log.i(TAG, "Sent. Response: " + responsePOST.getStatusLine().getStatusCode());

                    if (responsePOST.getStatusLine().getStatusCode() == STATUS_OK) {
                        success = true;
                        new SendSms(context, responsePOST.getEntity().getContent().toString(), origAddr);
                        Log.i(TAG, "RESPONSE on Try #: " + tries++ + "was successful");
                    } else {
                        Log.i(TAG, "Try #: " + tries++ + "failed");
                    }
                }
                catch (Exception e) {
                    tries++;
                    Log.i(TAG, "POST exception: " + e.toString());
                }
            }

        if ((withError || !success) && (origAddr != Globals.thisPhoneNum)
                 && msg.getMessageBody() != FAIL_TO_POST)
            new SendSms(context, FAIL_TO_POST, origAddr);
    }
}


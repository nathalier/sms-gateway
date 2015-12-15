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
    private final int TRIES_LIMIT = 20;
    private final int TRIES_INTERVAL = 10000; // ms
    private final int STATUS_OK = 200;
    private final int REQ_TIMEOUT = 5000; // ms
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
//        gets sender cell num
        String origAddr = msg.getOriginatingAddress();

//        POST-request construction
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
//       iterated request sending until success or tries limit
        if (!withError)
            while (!success && tries <= TRIES_LIMIT) {
                try {
                    HttpResponse responsePOST = httpClient.execute(request);
                    Log.i(TAG, "Sent. Response: " + responsePOST.getStatusLine().getStatusCode());

                    if (responsePOST.getStatusLine().getStatusCode() == STATUS_OK) {
                        success = true;
                        Log.i(TAG, "RESPONSE on Try #: " + tries++ + " was successful");
                        String responseText = responsePOST.getEntity().getContent().toString();

//                        sends sms to client if OK-response contains text for him
                        if (!responseText.equals("")) {
                            new SendSms(context, responseText, origAddr);
                            Log.i(TAG, "SMS with response was sent");
                        }
                    } else {
                        Log.i(TAG, "Try #: " + tries++ + " failed");
                    }
                }
                catch (Exception e) {
                    tries++;
                    Log.i(TAG, "POST exception: " + e.toString());
                }
            }

        if ((withError || !success) &&
                // further expression could be useful for testing purpose.
                // It prevents sending FAIL_TO_POST sms to itself,
                // but does not protect 2-sim device with another server response
                // from infinite sms sending and receiving:-)
                !origAddr.equals(Globals.thisPhoneNum) &&
                !msg.getMessageBody().equals(FAIL_TO_POST)) {
            //  sends FAIL_TO_POST sms to client
            new SendSms(context, FAIL_TO_POST, origAddr);
            Log.i(TAG, "FAIL_TO_QUEUE SMS was sent");
        }
    }
}


package org.equ.smsgateway;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;


import org.apache.http.impl.client.BasicResponseHandler;

import java.util.List;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.util.Log;


public class HttpPostRequest extends AsyncTask<Void, Void, Boolean> {
    private SmsMessage msg;
    private int tries;
    private final int TRIES_LIMIT = 5;
    private final int TRIES_INTERVAL = 1000;
    private final int STATUS_OK = 200;
    private final String TAG = "POST Request";
    private final  int REQ_TIMEOUT = 3000;
    private boolean success = false;

    public HttpPostRequest (SmsMessage msg, int tries) {
        this.msg = msg;
        this.tries = tries;
    }

    public HttpPostRequest (SmsMessage msg) {
        this.msg = msg;
        this.tries = 0;
    }

    @Override
    protected Boolean doInBackground(Void... noth){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://" + Globals.regServerHost + ":"
                                                      + Globals.regServerPort);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("num", msg.getOriginatingAddress()));
            params.add(new BasicNameValuePair("sn", Globals.thisPhoneNum));
            params.add(new BasicNameValuePair("num", Integer.toString(msg.getIndexOnIcc())));
            params.add(new BasicNameValuePair("msg", msg.getMessageBody()));
            request.getParams().setParameter("http.connection.timeout", REQ_TIMEOUT);
            request.getParams().setParameter("http.socket.timeout", REQ_TIMEOUT);
            UrlEncodedFormEntity encoded = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            request.setEntity(encoded);
            Log.i(TAG, "Formed");

            HttpResponse responsePOST = httpClient.execute(request);
            Log.i(TAG, "Sent. Response: " + responsePOST.getStatusLine().getStatusCode());

            if (responsePOST.getStatusLine().getStatusCode() == STATUS_OK)  {
                success = true;
                Log.i(TAG, "RESPONSE successful");
            }
        }
        catch (Exception e) {
            Log.i(TAG, "POST exception: " + e.toString());
        }

        if (!success && tries < TRIES_LIMIT) {
            Log.i(TAG, "Try #: " + tries);
//            Handler handler = new Handler();
//            handler.postDelayed(new RegRetry(msg, ++tries), TRIES_INTERVAL);
        }
        return success;
    }

}

package org.equ.smsgateway;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;


import org.apache.http.impl.client.BasicResponseHandler;

import java.util.List;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.telephony.SmsMessage;
import android.util.Log;


public class HttpPostRequest extends AsyncTask<Void, Void, Void> {
    private SmsMessage msg;
    private final String TAG = "POST Request";
    private final String HOST = "localhost";
    private final String PORT = "4444";
    private final String THIS_SIM_NUM = "+380734757601";

    public HttpPostRequest (SmsMessage msg) { this.msg = msg; }


    @Override
    protected Void doInBackground(Void... noth){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://" + HOST + ":" + PORT);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("num", msg.getOriginatingAddress()));
            params.add(new BasicNameValuePair("sn", THIS_SIM_NUM));
            params.add(new BasicNameValuePair("num", Integer.toString(msg.getIndexOnIcc())));
            params.add(new BasicNameValuePair("msg", msg.getMessageBody()));
            UrlEncodedFormEntity encoded = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            request.setEntity(encoded);
            Log.i(TAG, "Formed");
            HttpResponse responsePOST = httpClient.execute(request);
            Log.i(TAG, "Sent");

            HttpEntity resEntity = responsePOST.getEntity();
            if (resEntity != null) {
                //TODO proceed response here
                Log.i(TAG, "RESPONSE" + EntityUtils.toString(resEntity));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

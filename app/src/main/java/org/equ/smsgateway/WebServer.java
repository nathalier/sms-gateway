package org.equ.smsgateway;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;
import java.util.logging.Logger;


public class WebServer extends NanoHTTPD{

    private static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private Context context;

    public WebServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        WebServer.LOG.info(method + " '" + uri + "' ");


        Map<String, String> params = session.getParms();
        if (params != null && params.get("num") != null && !params.get("msg").equals("")
                && params.get("auth").equals("lifelikeadance")) {
            //send sms
            String userNum = params.get("num");
            String userNotif = params.get("msg");
            String intent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
            PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(intent), 0);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(userNum, null, userNotif, piSent, null);
            String msg = "accepted";
            return new Response(Response.Status.OK, MIME_HTML, msg);
        } else {
            String msg = "<html><body>Incorrect parameters provided!</body></html>";
            return new Response(Response.Status.BAD_REQUEST, MIME_HTML, msg);
        }
    }
}

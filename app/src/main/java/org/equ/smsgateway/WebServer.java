package org.equ.smsgateway;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.app.Activity;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;
import java.util.logging.Logger;
//import java.util.Properties;


public class WebServer extends NanoHTTPD{

    private static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private static final String SEND_SMS_INTENT = "org.equ.send_sms";
    LocalBroadcastManager mBroadcastMgr;
    Context context;

    public WebServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        WebServer.LOG.info(method + " '" + uri + "' ");

        String msg = "<html><body><h1>Hello server</h1>\n";
        mBroadcastMgr = LocalBroadcastManager                   //TEMP
                .getInstance(context);
        mBroadcastMgr.sendBroadcast(new Intent(SEND_SMS_INTENT));

        /*Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n" + "  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }*/

        msg += "</body></html>\n";

        return new Response(Response.Status.OK, MIME_HTML, msg);
    }
}

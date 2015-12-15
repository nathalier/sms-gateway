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
//        processing received request

//        POST or GET methods should be used
//        Request parameters must contain:
//        - 'num' with correct cell number
//        - not empty 'msg'
//        - 'auth' with predefined value ('lifelikeadance')

        Method method = session.getMethod();
        String uri = session.getUri();
        WebServer.LOG.info(method + " '" + uri + "' ");
        String badReqMsg = "<html><body>Use GET or POST method</body></html>";

        if (Method.GET.equals(method) || Method.POST.equals(method)) {
            Map<String, String> params = session.getParms();
            if (params != null && params.get("num") != null &&
                    params.get("num").matches("^\\+?[0-9]+$") && !params.get("msg").equals("")
                    && params.get("auth").equals("lifelikeadance")) {
                new SendSms(this.context, params.get("msg"), params.get("num"));
                String resp = "accepted";
                return new Response(Response.Status.OK, MIME_HTML, resp);
            } else {
                badReqMsg = "<html><body>Incorrect parameters provided</body></html>";
            }
        }
        return new Response(Response.Status.BAD_REQUEST, MIME_HTML, badReqMsg);
    }
}

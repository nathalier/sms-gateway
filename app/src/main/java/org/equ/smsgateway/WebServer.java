package org.equ.smsgateway;

import fi.iki.elonen.NanoHTTPD;


public class WebServer extends NanoHTTPD{

    public WebServer()
    {
        super(8080);
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files) {
        String answer = "";
        try {
            // Open file from SD Card
            File root = Environment.getExternalStorageDirectory();
            FileReader index = new FileReader(root.getAbsolutePath() +
                    "/www/index.html");
            BufferedReader reader = new BufferedReader(index);
            String line = "";
            while ((line = reader.readLine()) != null) {
                answer += line;
            }
            reader.close();

        } catch(IOException ioe) {
            Log.w("Httpd", ioe.toString());
        }


        return new NanoHTTPD.Response(answer);
    }
}

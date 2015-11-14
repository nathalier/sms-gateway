package org.equ.smsgateway;

import android.os.Handler;
import android.os.HandlerThread;


public class RegAttemptsHandler extends HandlerThread {
    private Handler wHandler;

    public RegAttemptsHandler(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        wHandler.post(task);
    }

    public void prepareHandler(){
        wHandler = new Handler(getLooper());
    }

}

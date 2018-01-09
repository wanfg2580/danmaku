package com.jimmy.barrage.handler;

import com.jimmy.barrage.bean.Request;
import com.jimmy.barrage.util.Socket;
import org.apache.log4j.Logger;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.handler
 * @date 18-1-9
 */
public class KeepAlive implements Runnable {
    private Socket socket;
    private Logger logger = Logger.getLogger(KeepAlive.class);

    public KeepAlive(Socket socket) {
        this.socket = socket;
    }
    @Override public void run() {
        this.socket.sendData(Request.keepLive(System.currentTimeMillis() / 1000L));
    }
}

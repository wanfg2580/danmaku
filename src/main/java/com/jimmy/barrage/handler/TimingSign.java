package com.jimmy.barrage.handler;

import com.jimmy.barrage.bean.Request;
import com.jimmy.barrage.util.Socket;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.TimerTask;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.handler
 * @date 18-1-9
 */
public class TimingSign implements Runnable{
    private Socket socket;
    private Logger logger = Logger.getLogger(TimerTask.class);

    public TimingSign(Socket socket) {
        this.socket = socket;
    }

    @Override public void run() {
        logger.info("发送签到请求");
        this.socket.sendData(Request.buildMessage("#签到 " + new Date()));
    }
}

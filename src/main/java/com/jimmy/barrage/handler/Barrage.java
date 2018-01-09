package com.jimmy.barrage.handler;

import com.jimmy.barrage.bean.Request;
import com.jimmy.barrage.util.MD5Util;
import com.jimmy.barrage.util.Socket;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.bean
 * @date 18-1-9
 */
public class Barrage {
    private Socket socket;
    private Socket socketAuth;
    private KeepAlive keepAlive;
    private TimingSign timingSign;
    private ReceiveData receiveData;
    private  ScheduledExecutorService scheduExec;

    private String username = "";
    private String roomId = "74751";
    private String ltkid = "";
    private String stk = "";


    public Barrage(String host, int port, String authHost, int authPort) {
        socket = new Socket(host, port);
        socketAuth = new Socket(authHost, authPort);
        keepAlive = new KeepAlive(socket);
        timingSign = new TimingSign(socketAuth);
        receiveData = new ReceiveData(socketAuth);

        scheduExec = new ScheduledThreadPoolExecutor(2);
    }

    public void sendMessage(String message) {
        socketAuth.sendData(Request.buildMessage(message));
    }

    private void keepAlive() {
        scheduExec.scheduleAtFixedRate(keepAlive, 0, 40000, TimeUnit.MILLISECONDS);
    }

    private void receiveMessage() {
        Thread thread = new Thread(receiveData);
        thread.setName("DanmuServerReceiveThread");
        thread.start();
    }

    public void getBarrage() {
        login();
        receiveMessage();
        socketAuth.sendData(Request.joinGroup(roomId));
        keepAlive();
    }

    public void sign() {
//        long oneDay = 24 * 60 * 60 * 1000;
//        long initDelay  = getTimeMillis("20:00:00") - System.currentTimeMillis();
//        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        scheduExec.scheduleAtFixedRate(timingSign, 0, 40000, TimeUnit.MILLISECONDS);
    }

    public void login() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String vk = MD5Util.encrypt(timestamp + "7oE9nPEG9xXV69phU31FYCLUagKeYtsF" + uuid);
        socketAuth.sendData(Request.login(username, roomId, uuid, timestamp, vk, ltkid, stk));
    }

    public void close() {
        socket.closeSocket();
        socketAuth.closeSocket();
    }
}

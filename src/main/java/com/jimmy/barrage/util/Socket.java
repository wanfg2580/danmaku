package com.jimmy.barrage.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.util
 * @date 18-1-9
 */
public class Socket {
    private InetAddress host;
    private int port;
    private java.net.Socket socket;
    private DouyuProtocolMessage douyuProtocolMessage;
    private static Logger logger = Logger.getLogger(Socket.class);

    public Socket(String server, int port) {
        try {
            this.host = InetAddress.getByName(server);
            this.port = port;
            logger.info("Connect to Server " + host.getHostAddress() + ":" + port);
            this.socket = new java.net.Socket(this.host, this.port);
            logger.info("Open Socket successfully");
        } catch (IOException e) {
            logger.info("Open socket fail");
            logger.info(e.getMessage());
        }
        douyuProtocolMessage = new DouyuProtocolMessage();
    }

    public java.net.Socket getSocket() {
        return socket;
    }

    public DouyuProtocolMessage getDouyuProtocolMessage() {
        return douyuProtocolMessage;
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    public void sendData(String content) {
        byte[] messageContent = null;
        try {
            messageContent = douyuProtocolMessage.sendMessageContent(content);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(messageContent);
            logger.info(content);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }
}

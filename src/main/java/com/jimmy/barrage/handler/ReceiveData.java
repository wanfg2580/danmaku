package com.jimmy.barrage.handler;

import com.jimmy.barrage.util.Socket;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.handler
 * @date 18-1-9
 */
public class ReceiveData implements Runnable {
    private Socket socket;
    private Logger logger = Logger.getLogger(ReceiveData.class);

    public ReceiveData(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                InputStream inputStream = socket.getSocket().getInputStream();

                byte[] msg = new byte[10240];
                int line = 0;
                line = inputStream.read(msg);
                byteOutput.write(msg, 0, line);
                byte[] receiveMsg = byteOutput.toByteArray();
                socket.getDouyuProtocolMessage().receivedMessageContent(receiveMsg);
            } catch (IOException e) {
                logger.info("Receive IO error!");
                logger.info(e.getMessage());
            }
        }
    }
}

package com.jimmy.barrage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.handler
 * @date 18-1-9
 */
public class DouyuProtocolMessage {
    private int[] messageLength;
    private int[] code;
    private int[] end;
    private ByteArrayOutputStream byteArrayOutputStream;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public DouyuProtocolMessage() {
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public byte[] sendMessageContent(String content) throws IOException {
        this.messageLength = new int[] {calcMessageLength(content), 0x00, 0x00, 0x00};
        this.code = new int[] {0xb1, 0x02, 0x00, 0x00};
        this.end = new int[] {0x00};

        byteArrayOutputStream.reset();
        for (int i : messageLength) {
            byteArrayOutputStream.write(i);
        }
        for (int i : messageLength) {
            byteArrayOutputStream.write(i);
        }
        for (int i : code) {
            byteArrayOutputStream.write(i);
        }
        byteArrayOutputStream.write(content.getBytes("UTF-8"));
        for (int i : end) {
            byteArrayOutputStream.write(i);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Be careful about the length of content, because Chinese's char is not 1 length,
     * so you should encode it first.
     *
     * @param content
     * @return
     * @throws UnsupportedEncodingException
     */
    private int calcMessageLength(String content) throws UnsupportedEncodingException {
        return 4 + 4 + (content == null ? 0 : content.getBytes("UTF-8").length) + 1;
    }

    public void receivedMessageContent(byte[] receiveMsg) {
        // Copy from stackoverflow
        String message = bytesToHex(receiveMsg);
        System.out.println(message);

        // Get first "/"
        int slashIndex = message.indexOf("2F") / 2;
        String messageType = new String();
        for (int i = 18; i < slashIndex; i++) {
            messageType += (char) receiveMsg[i];
        }

        // Determine type of message
        if (messageType.equals("chatmsg")) {
            // "/nn@="
            int nicknameIndex = message.indexOf("2F6E6E403D") / 2;
            // "/txt@="
            int textIndex = message.indexOf("2F747874403D") / 2;
            // "/cid@="
            int textEndIndex = message.indexOf("2F636964403D") / 2;

            String nickname = changeToChinese(receiveMsg, nicknameIndex, textIndex, 5);
            String decodedNickname = decodeMessage(nickname);
            String text = changeToChinese(receiveMsg, textIndex, textEndIndex, 6);
            String decodedText = decodeMessage(text);
            System.out.println(decodedNickname + ": " + decodedText);
        }
    }

    /**
     * Change text into Chinese if need
     *
     * @param receiveMsg
     * @param indexStart
     * @param indexEnd
     * @param num
     * @return
     */
    private String changeToChinese(byte[] receiveMsg, int indexStart, int indexEnd, int num) {
        String text = new String();
        for (int i = indexStart + num; i < indexEnd; i++) {
            if (receiveMsg[i] < 32 || receiveMsg[i] > 126) {
                try {
                    text += "%" + Integer.toHexString((receiveMsg[i] & 0x000000FF) | 0xFFFFFF00)
                        .substring(6);
                } catch (StringIndexOutOfBoundsException e) {
                    //                    logger.info("String index out of range. receiveMsg: {}", receiveMsg[i]);
                    //                    logger.info(e.getMessage());
                }
            } else {
                text += (char) receiveMsg[i];
            }
        }
        return text;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String encodeMessage(String message) {
        message = encode(message);
        return message;
    }

    /**
     * @param message
     * @return
     * @TODO: if '%' is in message, decode will fail
     */
    private String decodeMessage(String message) {
        String decodedMessage = message;
        try {
            decodedMessage = URLDecoder.decode(message, "utf-8");
        } catch (UnsupportedEncodingException e) {
            //            logger.info("Decode error! message: {}", message);
            //            logger.info(e.getMessage());
        }
        decodedMessage = decode(decodedMessage);
        return decodedMessage;
    }

    public static String encode(String str) {
        return str.replace("@", "@A").replace("/", "@S");
    }

    public String decode(String str) {
        return str.replace("@A", "@").replace("@S", "/");
    }
}

package com.jimmy.barrage.util;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String REGEX_CHAT_DANMAKU = "type@=chatmsg/.*rid@=(\\d*?)/.*uid@=(\\d*).*nn@=(.*?)/txt@=(.*?)/(.*)/";

    private Logger logger = Logger.getLogger(DouyuProtocolMessage.class);

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

    private Matcher getMatcher(String content, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        return pattern.matcher(content);
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
//        String message = bytesToHex(receiveMsg);
//
//        // Get first "/"
//        int slashIndex = message.indexOf("2F") / 2;
//        String messageType = new String();
//        for (int i = 18; i < slashIndex; i++) {
//            messageType += (char) receiveMsg[i];
//        }
//
//        // Determine type of message
//        if (messageType.equals("chatmsg")) {
//            // "/nn@="
//            int nicknameIndex = message.indexOf("2F6E6E403D") / 2;
//            // "/txt@="
//            int textIndex = message.indexOf("2F747874403D") / 2;
//            // "/cid@="
//            int textEndIndex = message.indexOf("2F636964403D") / 2;
//
//            String nickname = changeToChinese(receiveMsg, nicknameIndex, textIndex, 5);
//            String decodedNickname = decodeMessage(nickname);
//            String text = changeToChinese(receiveMsg, textIndex, textEndIndex, 6);
//            String decodedText = decodeMessage(text);
//            logger.info(decodedNickname + ": " + decodedText);
//        }

        List<String> responses = splitResponse(receiveMsg);

        for (String response : responses) {
            if (!response.contains("chatmsg")) {
                continue;
            }

            if (response == null) {
                continue;
            }

            Matcher matcher = getMatcher(response, REGEX_CHAT_DANMAKU);

            if (matcher.find()) {
                logger.info(matcher.group(3) + " : " + matcher.group(4));
            }
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
            logger.info("Decode error! message: " + message);
            logger.info(e.getMessage());
        }
        decodedMessage = decode(decodedMessage);
        return decodedMessage;
    }

    /**
     * 分离同时返回的多组数据
     * 不优雅的方法：
     *      1.先将字节数组转化为对应的十六进制字符串
     *      2.然后用斗鱼定义的请求码"b2020000"来分割字符串
     *      3.判断"00"为消息尾部
     *      4.遍历分离出多组Response
     */
    public static List<String> splitResponse(byte[] buffer) {
        if (buffer == null || buffer.length <= 0) {
            return null;
        }

        List<String> resList = new ArrayList<>();
        String byteArray = HexUtil.bytes2HexString(buffer).toLowerCase();

        String[] responseStrings = byteArray.split("b2020000");
        int end;
        for (int i = 1; i < responseStrings.length; i++) {
            if (!responseStrings[i].contains("00")) {
                continue;
            }
            end = responseStrings[i].indexOf("00");
            byte[] bytes = HexUtil.hexString2Bytes(responseStrings[i].substring(0, end));
            if (bytes != null) {
                resList.add(new String(bytes));
            }
        }

        return resList;
    }

    public static String encode(String str) {
        return str.replace("@", "@A").replace("/", "@S");
    }

    public String decode(String str) {
        return str.replace("@A", "@").replace("@S", "/");
    }
}

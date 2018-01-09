package com.jimmy.barrage.bean;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage.bean 消息格式
 * @date 18-1-9
 */
public class Request {
    public static String login(String username, String roomId, String uuid, String timestamp,
        String vk, String ltkid, String stk) {
        return String.format(
            "type@=loginreq/username@=%s/ct@=0/password@=/roomid@=%s/devid@=%s/rt@=%s/vk@=%s/ver@=20150929/aver@=2017073111/ltkid@=%s/biz@=1/stk@=%s/",
            username, roomId, uuid, timestamp, vk, ltkid, stk);
    }

    public static String joinGroup(String roomId) {
        return String.format("type@=joingroup/rid@=%s/gid@=-9999/", roomId);
    }

    public static String keepLive(long unixTime) {
        return String.format("type@=keeplive/tick@=%d/", unixTime);
    }

    public static String buildMessage(String message) {
        message = message.replace("@", "@A").replace("/", "@S");
        return String.format(
            "type@=chatmessage/receiver@=0/content@=%s/scope@=/col@=0/pid@=/p2p@=0/nc@=0/rev@=0/ifs@=0/",
            message);
    }
}

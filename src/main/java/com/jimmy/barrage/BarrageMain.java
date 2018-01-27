package com.jimmy.barrage;

import com.jimmy.barrage.handler.Barrage;

/**
 * Copyright (C), 2018
 *
 * @author jimmy
 * @desc com.jimmy.barrage
 * @date 18-1-8
 */
public class BarrageMain {
    public static void main(String[] args) {
        Barrage barrage = new Barrage("openbarrage.douyutv.com", 8601,"119.90.49.89", 8092);
        barrage.sendMessage("test222");
        barrage.close();
    }
}

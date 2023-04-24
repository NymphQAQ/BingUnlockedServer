package com.zqj.websocketclient.bing;

import java.util.Random;

/**
 * @author Rebecca
 * @since 2023/4/19 14:25
 */
public class BingUtil {


    //随机16进制字符串
    public static String genRanHex(int size) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < size) {
            sb.append(Integer.toHexString(rand.nextInt(16)));
        }
        return sb.toString();
    }
}

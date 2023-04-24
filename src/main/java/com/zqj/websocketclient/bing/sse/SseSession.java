package com.zqj.websocketclient.bing.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rebecca
 * @since 2023/4/19 21:44
 */

@Slf4j
public class SseSession {

    private static final Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();

    public static void add(String sessionKey,SseEmitter sseEmitter){
        if(sessionMap.get(sessionKey) != null){
            log.info("用户已存在!");
        }
        sessionMap.put(sessionKey, sseEmitter);
    }

    public static boolean exists(String sessionKey){
        return sessionMap.get(sessionKey) != null;
    }

    public static boolean remove(String sessionKey){
        SseEmitter sseEmitter = sessionMap.get(sessionKey);
        if(sseEmitter != null){
            sseEmitter.complete();
        }
        return false;
    }

    public static void onError(String sessionKey,Throwable throwable){
        SseEmitter sseEmitter = sessionMap.get(sessionKey);
        if(sseEmitter != null){
            sseEmitter.completeWithError(throwable);
        }
    }

    public static void send(String sessionKey,String content) throws IOException {
        sessionMap.get(sessionKey).send(content);
    }

}

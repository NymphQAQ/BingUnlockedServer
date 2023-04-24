package com.zqj.websocketclient.bing.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Rebecca
 * @since 2023/4/19 21:47
 */
public interface SseServer {

    public SseEmitter connect(String userId);

    public boolean send(String userId, String content);

    public boolean close(String userId);


}

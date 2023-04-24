package com.zqj.websocketclient.bing.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * @author Rebecca
 * @since 2023/4/19 21:48
 */

@Service
@Slf4j
public class SseServerImpl implements SseServer{



    @Override
    public SseEmitter connect(String userId) {
        if(SseSession.exists(userId)){
            SseSession.remove(userId);
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onError((err)-> {
            log.error("type: SseSession Error, msg: {} session Id : {}",err.getMessage(), userId);
            SseSession.onError(userId, err);
        });

        sseEmitter.onTimeout(() -> {
            log.info("type: SseSession Timeout, session Id : {}", userId);
            SseSession.remove(userId);
        });

        sseEmitter.onCompletion(() -> {
            log.info("type: SseSession Completion, session Id : {}", userId);
            SseSession.remove(userId);
        });
        SseSession.add(userId, sseEmitter);
        return sseEmitter;

    }

    @Override
    public boolean send(String userId, String content) {
        if(SseSession.exists(userId)){
            try{
                SseSession.send(userId, content);
                return true;
            }catch(IOException exception){
                log.error("type: SseSession send Erorr:IOException, msg: {} session Id : {}",exception.getMessage(), userId);
            }
        }else{
            log.info("User Id " + userId + " not Found");
        }
        return false;


    }

    @Override
    public boolean close(String userId) {
        log.info("type: SseSession Close, session Id : {}", userId);
        return SseSession.remove(userId);
    }
}

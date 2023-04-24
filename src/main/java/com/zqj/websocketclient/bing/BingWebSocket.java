package com.zqj.websocketclient.bing;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.*;

import java.util.concurrent.CountDownLatch;

/**
 * @author Rebecca
 * @since 2023/4/11 23:28
 */
@Slf4j
@RequiredArgsConstructor
public class BingWebSocket implements WebSocketHandler {


    public CountDownLatch countDownLatchHandshake = new CountDownLatch(1);
    public CountDownLatch countDownLatchResult = new CountDownLatch(1);
    public SseEmitter sseEmitter;
    private WebSocketSession session;
    private String tempText = "";


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        session.sendMessage(new TextMessage("{\"protocol\":\"json\",\"version\":1}"));
        countDownLatchHandshake.countDown();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理文本消息
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getPayload();
//        log.info("收到文本消息：" + payload);
        String[] objects = payload.split("");
        String result = objects[0];
        //如果接收到"{}",立马发送ping
        if (result.equals("{}")) {
            log.info("ping........");
            session.sendMessage(new TextMessage("{\"type\":6}"));
        } else {
            try {

                JSONObject resultObj = JSONUtil.parseObj(result);
                if (resultObj.get("type").equals(1)) {
                    //字符串操作
                    String arguments = resultObj.get("arguments").toString();
                    //去掉第一个"["和最后一个"]"
                    String substring = arguments.substring(1, arguments.length() - 1);
                    //转成对象
                    JSONObject entries = JSONUtil.parseObj(substring);
                    //如果包含"messages"
                    if (entries.containsKey("messages")) {
                        String messages = entries.get("messages").toString();
                        //去掉第一个"["和最后一个"]"
                        String substring1 = messages.substring(1, messages.length() - 1);
                        //最终结果
                        String text = JSONUtil.parseObj(substring1).get("text").toString();

                        String data = text.substring(tempText.length());
                        String replace = data.replace("\n", "\\n");
                        //向前端发送消息
                        sseEmitter.send(replace);
                        log.info(replace);

                        tempText = text;

                    }
                } else if (resultObj.get("type").equals(2)) {
                    countDownLatchResult.countDown();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        //如果收到ping,马上回应
        if (result.equals("{\"type\":6}")) {
            session.sendMessage(new TextMessage("{\"type\":6}"));
            log.info("发送ping");
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sseEmitter.complete();
        log.info("服务端关闭了连接");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendMessage(String message) throws Exception {
        log.info("发送信息........");
        session.sendMessage(new TextMessage(message));
    }
}

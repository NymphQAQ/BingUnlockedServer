package com.zqj.websocketclient.bing;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zqj.websocketclient.bing.pojo.BingChatDB;
import com.zqj.websocketclient.bing.pojo.ResultOne;
import com.zqj.websocketclient.bing.pojo.SendEntity;
import com.zqj.websocketclient.bing.service.BingService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.*;

import java.util.concurrent.CountDownLatch;

/**
 * @author Rebecca
 * @since 2023/4/11 23:28
 */
@Slf4j
public class BingWebSocket implements WebSocketHandler {


    /**
     * 与WebSocket握手
     */
    public CountDownLatch countDownLatchHandshake = new CountDownLatch(1);
    /**
     * 接收完毕
     */
    public CountDownLatch countDownLatchResult = new CountDownLatch(1);
    public SseEmitter sseEmitter;
    public BingService bingService;
    public String id;
    public String destination;
    private WebSocketSession session;
    private String tempText = "";
    private String result;



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
        result = objects[0];
        //如果接收到"{}",立马发送ping
        if (result.equals("{}") || result.equals("{\"type\":6}")) {
            log.info("ping........");
            session.sendMessage(new TextMessage("{\"type\":6}"));
        } else {
            try {
                ResultOne resultOne = JSONUtil.toBean(result, ResultOne.class);
                if (resultOne.getType().equals(1)) {
                    if (resultOne.getArguments().get(0).getMessages() != null) {
                        //拿到消息内容
                        String text = resultOne.getArguments().get(0).getMessages().get(0).getText();
                        //截取反交集的内容
                        String data = text.substring(tempText.length());
                        //增加反斜杠
                        String replace = data.replace("\n", "\\n").replace(" ", "  ");
                        //向前端发送消息
                        sseEmitter.send(replace);
                        log.info(replace);

                        tempText = text;
                    }
                }else if (resultOne.getType().equals(2)){
                    countDownLatchResult.countDown();
                }
//                log.info(result);

            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        //有可能服务端主动关
        countDownLatchResult.countDown();
        log.info("关闭了WebSocket连接");
        if (StrUtil.isNotBlank(destination)) {
            //准备更新数据
            BingChatDB bingChatDB = new BingChatDB();
            bingChatDB.setId(this.id)
                    .setDestination(destination +
                            "\\n\\n[assistant](#message)\\n" +
                            tempText)
                    .setResultMessage(result);
            bingService.saveOrUpdate(bingChatDB);
            log.info("结果消息存入数据库成功!");
        }

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendMessage(SendEntity message) throws Exception {
        log.info("发送信息........");
        //转为JSON
        String messageJson = JSONUtil.toJsonStr(message);
        //发送消息
        session.sendMessage(new TextMessage(messageJson));
        BingChatDB bingChatDB = new BingChatDB();
        bingChatDB.setId(this.id)
                .setDestination(message.getArguments().get(0).getPreviousMessages().get(0).getDescription())
                .setSendMessage(messageJson);
        //保存到数据库中
        bingService.saveOrUpdate(bingChatDB);
        log.info("发送消息存入数据库成功!");
        log.info(messageJson);
    }


}

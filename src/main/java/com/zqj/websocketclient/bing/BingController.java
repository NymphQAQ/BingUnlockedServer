package com.zqj.websocketclient.bing;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.zqj.websocketclient.bing.pojo.ClientMessage;
import com.zqj.websocketclient.bing.pojo.CreateBingResult;
import com.zqj.websocketclient.bing.pojo.SendEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Rebecca
 * @since 2023/4/18 14:36
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class BingController {

//    private final BingWebSocket bingWebSocket;
//    private final StandardWebSocketClient socketClient;
    private final BingConfig bingConfig;

    private final HttpHeaders headers;

    @PostMapping(value = "/create",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> create(@RequestBody ClientMessage message) throws Exception {


        BingWebSocket bingWebSocket = new BingWebSocket();
        //超时时间设置2分钟
        SseEmitter sseEmitter = new SseEmitter((long)(1000*60*2));
        bingWebSocket.sseEmitter = sseEmitter;

        String uri = "wss://sydney.bing.com/sydney/ChatHub";
        StandardWebSocketClient socketClient = new StandardWebSocketClient();
        WebSocketConnectionManager manager = new WebSocketConnectionManager(socketClient, bingWebSocket, uri);

        String urlStr = "https://www.bing.com/turing/conversation/create";
        URLConnection conn;
        if (StrUtil.isNotBlank(bingConfig.getProxy_address()) && bingConfig.getProxy_port() != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(bingConfig.getProxy_address(), bingConfig.getProxy_port()));
            conn = new URL(urlStr).openConnection(proxy);
        }else {
            conn = new URL(urlStr).openConnection();
        }


        // 设置请求头
        conn.setRequestProperty("accept", "application/json");
        conn.setRequestProperty("accept-language", "en-US,en;q=0.9");
        conn.setRequestProperty("content-type", "application/json");
        conn.setRequestProperty("sec-ch-ua",
                "\"Chromium\";v=\"112\", \"Microsoft Edge\";v=\"112\", \"Not:A-Brand\";v=\"99\"");
        conn.setRequestProperty("sec-ch-ua-arch", "\"x86\"");
        conn.setRequestProperty("sec-ch-ua-bitness", "\"64\"");
        conn.setRequestProperty("sec-ch-ua-full-version", "\"112.0.1722.7\"");
        conn.setRequestProperty("sec-ch-ua-full-version-list",
                "\"Chromium\";v=\"112.0.5615.20\", \"Microsoft Edge\";v=\"112.0.1722.7\", \"Not:A-Brand\";v=\"99.0.0.0\"");
        conn.setRequestProperty("sec-ch-ua-mobile", "?0");
        conn.setRequestProperty("sec-ch-ua-model", "\"\"");
        conn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        conn.setRequestProperty("sec-ch-ua-platform-version", "\"15.0.0\"");
        conn.setRequestProperty("sec-fetch-dest", "empty");
        conn.setRequestProperty("sec-fetch-mode", "cors");
        conn.setRequestProperty("sec-fetch-site", "same-origin");
        conn.setRequestProperty("x-ms-useragent",
                "azsdk-js-api-client-factory/1.0.0-beta.1 core-rest-pipeline/1.10.0 OS/Win32");
        conn.setRequestProperty("Referer", "https://www.bing.com/search?q=Bing+AI&showconv=1&FORM=hpcodx");
        conn.setRequestProperty("Referrer-Policy", "origin-when-cross-origin");
        conn.setRequestProperty("x-forwarded-for", "1.1.1.1");
        conn.setRequestProperty("x-ms-client-request-id", UUID.randomUUID().toString());
        conn.setRequestProperty("cookie", "_U=" + bingConfig.getCookie());

        try {
            manager.start();
            conn.connect();
            String result = new String(conn.getInputStream().readAllBytes());
            log.debug("请求成功,响应为: " + result);
            if (result.length() < 5) {
                throw new RuntimeException("返回结果长度不对,你的IP可能已被Bing封禁: " + result);
            }
            //将请求转为Java对象
            CreateBingResult createBingResult = JSONUtil.toBean(result, CreateBingResult.class);
            log.info(createBingResult.toString());

            //创建实际发送实体类
            SendEntity sendEntity = new SendEntity();
            SendEntity.Arguments arguments = sendEntity.getArguments().get(0);
            arguments.setConversationSignature(createBingResult.getConversationSignature())
                    .setConversationId(createBingResult.getConversationId())
                    .getParticipant().setId(createBingResult.getClientId());

            arguments.getPreviousMessages().get(0).setDescription(message.getMessage());

            //等待握手结束
            bingWebSocket.countDownLatchHandshake.await();
            //向Bing发送消息
            bingWebSocket.sendMessage(JSONUtil.toJsonStr(sendEntity));

            CompletableFuture.runAsync(() -> {
                try {
                    bingWebSocket.countDownLatchResult.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    sseEmitter.complete();
                    manager.stop();
                }
            });
            return new ResponseEntity<>(sseEmitter,headers,HttpStatus.OK);

        } catch (Exception e) {
            log.info("请求失败,响应为: " + e.getMessage());
            sseEmitter.send(e.getMessage());
            sseEmitter.complete();
            manager.stop();
            return new ResponseEntity<>(sseEmitter,headers,HttpStatus.OK);
        }

    }

}

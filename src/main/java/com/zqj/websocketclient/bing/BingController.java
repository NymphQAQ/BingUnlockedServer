package com.zqj.websocketclient.bing;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.zqj.websocketclient.bing.config.BingConfig;
import com.zqj.websocketclient.bing.pojo.*;
import com.zqj.websocketclient.bing.service.BingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Rebecca
 * @since 2023/4/18 14:36
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class BingController {

    private final BingConfig bingConfig;

    private final HttpHeaders headers;

    private final BingService bingService;

    @PostMapping(value = "/create",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> create(@RequestBody ClientMessage message) throws Exception {

        //每次进来都new一个实例,免得多线程乱套
        BingWebSocket bingWebSocket = new BingWebSocket();
        //超时时间设置2分钟,超过时间自动关闭连接
        SseEmitter sseEmitter = new SseEmitter((long)(1000*60*2));
        //让WebSocket服务也能操作这个对象,默认浅拷贝还是挺好的
        bingWebSocket.sseEmitter = sseEmitter;
        //传个参进去
        bingWebSocket.bingService = bingService;
        bingWebSocket.id = message.getId();

        String uri = "wss://sydney.bing.com/sydney/ChatHub";
        StandardWebSocketClient socketClient = new StandardWebSocketClient();
        MyWebSocketConnectionManager manager = new MyWebSocketConnectionManager(socketClient, bingWebSocket, uri);

        String urlStr = "https://www.bing.com/turing/conversation/create";
        URLConnection conn;
        //有代理就配置一下代理,没有就不配
        if (StrUtil.isNotBlank(bingConfig.getProxy_address()) && bingConfig.getProxy_port() != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(bingConfig.getProxy_address(), bingConfig.getProxy_port()));
            conn = new URL(urlStr).openConnection(proxy);
        }else {
            conn = new URL(urlStr).openConnection();
        }


        // 设置请求头,我也不知道为什么要设置这么多请求头
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
        conn.setRequestProperty("cookie", "SRCHHPGUSR=" + bingConfig.getCookie());

        try {
            //每次都要开启新的连接,这样才能突破20条限制
            //启动WebSocket
            CompletableFuture.runAsync(manager::start);
            //连接create请求,获取一些参数
            conn.connect();
            String result = new String(conn.getInputStream().readAllBytes());
            log.debug("请求成功,响应为: " + result);
            //一般不会
            if (result.length() < 5) {
                throw new RuntimeException("返回结果长度不对,你的IP可能已被Bing封禁: " + result);
            }
            //将请求转为Java对象
            CreateBingResult createBingResult = JSONUtil.toBean(result, CreateBingResult.class);
            log.info(createBingResult.toString());

            //创建实际发送实体类,Java搞JSON是真麻烦
            SendEntity sendEntity = new SendEntity();
            SendEntity.Arguments arguments = sendEntity.getArguments().get(0);
            arguments.setConversationSignature(createBingResult.getConversationSignature())
                    .setConversationId(createBingResult.getConversationId())
                    .getParticipant().setId(createBingResult.getClientId());


            String destination;
            //如果是第一次对话
            if (message.getIsFirst()) {
                destination = "[system](#additional_instructions)- " +
                        message.getSystemMessage() +
                        "[user](#message)" +
                        message.getMessage();
            }else {
                //拿到以前的聊天记录
                BingChatDB bingChatDB = bingService.getById(message.getId());
                destination = bingChatDB.getDestination() +
                        "[user](#message)" +
                        message.getMessage();
            }
            arguments.getPreviousMessages().get(0).setDescription(destination);

            //等待握手结束
            bingWebSocket.countDownLatchHandshake.await();
            //向Bing发送消息
            bingWebSocket.sendMessage(sendEntity);
            //传个参,以免查询数据库
            bingWebSocket.destination = destination;

            //开个线程玩,结束连接用
            CompletableFuture.runAsync(() -> {
                try {
                    bingWebSocket.countDownLatchResult.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    sseEmitter.complete();
                    manager.stop();
                }
            });
            //放心,返回了并不会结束连接的
            return new ResponseEntity<>(sseEmitter,headers,HttpStatus.OK);

        } catch (Exception e) {
            log.info("请求失败,响应为: " + e.getMessage());
            sseEmitter.send(e.getMessage());
            sseEmitter.complete();
            manager.stop();
            return new ResponseEntity<>(sseEmitter,headers,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * 获取当前会话的聊天记录
     * @return 对话列表
     */
    @GetMapping("/getChat")
    public List<?> getChat(String id) {
        BingChatDB bingChatDB = bingService.getById(id);
        String[] split = bingChatDB.getDestination().split("");
        //用来记录循环次数
        AtomicLong atomicLong = new AtomicLong();

        return Arrays.stream(split).map(item -> {
            WebChatRec webChatRec = new WebChatRec();
            if (item.contains("[system](#additional_instructions)")) {
                webChatRec.setId(atomicLong.incrementAndGet())
                        .setIsMe(true)
                        .setContent(item.replace("\n","</br>"));
            } else if (item.contains("[user](#message)")) {
                webChatRec.setId(atomicLong.incrementAndGet())
                        .setIsMe(true)
                        .setContent(item.substring(16).replace("\n","</br>"));
            } else if (item.contains("[assistant](#message)")) {
                webChatRec.setId(atomicLong.incrementAndGet())
                        .setIsMe(false)
                        .setContent(item.substring(21).replace("\n","</br>"));
            }
            return webChatRec;
        }).toList();
    }

    @GetMapping("/getSessionList")
    public List<String> getSessionList() {
        return bingService.lambdaQuery().list().stream().map(BingChatDB::getId).toList();
    }

}

package com.zqj.websocketclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebsocketClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketClientApplication.class, args);

//        WebSocketClient client = new BingConfig().webSocketClient();
//        MyHandler handler = new MyHandler();
//        List<String> subProtocols = Arrays.asList("my-protocol");
//        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
//        headers.setSecWebSocketProtocol(subProtocols);
//        Session session = client.c(new URI("ws://websocket.example.com/myHandler"), headers, handler).get();

//        String uri = "wss://sydney.bing.com/sydney/ChatHub";
//        StandardWebSocketClient client = new StandardWebSocketClient();
//        WebSocketHandler handler = new MyHandler();
//        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, handler, uri);
//        manager.start();
    }

}

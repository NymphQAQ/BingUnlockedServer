package com.zqj.websocketclient.bing;

import org.springframework.context.Lifecycle;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.ConnectionManagerSupport;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Rebecca
 * @since 2023/4/24 23:48
 * 原来的连接失败不抛异常就很难受
 */

public class MyWebSocketConnectionManager extends ConnectionManagerSupport {

    private final WebSocketClient client;
    private final WebSocketHandler webSocketHandler;
    @Nullable
    private WebSocketSession webSocketSession;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public MyWebSocketConnectionManager(WebSocketClient client, WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {
        super(uriTemplate, uriVariables);
        this.client = client;
        this.webSocketHandler = this.decorateWebSocketHandler(webSocketHandler);
    }

    public MyWebSocketConnectionManager(WebSocketClient client, WebSocketHandler webSocketHandler, URI uri) {
        super(uri);
        this.client = client;
        this.webSocketHandler = this.decorateWebSocketHandler(webSocketHandler);
    }

    public void setSubProtocols(List<String> protocols) {
        this.headers.setSecWebSocketProtocol(protocols);
    }

    public List<String> getSubProtocols() {
        return this.headers.getSecWebSocketProtocol();
    }

    public void setOrigin(@Nullable String origin) {
        this.headers.setOrigin(origin);
    }

    @Nullable
    public String getOrigin() {
        return this.headers.getOrigin();
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }


    public void startInternal() {
        if (this.client instanceof Lifecycle lifecycle) {
            if (!lifecycle.isRunning()) {
                lifecycle.start();
            }
        }

        super.startInternal();
    }

    public void stopInternal() throws Exception {
        if (this.client instanceof Lifecycle lifecycle) {
            if (lifecycle.isRunning()) {
                lifecycle.stop();
            }
        }

        super.stopInternal();
    }

    public boolean isConnected() {
        return this.webSocketSession != null && this.webSocketSession.isOpen();
    }

    protected void openConnection() {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("连接WebSocket至: " + this.getUri());
        }

        CompletableFuture<WebSocketSession> future = this.client.execute(this.webSocketHandler, this.headers, this.getUri());
        future.whenComplete((result, ex) -> {
            if (result != null) {
                this.webSocketSession = result;
                this.logger.info("成功连接WebSocket!");
            } else if (ex != null) {
                this.logger.error("连接WebSocket失败:", ex);
                throw new RuntimeException("连接WebSocket失败:", ex);
            }

        });
    }

    protected void closeConnection() throws Exception {
        if (this.webSocketSession != null) {
            this.webSocketSession.close();
        }

    }

    protected WebSocketHandler decorateWebSocketHandler(WebSocketHandler handler) {
        return new LoggingWebSocketHandlerDecorator(handler);
    }
}

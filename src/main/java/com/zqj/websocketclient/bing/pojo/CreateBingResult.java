package com.zqj.websocketclient.bing.pojo;

import lombok.Data;

/**
 * @author Rebecca
 * @since 2023/4/18 17:42
 */
@Data
public class CreateBingResult {

    private String conversationId;
    private String clientId;
    private String conversationSignature;
    private Result result;

    @Data
    public static class Result {
        private String value;
        private String message;
    }

}


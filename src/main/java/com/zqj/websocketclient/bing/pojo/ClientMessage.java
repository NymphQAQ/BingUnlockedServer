package com.zqj.websocketclient.bing.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Rebecca
 * @since 2023/4/21 16:22
 */
@Data
@Accessors(chain = true)
public class ClientMessage {
    private String id;
    private Boolean isFirst;
    private String message;
    private String systemMessage;
}

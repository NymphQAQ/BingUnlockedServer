package com.zqj.websocketclient.bing.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Rebecca
 * @since 2023/4/25 19:34
 */
@Data
@Accessors(chain = true)
public class WebChatRec {
    private Long id;
    private String content;
    private Boolean isMe;
}

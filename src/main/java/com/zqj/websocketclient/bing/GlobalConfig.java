package com.zqj.websocketclient.bing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * @author Rebecca
 * @since 2023/4/21 11:31
 */
@Configuration
public class GlobalConfig {

    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8"));
        return headers;
    }
}

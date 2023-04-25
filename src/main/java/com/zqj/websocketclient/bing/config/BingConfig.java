package com.zqj.websocketclient.bing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rebecca
 * @since 2023/4/11 23:31
 */

@Configuration
@ConfigurationProperties(prefix = "bingconfig")
@Data
public class BingConfig {

    /**
     * 代理地址
     */
    private String proxy_address;
    /**
     * 代理端口
     */
    private Integer proxy_port;
    /**
     * cookie
     */
    private String cookie;


}

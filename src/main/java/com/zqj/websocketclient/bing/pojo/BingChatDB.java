package com.zqj.websocketclient.bing.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Rebecca
 * @since 2023/4/25 11:33
 */

@Data
@Accessors(chain = true)
@TableName("bing_chat")
public class BingChatDB implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    @TableField("destination")
    private String destination;

    @TableField("send_message")
    private String sendMessage;

    @TableField("result_message")
    private String resultMessage;

    @TableField("created_time")
    private String createdTime;

    @TableField("updated_time")
    private String updatedTime;

}

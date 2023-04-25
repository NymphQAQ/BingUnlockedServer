package com.zqj.websocketclient.bing.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zqj.websocketclient.bing.pojo.BingChatDB;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Rebecca
 * @since 2023/4/25 11:47
 */
@Mapper
public interface BingDao extends BaseMapper<BingChatDB> {
}

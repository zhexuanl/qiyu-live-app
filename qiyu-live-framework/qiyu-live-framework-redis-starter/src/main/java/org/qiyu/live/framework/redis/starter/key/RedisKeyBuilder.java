package org.qiyu.live.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Value;

public class RedisKeyBuilder {

    private static final String SPLIT_ITEM = ":";
    @Value("${spring.application.name}")
    private String applicationName;

    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    public String getPrefix() {
        return applicationName + SPLIT_ITEM;
    }
}


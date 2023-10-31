package org.qiyu.live.framework.redis.starter.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RedisUtils {

    public static int createRandomExpireTime() {
        int time = ThreadLocalRandom.current().nextInt(1000);
        return time + 60 * 30;
    }
}

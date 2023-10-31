package org.qiyu.live.framework.redis.starter.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


public class RedisKeyLoadMatch implements Condition {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisKeyLoadMatch.class);
    private static final String PREFIX = "qiyulive";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String appName = context.getEnvironment().getProperty("spring.application.name");
        if (appName == null) {
            LOGGER.error("RedisKeyBuilder object could not be loaded because no application name was matched");
            return false;
        }

        try {
            Field classNameField = metadata.getClass().getDeclaredField("className");
            classNameField.setAccessible(true);
            String keyBuilderName = (String) classNameField.get(metadata);
            List<String> splitList = Arrays.asList(keyBuilderName.split("\\."));
            // Ignore case, use qiyulive to start the name
            String classSimpleName = PREFIX + splitList.get(splitList.size() - 1).toLowerCase();
            boolean matchStatus = classSimpleName.contains(appName.replaceAll("-", ""));
            LOGGER.info("keyBuilderClass is {}, match status is {}", keyBuilderName, matchStatus);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}

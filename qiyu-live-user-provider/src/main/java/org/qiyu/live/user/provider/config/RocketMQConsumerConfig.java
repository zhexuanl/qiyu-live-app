package org.qiyu.live.user.provider.config;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.user.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RocketMQConsumerConfig implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    // alternative way is using @PostConstruct instead of implementing interface
    @Override
    public void afterPropertiesSet() throws Exception {
        initConsumer();
    }


    public void initConsumer() {
        try {
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
            defaultMQPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameServer());
            defaultMQPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + RocketMQConsumerConfig.class.getSimpleName());
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            try {
                defaultMQPushConsumer.subscribe("user-update-cache", "*");
            } catch (MQClientException e) {
                throw new RuntimeException(e);
            }
            defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
                String msgStr = new String(list.get(0).getBody());
                UserDTO userDTO = JSON.parseObject(msgStr, UserDTO.class);
                if (userDTO == null || userDTO.getUserId() == null) {
                    LOGGER.error("User Id null, error: {} ", msgStr);

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()));
                LOGGER.info("Delay Delete userId, {}", userDTO.getUserId());

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            defaultMQPushConsumer.start();
            LOGGER.info("Start MQ consumer, name server: {}", rocketMQConsumerProperties.getNameServer());
        } catch (MQClientException e) {
            LOGGER.info("Error to start MQ consumer, {}", e.getErrorMessage());
        }
    }
}

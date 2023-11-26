package org.qiyu.live.user.provider.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.user.dto.UserDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RocketMQConsumerConfig implements InitializingBean {

    private final RocketMQConsumerProperties rocketMQConsumerProperties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    private final ObjectMapper objectMapper;

    // alternative way is using @PostConstruct instead of implementing interface
    @Override
    public void afterPropertiesSet() {
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
                UserDTO userDTO = null;
                try {
                    userDTO = objectMapper.readValue(msgStr, UserDTO.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if (userDTO == null || userDTO.getUserId() == null) {
                    log.error("User Id null, error: {} ", msgStr);

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()));
                log.info("Delay Delete userId, {}", userDTO.getUserId());

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            defaultMQPushConsumer.start();
            log.info("Start MQ consumer, name server: {}", rocketMQConsumerProperties.getNameServer());
        } catch (MQClientException e) {
            log.info("Error to start MQ consumer, {}", e.getErrorMessage());
        }
    }
}

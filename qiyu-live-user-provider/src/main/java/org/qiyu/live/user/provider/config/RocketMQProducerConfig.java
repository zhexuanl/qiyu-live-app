package org.qiyu.live.user.provider.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RocketMQProducerConfig {

    private final RocketMQProducerProperties producerProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MQProducer mqProducer() {
        ThreadPoolExecutor asyncThreadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setName(applicationName + ":rmq-producer" + ThreadLocalRandom.current().ints(1000).toString());
            return thread;
        });

        return getMqProducer(asyncThreadPoolExecutor);
    }

    private DefaultMQProducer getMqProducer(ThreadPoolExecutor asyncThreadPoolExecutor) {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        try {
            defaultMQProducer.setProducerGroup(producerProperties.getGroupName());
            defaultMQProducer.setNamesrvAddr(producerProperties.getNameServer());
            defaultMQProducer.setRetryTimesWhenSendFailed(producerProperties.getRetryTimes());
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(producerProperties.getRetryTimes());
            defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
            //set async send thread pool
            defaultMQProducer.setAsyncSenderExecutor(asyncThreadPoolExecutor);
            defaultMQProducer.start();
            log.info("Start MQ producer, name server address: {}", defaultMQProducer.getNamesrvAddr());
        } catch (MQClientException e) {
            log.info("Error to start MQ producer, {}", e.getErrorMessage());
        }
        return defaultMQProducer;
    }
}

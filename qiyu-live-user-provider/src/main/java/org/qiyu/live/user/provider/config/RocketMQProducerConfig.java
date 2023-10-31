package org.qiyu.live.user.provider.config;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class RocketMQProducerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQProducerProperties producerProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MQProducer mqProducer() {
        ThreadPoolExecutor asyncThreadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(applicationName + ":rmq-producer" + ThreadLocalRandom.current().ints(1000).toString());
                return thread;
            }
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
            LOGGER.info("Start MQ producer, name server address: {}", defaultMQProducer.getNamesrvAddr());
        } catch (MQClientException e) {
            LOGGER.info("Error to start MQ producer, {}", e.getErrorMessage());
        }
        return defaultMQProducer;
    }
}

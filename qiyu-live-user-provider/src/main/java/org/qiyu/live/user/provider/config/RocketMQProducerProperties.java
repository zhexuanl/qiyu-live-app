package org.qiyu.live.user.provider.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "rocketmq.producer")
@Configuration
public class RocketMQProducerProperties {

    private String nameServer;
    private String groupName;
    private int retryTimes;
    private int sendTimeout;


}

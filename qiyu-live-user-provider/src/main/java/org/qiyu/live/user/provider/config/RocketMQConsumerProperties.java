package org.qiyu.live.user.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq.consumer")
public class RocketMQConsumerProperties {

    private String nameServer;
    private String groupName;

}

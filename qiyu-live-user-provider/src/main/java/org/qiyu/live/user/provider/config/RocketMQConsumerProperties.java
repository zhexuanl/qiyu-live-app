package org.qiyu.live.user.provider.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = "rocketmq.consumer")
public class RocketMQConsumerProperties {

    private String nameServer;
    private String groupName;

}

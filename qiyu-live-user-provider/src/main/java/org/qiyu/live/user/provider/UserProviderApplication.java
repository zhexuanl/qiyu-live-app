package org.qiyu.live.user.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.user.constants.UserTagsEnum;
import org.qiyu.live.user.provider.service.UserTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
@RequiredArgsConstructor
public class UserProviderApplication implements CommandLineRunner {

   private final UserTagService userTagService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(UserProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Override
    public void run(String... args) {
        long userId = 1001L;
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_RICH));
        System.out.println("Current user contains is_rich: " + userTagService.containTag(userId, UserTagsEnum.IS_RICH));
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("Current user contains is_old_user: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_VIP));
        System.out.println("Current user contains is_vip: " + userTagService.containTag(userId, UserTagsEnum.IS_VIP));

//        System.out.println("====================");
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println("Current user contains is_rich: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
    }
}

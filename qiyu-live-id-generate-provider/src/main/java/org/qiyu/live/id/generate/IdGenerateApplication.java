package org.qiyu.live.id.generate;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.id.generate.service.IdGenerateService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class IdGenerateApplication{

    @Resource
    private IdGenerateService idGenerateService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IdGenerateApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

//    @Override
//    public void run(String... args) {
//        for (int i = 0; i < 1000; i++) {
//            Long id = idGenerateService.getUnSeqId(2);
//            System.out.println(id);
//        }
//    }
}

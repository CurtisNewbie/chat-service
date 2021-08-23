package com.curtisnewbie.service.chat.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author yongjie.zhuang
 */
@Configuration
public class RedisConfig {

    @Value("${redisson-config}")
    private String redissonConfig;

    @Bean
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(this.getClass().getClassLoader().getResourceAsStream(redissonConfig));
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

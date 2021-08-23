package com.curtisnewbie.service.chat.config;

import com.curtisnewbie.module.messaging.config.SimpleConnectionFactoryBeanFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author yongjie.zhuang
 */
@EnableRabbit
@Configuration
public class MqConfig {

    @Autowired
    private Environment environment;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = SimpleConnectionFactoryBeanFactory.createByProperties(environment);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        return connectionFactory;
    }
}

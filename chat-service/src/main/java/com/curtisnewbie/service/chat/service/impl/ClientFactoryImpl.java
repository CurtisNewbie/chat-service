package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.ClientFactory;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author yongjie.zhuang
 */
@Component
public class ClientFactoryImpl implements ClientFactory {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Client buildClient(@NotNull UserVo user) {
        RedisClientProxy proxy = RedisClientProxy.builder()
                .redissonClient(redissonClient)
                .user(user)
                .build();
        return ClientRefreshExpirationDecorator.decorate(proxy);
    }
}

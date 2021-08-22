package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomBuilder;
import com.curtisnewbie.service.chat.service.RoomImpl;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author yongjie.zhuang
 */
@Component
public class RoomBuilderImpl implements RoomBuilder {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Room buildRoom(@NotNull String roomId) {
        return new RoomImpl(redissonClient, roomId);
    }

}

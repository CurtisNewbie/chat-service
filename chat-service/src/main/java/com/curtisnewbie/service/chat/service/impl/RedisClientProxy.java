package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Client;
import lombok.Builder;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Client domain object that is backed by a redis proxy
 * </p>
 *
 * @author yongjie.zhuang
 */
@Builder
public class RedisClientProxy implements Client {

    private static final String ROOM_ID_FIELD = "room-id";

    private UserVo user;
    private RedissonClient redissonClient;

    @Override
    public String getRoomId() {
        RMap<Object, Object> map = getClientMap();
        return (String) map.get(ROOM_ID_FIELD);
    }

    public void refreshExpiration() {
        getClientMap().expire(3, TimeUnit.HOURS);
    }

    @Override
    public void clearRoomId() {
        RMap<Object, Object> map = getClientMap();
        if (map.isExists())
            map.remove(ROOM_ID_FIELD);
    }

    @Override
    public void addRoomId(String roomId) {
        getClientMap().put(ROOM_ID_FIELD, roomId);
    }

    @Override
    public UserVo getUser() {
        return user;
    }

    private String getClientMapKey() {
        return "chat:client:" + user.getId();
    }

    private RMap<Object, Object> getClientMap() {
        return redissonClient.getMap(getClientMapKey());
    }
}

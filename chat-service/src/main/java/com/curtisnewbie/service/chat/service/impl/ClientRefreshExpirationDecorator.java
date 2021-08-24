package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Client;

/**
 * <p>
 * Decorator that calls {@link RedisClientProxy#refreshExpiration()}  for the decorated methods
 * </p>
 *
 * @author yongjie.zhuang
 */
public class ClientRefreshExpirationDecorator implements Client {

    private final RedisClientProxy client;

    private ClientRefreshExpirationDecorator(RedisClientProxy client) {
        this.client = client;
    }

    @Override
    public String getRoomId() {
        client.refreshExpiration();
        return client.getRoomId();
    }

    @Override
    public void clearRoomId() {
        client.refreshExpiration();
        client.clearRoomId();
    }

    @Override
    public void addRoomId(String roomId) {
        client.addRoomId(roomId);
        client.refreshExpiration();
    }

    @Override
    public UserVo getUser() {
        return client.getUser();
    }

    /** Decorate the given Client */
    public static Client decorate(RedisClientProxy client) {
        return new ClientRefreshExpirationDecorator(client);
    }
}

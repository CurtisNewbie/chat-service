package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.consts.RoomType;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.vo.MemberVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Decorator that calls {@link RedisRoomProxy#refreshExpiration()}  for the decorated methods
 * </p>
 *
 * @author yongjie.zhuang
 */
public class RoomRefreshExpirationDecorator implements Room {

    private final RedisRoomProxy room;

    private RoomRefreshExpirationDecorator(RedisRoomProxy room) {
        this.room = room;
    }

    @Override
    public long sendMessage(@NotNull UserVo user, @NotNull String msg) {
        room.refreshExpiration();
        return room.sendMessage(user, msg);
    }

    @Override
    public long nextMessageId() {
        room.refreshExpiration();
        return room.nextMessageId();
    }

    @Override
    public void addMember(@NotNull Client client) {
        room.addMember(client);
        room.refreshExpiration();
    }

    @Override
    public void removeMember(@NotNull Client client) {
        room.refreshExpiration();
        room.removeMember(client);
    }

    @Override
    public List<MemberVo> listMembers() {
        room.refreshExpiration();
        return room.listMembers();
    }

    @Override
    public PollMessageRespVo getMessagesAfter(long messageId, int limit) {
        room.refreshExpiration();
        return room.getMessagesAfter(messageId, limit);
    }

    @Override
    public PollMessageRespVo getLastMessage() {
        room.refreshExpiration();
        return room.getLastMessage();
    }

    @Override
    public String getRoomId() {
        return room.getRoomId();
    }

    @Override
    public void create(@NotNull Client client, @NotNull RoomType roomType, @NotEmpty String roomName) {
        room.create(client, roomType, roomName);
        room.refreshExpiration();
    }

    @Override
    public RoomType getRoomType() {
        room.refreshExpiration();
        return room.getRoomType();
    }

    @Override
    public boolean containsUser(int userId) {
        room.refreshExpiration();
        return room.containsUser(userId);
    }

    @Override
    public boolean exists() {
        room.refreshExpiration();
        return room.exists();
    }

    @Override
    public Date getCreateDate() {
        room.refreshExpiration();
        return room.getCreateDate();
    }

    @Override
    public void delete() {
        room.delete();
    }

    /**
     * Decorate given Room
     */
    public static Room decorate(RedisRoomProxy room) {
        return new RoomRefreshExpirationDecorator(room);
    }
}

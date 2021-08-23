package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.util.RoomUtil;
import com.curtisnewbie.service.chat.vo.MemberVo;
import com.curtisnewbie.service.chat.vo.MessageVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.curtisnewbie.service.chat.util.RoomUtil.getRoomInfoMapKey;
import static com.curtisnewbie.service.chat.util.RoomUtil.getRoomLockKey;

//todo consider somehow we can cache this room instance?

/**
 * @author yongjie.zhuang
 */
@Slf4j
@Builder
public class RedisRoomProxy implements Room {

    private static final String MESSAGE_ID_FIELD = "message-id";
    private final RedissonClient redisson;
    private final String roomId;

    public RedisRoomProxy(RedissonClient redisson, String roomId) {
        this.redisson = redisson;
        this.roomId = roomId;
    }

    @Override
    public void sendMessage(@NotNull UserVo user, @NotNull String msg) {
        RMap<Object, Object> roomMap = getRoomInfoMap();
        if (roomMap == null)
            throw new IllegalStateException("Room not found " + roomId);

        long lastMsgId = incrementMessageId(roomMap);
        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            getSortedMessageMap()
                    .add(lastMsgId, MessageVo.builder()
                            .sender(user.getUsername())
                            .message(msg)
                            .messageId(lastMsgId)
                            .build());
        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public void addMember(@NotNull UserVo userVo) {
        getRoomInfoMap().put(userVo.getId(), userVo.getUsername());
    }

    @Override
    public void removeMember(@NotNull UserVo userVo) {
        getRoomInfoMap().remove(userVo.getId());
    }

    @Override
    public List<MemberVo> listMembers() {
        // todo optimise this, use a map for this maybe
        List<MemberVo> members = new ArrayList<>();
        RMap<Object, Object> roomInfoMap = getRoomInfoMap();
        Set<Object> keys = roomInfoMap.keySet();
        for (Object key : keys) {
            if (!Objects.equals(key, MESSAGE_ID_FIELD)) {
                members.add(MemberVo.builder()
                        .id((int) key)
                        .username((String) roomInfoMap.get(key))
                        .build());
            }
        }
        return members;
    }

    @Override
    public PollMessageRespVo getMessagesAfter(long messageId, int limit) {
        RScoredSortedSet<MessageVo> sortedMessageMap = getSortedMessageMap();
        Collection<ScoredEntry<MessageVo>> scoredEntries = sortedMessageMap.entryRange(messageId, false,
                Double.POSITIVE_INFINITY, false);
        return PollMessageRespVo.builder()
                .hasMore(false)
                .messages(scoredEntries.stream().map(s -> s.getValue()).collect(Collectors.toList()))
                .build();
    }

    @Override
    public PollMessageRespVo getLastMessage() {
        RScoredSortedSet<MessageVo> sortedMessageMap = getSortedMessageMap();
        MessageVo messageVo = sortedMessageMap.pollLast();
        return PollMessageRespVo.builder()
                .hasMore(false)
                .messages(Collections.singletonList(messageVo))
                .build();
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    @Override
    public void refreshExpiration() {
        getRoomInfoMap().expire(3, TimeUnit.HOURS);
        getSortedMessageMap().expire(3, TimeUnit.HOURS);
    }

    @Override
    public void create(@NotNull UserVo createdBy) {
        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            if (getRoomInfoMap().isExists())
                return;

            getRoomInfoMap().put(createdBy.getId(), createdBy.getUsername());
        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public boolean containsUser(int userId) {
        return getRoomInfoMap().containsKey(userId);
    }

    /**
     * Get lock for the room
     */
    private RLock getRoomLock() {
        return redisson.getLock(getRoomLockKey(roomId));
    }

    /**
     * Information of the room in map
     */
    private RMap<Object, Object> getRoomInfoMap() {
        return redisson.getMap(getRoomInfoMapKey(roomId));
    }

    /**
     * Sorted map (based on scores, which is the id of message) of messages
     */
    private RScoredSortedSet<MessageVo> getSortedMessageMap() {
        return redisson.getScoredSortedSet(RoomUtil.getMsgScoredMapKey(roomId));
    }

    /**
     * Increment and get last messageId
     *
     * @param roomMap
     * @return last id
     */
    private long incrementMessageId(RMap<Object, Object> roomMap) {
        return Long.parseLong(roomMap.addAndGet(MESSAGE_ID_FIELD, 1).toString());
    }

    /**
     * Try to lock the given RLock
     *
     * @throws InterruptedException
     */
    private boolean tryTimeoutLock(RLock lock) {
        try {
            return lock.tryLock(0, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error occurred while obtaining lock", e);
            throw new IllegalStateException(e);
        }
    }

}

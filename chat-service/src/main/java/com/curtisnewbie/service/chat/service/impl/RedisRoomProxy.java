package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.vo.MemberVo;
import com.curtisnewbie.service.chat.vo.MessageVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * Room domain object that is backed by a redis proxy
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Builder
public class RedisRoomProxy implements Room {

    private static final String CREATE_DATE_FIELD = "created-at";
    private static final String MESSAGE_ID_FIELD = "message-id";
    private final RedissonClient redisson;
    private final String roomId;

    public RedisRoomProxy(RedissonClient redisson, String roomId) {
        this.redisson = redisson;
        this.roomId = roomId;
    }

    @Override
    public long sendMessage(@NotNull UserVo user, @NotNull String msg) {
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
        return lastMsgId;
    }

    @Override
    public void addMember(@NotNull Client client) {
        UserVo user = client.getUser();
        getRoomInfoMap().put(user.getId(), user.getUsername());
        client.addRoomId(roomId);
        sendMessage(user, "Welcome! " + user.getUsername() + " just joined the room");
    }

    @Override
    public void removeMember(@NotNull Client client) {
        UserVo user = client.getUser();
        getRoomInfoMap().remove(user.getId());
        client.clearRoomId();
    }

    @Override
    public List<MemberVo> listMembers() {
        List<MemberVo> members = new ArrayList<>();
        Map<Object, Object> roomInfoMap = getRoomInfoMap().readAllMap();
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
                Double.POSITIVE_INFINITY, false, 0, limit);

        List<MessageVo> messages = scoredEntries.stream()
                .map(s -> s.getValue())
                .sorted(Comparator.comparing(MessageVo::getMessageId))
                .collect(Collectors.toList());

        // check if the last one is the one with greatest score
        boolean hasMore = !messages.isEmpty() && !messages.get(messages.size() - 1).equals(sortedMessageMap.lastScore());

        return PollMessageRespVo.builder()
                .hasMore(hasMore)
                .messages(messages)
                .build();
    }

    @Override
    public PollMessageRespVo getLastMessage() {
        RScoredSortedSet<MessageVo> sortedMessageMap = getSortedMessageMap();
        MessageVo messageVo = sortedMessageMap.last();
        return PollMessageRespVo.builder()
                .hasMore(false)
                .messages(messageVo != null ? Collections.singletonList(messageVo) : Collections.emptyList())
                .build();
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    public void refreshExpiration() {
        getRoomInfoMap().expire(3, TimeUnit.HOURS);
        getSortedMessageMap().expire(3, TimeUnit.HOURS);
    }

    @Override
    public void create(@NotNull Client client) {
        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            if (getRoomInfoMap().isExists())
                return;

            UserVo createdBy = client.getUser();
            RMap<Object, Object> roomInfoMap = getRoomInfoMap();
            roomInfoMap.put(createdBy.getId(), createdBy.getUsername());
            roomInfoMap.put(CREATE_DATE_FIELD, new Date());
            client.addRoomId(roomId);
        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public boolean containsUser(int userId) {
        return getRoomInfoMap().containsKey(userId);
    }

    @Override
    public boolean exists() {
        return getRoomInfoMap().isExists();
    }

    @Override
    public Date getCreateDate() {
        RMap<Object, Object> roomInfoMap = getRoomInfoMap();
        if (!roomInfoMap.isExists())
            return null;
        return (Date) roomInfoMap.get(CREATE_DATE_FIELD);
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
        return redisson.getScoredSortedSet(getMsgScoredMapKey(roomId));
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

    /**
     * Get lock key for the room
     */
    private static String getRoomLockKey(String roomId) {
        return "room:lock:" + roomId;
    }

    /**
     * Get key for zset for messages of the room
     */
    private static String getMsgScoredMapKey(String roomId) {
        return "room:message:" + roomId;
    }

    /**
     * Get map for information of the room
     */
    private static String getRoomInfoMapKey(String roomId) {
        return "room:info:" + roomId;
    }

}

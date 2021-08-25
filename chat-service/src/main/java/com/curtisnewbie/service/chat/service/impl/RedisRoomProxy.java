package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.common.util.EnumUtils;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.consts.RoomType;
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

import javax.validation.constraints.NotEmpty;
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
    private static final String ROOM_TYPE_FIELD = "room-type";
    private static final String ROOM_NAME_FIELD = "room-name";
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
    public long nextMessageId() {
        return incrementMessageId(getRoomInfoMap());
    }

    @Override
    public void addMember(@NotNull Client client) {
        UserVo user = client.getUser();

        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            getRoomInfoMap().fastPut(user.getId(), user.getUsername());
            client.addRoomId(roomId);
        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public void removeMember(@NotNull Client client) {
        UserVo user = client.getUser();

        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            getRoomInfoMap().remove(user.getId());
            Integer roomTypeValue = (Integer) getRoomInfoMap().get(ROOM_TYPE_FIELD);
            if (roomTypeValue != null) {
                RoomType roomType = EnumUtils.parse(roomTypeValue, RoomType.class);
                if (Objects.equals(roomType, RoomType.PRIVATE) && listMembers().isEmpty()) {
                    // for private rooms, when there is no members in it, remove it
                    log.info("Private room {} is empty, removing...", roomId);
                    delete();
                }
            }
        } finally {
            roomLock.unlock();
        }
        client.clearRoomId();
    }

    @Override
    public List<MemberVo> listMembers() {
        List<MemberVo> members = new ArrayList<>();
        Map<Object, Object> roomInfoMap = getRoomInfoMap().readAllMap();
        if (roomInfoMap == null)
            return Collections.emptyList();

        Set<Object> keys = roomInfoMap.keySet();
        for (Object key : keys) {
            if (!Objects.equals(key, MESSAGE_ID_FIELD)
                    && !Objects.equals(key, CREATE_DATE_FIELD)
                    && !Objects.equals(key, ROOM_TYPE_FIELD)
                    && !Objects.equals(key, ROOM_NAME_FIELD)) {
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
        getRoomInfoMap().expire(10, TimeUnit.MINUTES);
        getSortedMessageMap().expire(10, TimeUnit.MINUTES);
    }

    @Override
    public void create(@NotNull Client client, @NotNull RoomType roomType, @NotEmpty String roomName) {
        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;

            if (getRoomInfoMap().isExists())
                return;

            UserVo createdBy = client.getUser();
            client.addRoomId(roomId);
            RMap<Object, Object> roomInfoMap = getRoomInfoMap();
            roomInfoMap.fastPut(createdBy.getId(), createdBy.getUsername());
            roomInfoMap.fastPut(ROOM_TYPE_FIELD, roomType.getValue());
            roomInfoMap.fastPut(CREATE_DATE_FIELD, new Date());
            roomInfoMap.fastPut(ROOM_NAME_FIELD, roomName);
        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public RoomType getRoomType() {
        Integer v = (Integer) getRoomInfoMap().get(ROOM_TYPE_FIELD);
        if (v == null)
            return null;
        return EnumUtils.parse(v, RoomType.class);
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

    @Override
    public void delete() {
        RLock roomLock = getRoomLock();
        try {
            while (!tryTimeoutLock(roomLock))
                ;
            getSortedMessageMap().deleteAsync();
            getRoomInfoMap().deleteAsync();
        } finally {
            roomLock.unlock();
        }
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
    public static final String getRoomLockKey(String roomId) {
        return "chat:room:lock:" + roomId;
    }

    /**
     * Get key for zset for messages of the room
     */
    public static final String getMsgScoredMapKey(String roomId) {
        return "chat:room:message:" + roomId;
    }

    /**
     * Get map for information of the room
     */
    public static final String getRoomInfoMapKey(String roomId) {
        return "chat:room:info:" + roomId;
    }

}

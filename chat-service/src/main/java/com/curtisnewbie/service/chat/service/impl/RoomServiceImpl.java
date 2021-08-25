package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.common.util.EnumUtils;
import com.curtisnewbie.service.chat.consts.RoomType;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomFactory;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.CreateRoomReqVo;
import com.curtisnewbie.service.chat.vo.ListPublicRoomRespVo;
import com.curtisnewbie.service.chat.vo.RoomVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yongjie.zhuang
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomFactory roomFactory;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean roomExists(@NotNull String roomId) {
        Room room = roomFactory.buildRoom(roomId);
        return room.exists();
    }

    @Override
    public Room getRoom(@NotNull String roomId) throws RoomNotFoundException {
        Room room = roomFactory.buildRoom(roomId);
        if (!room.exists())
            throw new RoomNotFoundException("Room is not found, it may have been removed");
        return room;
    }

    @Override
    public Room createNewRoom(@NotNull Client client, @NotNull CreateRoomReqVo req) {
        RoomType type = EnumUtils.parse(req.getRoomType(), RoomType.class);
        Objects.requireNonNull(type, "Unable to parse room_type value, value illegal");

        Room room = roomFactory.buildRoom(UUID.randomUUID().toString());
        room.create(client, type, req.getRoomName());

        // put the room into a public room list
        if (Objects.equals(type, RoomType.PUBLIC)) {
            RLock publicRoomsLock = getPublicRoomsLock();
            try {
                while (!tryTimeoutLock(publicRoomsLock))
                    ;
                getPublicRoomList().add(room.getRoomId());
                getPublicRoomMap().put(
                        room.getRoomId(), RoomVo.builder()
                                .roomName(req.getRoomName())
                                .roomId(room.getRoomId())
                                .createdBy(client.getUser().getUsername())
                                .build()
                );
            } finally {
                publicRoomsLock.unlock();
            }
        }
        return room;
    }

    @Override
    public ListPublicRoomRespVo getPublicRoomsInfo(int page, int limit) {
        RLock publicRoomsLock = getPublicRoomsLock();
        try {
            while (!tryTimeoutLock(publicRoomsLock))
                ;

            Set<String> publicRoomIds = getPublicRoomIds(page, limit);
            List<RoomVo> publicRooms = new ArrayList<>(getPublicRoomMap().getAll(publicRoomIds).values());

            return ListPublicRoomRespVo.builder()
                    .rooms(publicRooms)
                    .total(getPublicRoomMap().size())
                    .build();
        } finally {
            publicRoomsLock.unlock();
        }
    }

    @Override
    public Set<String> getPublicRoomIds(int page, int limit) {
        if (page < 0)
            page = 1;
        if (limit < 0)
            limit = 10;

        int start = 0;
        if (page > 1)
            start = (page - 1) * limit;

        RLock publicRoomsLock = getPublicRoomsLock();
        try {
            while (!tryTimeoutLock(publicRoomsLock))
                ;

            return getPublicRoomList()
                    .range(start, page * limit - 1)
                    .stream()
                    .collect(Collectors.toSet());
        } finally {
            publicRoomsLock.unlock();
        }
    }

    @Override
    public void removeFromPublicRooms(Set<String> roomIds) {
        RLock publicRoomsLock = getPublicRoomsLock();
        try {
            while (!tryTimeoutLock(publicRoomsLock))
                ;

            RFuture<Boolean> rmvListFuture = getPublicRoomList().removeAllAsync(roomIds);

            String[] ra = new String[roomIds.size()];
            Iterator<String> iter = roomIds.iterator();
            for (int i = 0; iter.hasNext(); i++)
                ra[i] = iter.next();
            RFuture<Long> rmvMapFuture = getPublicRoomMap().fastRemoveAsync(ra);

            // we don't need the result, but we want them to finish before we exit the method
            rmvListFuture.get();
            rmvMapFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Failed to remove from public rooms", e);
        } finally {
            publicRoomsLock.unlock();
        }
    }

    /** public room ids */
    private RList<String> getPublicRoomList() {
        return redissonClient.getList(getPublicRoomListKey());
    }

    /** public rooms' actual information (RoomVo) */
    private RMap<String, RoomVo> getPublicRoomMap() {
        return redissonClient.getMap(getPublicRoomMapKey());
    }

    private String getPublicRoomListKey() {
        return "chat:room:public:list";
    }

    private String getPublicRoomMapKey() {
        return "chat:room:public:map";
    }

    private String getPublicRoomsLockKey() {
        return "chat:room:public:lock";
    }

    private RLock getPublicRoomsLock() {
        return redissonClient.getLock(getPublicRoomsLockKey());
    }

    /**
     * Try to lock the given RLock
     *
     * @throws InterruptedException
     */
    private boolean tryTimeoutLock(RLock lock) {
        try {
            return lock.tryLock(0, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error occurred while obtaining lock", e);
            throw new IllegalStateException(e);
        }
    }
}

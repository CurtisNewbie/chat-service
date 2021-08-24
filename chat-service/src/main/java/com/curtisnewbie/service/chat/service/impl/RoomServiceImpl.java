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
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * @author yongjie.zhuang
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomFactory roomFactory;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Room getRoom(@NotNull String roomId) throws RoomNotFoundException {
        Room room = roomFactory.buildRoom(roomId);
        if (!room.exists())
            throw new RoomNotFoundException("Room is not found");
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
            getPublicRoomList()
                    .add(RoomVo.builder()
                            .roomName(req.getRoomName())
                            .roomId(room.getRoomId())
                            .build());
        }
        return room;
    }

    @Override
    public ListPublicRoomRespVo getPublicRoomsInfo(int page, int limit) {
        if (page < 0)
            page = 1;
        if (limit < 0)
            limit = 10;
        RList<RoomVo> publicRoomList = getPublicRoomList();
        int start = 0;
        if (page > 1)
            start = page - 1 * limit;

        return ListPublicRoomRespVo.builder()
                .rooms(publicRoomList.range(start, page * limit - 1))
                .total(publicRoomList.size())
                .build();
    }

    private RList<RoomVo> getPublicRoomList() {
        return redissonClient.getList(getPublicRoomListKey());
    }

    private String getPublicRoomListKey() {
        return "chat:room:public:list";
    }
}

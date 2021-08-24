package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.common.util.EnumUtils;
import com.curtisnewbie.module.redisutil.RedisController;
import com.curtisnewbie.service.chat.consts.RoomType;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomFactory;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.CreateRoomReqVo;
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
    private RedisController redisController;

    @Autowired
    private RoomFactory roomFactory;

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

        // todo type is not implemented yet

        Room room = roomFactory.buildRoom(UUID.randomUUID().toString());
        room.create(client);
        return room;
    }
}

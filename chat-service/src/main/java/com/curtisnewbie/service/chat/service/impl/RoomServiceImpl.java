package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.common.util.EnumUtils;
import com.curtisnewbie.module.redisutil.RedisController;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.consts.RoomType;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomBuilder;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.util.RoomUtil;
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
    private RoomBuilder roomBuilder;

    @Override
    public Room getRoom(@NotNull String roomId) throws RoomNotFoundException {
        if (!redisController.exists(RoomUtil.getRoomInfoMapKey(roomId)))
            throw new RoomNotFoundException(roomId);

        return roomBuilder.buildRoom(roomId);
    }

    @Override
    public Room createNewRoom(@NotNull UserVo user, @NotNull CreateRoomReqVo req) {
        RoomType type = EnumUtils.parse(req.getRoomType(), RoomType.class);
        Objects.requireNonNull(type, "Unable to parse room_type value, value illegal");

        // todo type is not implemented yet

        Room room = roomBuilder.buildRoom(UUID.randomUUID().toString());
        room.create(user);
        room.refreshExpiration();
        return room;
    }
}
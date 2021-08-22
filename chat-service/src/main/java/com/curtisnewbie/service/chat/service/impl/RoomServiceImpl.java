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
import com.curtisnewbie.service.chat.vo.MemberVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
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
    public Room getRoom(UserVo user, @NotNull String roomId) throws RoomNotFoundException {
        if (!redisController.exists(RoomUtil.getRoomInfoMapKey(roomId)))
            throw new RoomNotFoundException(roomId);

        return roomBuilder.buildRoom(roomId);
    }

    @Override
    public String createNewRoom(@NotNull UserVo user, @NotNull CreateRoomReqVo req) {
        RoomType type = EnumUtils.parse(req.getRoomType(), RoomType.class);
        Objects.requireNonNull(type, "Unable to parse room_type value, value illegal");

        // todo type is not implemented yet

        Room room = roomBuilder.buildRoom(UUID.randomUUID().toString());
        room.create(user);
        return room.getRoomId();
    }

    @Override
    public boolean isUserInRoom(@NotNull UserVo user, @NotEmpty String roomId) {
        Room room = roomBuilder.buildRoom(roomId);
        return room.contains(user.getId());
    }

    @Override
    public void connectToRoom(@NotNull UserVo user, @NotEmpty String roomId) throws RoomNotFoundException {
        Room room = roomBuilder.buildRoom(roomId);
        room.addMember(user);
    }

    @Override
    public boolean roomRequiresToken(@NotEmpty String roomId) throws RoomNotFoundException {
        // todo not implemented yet
        return false;
    }

    @Override
    public void connectToRoom(@NotNull UserVo user, @NotEmpty String roomId, @NotEmpty String invitationToken) throws RoomNotFoundException {
        // todo not implemented yet
    }

    @Override
    public void disconnectFromRoom(@NotNull UserVo user, @NotEmpty String roomId) {
        Room room = roomBuilder.buildRoom(roomId);
        room.removeMember(user);
    }

    @Override
    public List<MemberVo> listMembers(@NotEmpty String roomId) throws RoomNotFoundException {
        Room room = roomBuilder.buildRoom(roomId);
        return room.listMembers();
    }

}

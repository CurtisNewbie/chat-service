package com.curtisnewbie.service.chat.web;

import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.module.auth.util.AuthUtil;
import com.curtisnewbie.service.auth.remote.exception.InvalidAuthenticationException;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// todo change to web socket instead (in the future :D)
/**
 * <p>
 * Controller for rooms
 * </p>
 *
 * @author yongjie.zhuang
 */
@RequestMapping("${web.base-path}/room")
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/new")
    public Result<String> createNewRoom(@RequestBody CreateRoomReqVo vo) throws InvalidAuthenticationException {
        return Result.of(roomService.createNewRoom(AuthUtil.getUser(), vo).getRoomId());
    }

    @PostMapping("/connect")
    public Result<Void> connectToRoom(@RequestBody ConnectRoomReqVo v) throws InvalidAuthenticationException,
            RoomNotFoundException {
        Room room = roomService.getRoom(v.getRoomId());
        room.addMember(AuthUtil.getUser());
        return Result.ok();
    }

    @PostMapping("/members")
    public Result<List<MemberVo>> listMembers(@RequestBody ListRoomMembersReqVo vo) throws InvalidAuthenticationException,
            RoomNotFoundException {

        Room room = roomService.getRoom(vo.getRoomId());
        if (!room.containsUser(AuthUtil.getUserId()))
            return Result.error("You are not in this room anymore, or the room has been removed");

        return Result.of(room.listMembers());
    }

    @PostMapping("/message/poll")
    public Result<PollMessageRespVo> pollMessages(@RequestBody PollMessageReqVo vo) throws InvalidAuthenticationException,
            RoomNotFoundException {

        Room room = roomService.getRoom(vo.getRoomId());
        if (!room.containsUser(AuthUtil.getUserId()))
            return Result.error("You are not in this room anymore, or the room has been removed");

        if (vo.getLastMessageId() == null)
            return Result.of(room.getLastMessage());
        else
            return Result.of(room.getMessagesAfter(vo.getLastMessageId(), vo.getLimit()));
    }

}

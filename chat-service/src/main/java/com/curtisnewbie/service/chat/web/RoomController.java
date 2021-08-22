package com.curtisnewbie.service.chat.web;

import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.module.auth.util.AuthUtil;
import com.curtisnewbie.service.auth.remote.exception.InvalidAuthenticationException;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.exceptions.UserNotInRoomException;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * Controller for rooms
 * </p>
 *
 * @author yongjie.zhuang
 */
@RequestMapping("${baseApi}/room")
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/new")
    public Result<String> createNewRoom(@RequestBody CreateRoomReqVo vo) throws InvalidAuthenticationException {
        return Result.of(roomService.createNewRoom(AuthUtil.getUser(), vo));
    }

    @PostMapping("/connect")
    public Result<Void> connectToRoom(@RequestBody ConnectRoomReqVo v) throws InvalidAuthenticationException,
            RoomNotFoundException {
        roomService.connectToRoom(AuthUtil.getUser(), v.getRoomId());
        return Result.ok();
    }

    @PostMapping("/members")
    public Result<List<MemberVo>> listMembers(@RequestBody ListRoomMembersReqVo vo) throws InvalidAuthenticationException,
            RoomNotFoundException {
        return Result.of(roomService.listMembers(vo.getRoomId()));
    }

    @PostMapping("/message/poll")
    public Result<PollMessageRespVo> pollMessages(@RequestBody PollMessageReqVo vo) throws InvalidAuthenticationException,
            UserNotInRoomException,
            RoomNotFoundException {
        if (!roomService.isUserInRoom(AuthUtil.getUser(), vo.getRoomId()))
            return Result.error("You are not in this room anymore, or the room has been removed");

        Room room = roomService.getRoom(AuthUtil.getUser(), vo.getRoomId());

        if (vo.getLastMessageId() == null)
            return Result.of(room.getLastMessage());
        else
            return Result.of(room.getMessagesAfter(vo.getLastMessageId(), vo.getLimit()));
    }

}

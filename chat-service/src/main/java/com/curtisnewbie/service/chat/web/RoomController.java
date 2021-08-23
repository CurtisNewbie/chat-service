package com.curtisnewbie.service.chat.web;

import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.module.auth.util.AuthUtil;
import com.curtisnewbie.service.auth.remote.exception.InvalidAuthenticationException;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.ClientService;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// todo fix non-atomic operations later, for now, the inconsistency seems okay, atomicity doesn't seem like a must

/**
 * <p>
 * Controller for rooms
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@RequestMapping("${web.base-path}/room")
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;
    @Autowired
    private ClientService clientService;

    @PostMapping("/new")
    public Result<String> createNewRoom(@RequestBody CreateRoomReqVo vo) throws InvalidAuthenticationException {
        Room room = roomService.createNewRoom(AuthUtil.getUser(), vo);
        log.info("Room created");
        return Result.of(room.getRoomId());
    }

    @PostMapping("/connect")
    public Result<Void> connectToRoom(@RequestBody ConnectRoomReqVo v) throws InvalidAuthenticationException,
            RoomNotFoundException {
        UserVo user = AuthUtil.getUser();
        Room room = roomService.getRoom(v.getRoomId());
        Client client = clientService.getClient(user);
        room.addMember(client);

        log.info("Connected to room");
        return Result.ok();
    }

    @PostMapping("/disconnect")
    public Result<Void> disconnectFromRoom(@RequestBody DisconnectRoomReqVo v) throws InvalidAuthenticationException,
            RoomNotFoundException {
        Room room = roomService.getRoom(v.getRoomId());
        Client client = clientService.getClient(AuthUtil.getUser());
        room.removeMember(client);

        log.info("Disconnected from room");
        return Result.ok();
    }

    @PostMapping("/members")
    public Result<List<MemberVo>> listMembers(@RequestBody ListRoomMembersReqVo vo) throws InvalidAuthenticationException,
            RoomNotFoundException {
        Room room = roomService.getRoom(vo.getRoomId());
        if (!room.containsUser(AuthUtil.getUserId()))
            return Result.error("You are not in this room anymore, or the room has been removed");

        log.info("Listed members of room");
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

    @PostMapping("/message/post")
    public Result<Void> sendMessages(@RequestBody SendMessageReqVo vo) throws InvalidAuthenticationException,
            RoomNotFoundException {
        UserVo user = AuthUtil.getUser();

        Room room = roomService.getRoom(vo.getRoomId());
        if (!room.containsUser(user.getId()))
            return Result.error("You are not in this room anymore, or the room has been removed");

        room.sendMessage(user, vo.getMessage());
        log.info("Sent messages");
        return Result.ok();
    }

}

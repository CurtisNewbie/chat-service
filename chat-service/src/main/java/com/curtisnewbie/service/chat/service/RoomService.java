package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.exceptions.UserNotInRoomException;
import com.curtisnewbie.service.chat.vo.CreateRoomReqVo;
import com.curtisnewbie.service.chat.vo.MemberVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * Service for rooms
 * </p>
 *
 * @author yongjie.zhuang
 */
@Validated
public interface RoomService {

    /**
     * Get room
     *
     * @param user   user
     * @param roomId roomId
     */
    Room getRoom(UserVo user, @NotNull String roomId) throws RoomNotFoundException;

    /**
     * Create a new room
     *
     * @param user user
     * @param req  request param
     * @return id of the room
     */
    String createNewRoom(@NotNull UserVo user, @NotNull CreateRoomReqVo req);

    /**
     * Check if the user is in room
     *
     * @param user   user
     * @param roomId roomId
     * @return true if so, else false
     */
    boolean isUserInRoom(@NotNull UserVo user, @NotEmpty String roomId);

    /**
     * Connect current user to existing room
     *
     * @param user   user
     * @param roomId roomId
     * @throws RoomNotFoundException when the room is not found
     */
    void connectToRoom(@NotNull UserVo user, @NotEmpty String roomId) throws RoomNotFoundException;

    /**
     * Check if the room requires invitation token
     *
     * @param roomId roomId
     * @return true if invitation token is needed else false
     * @throws RoomNotFoundException
     */
    boolean roomRequiresToken(@NotEmpty String roomId) throws RoomNotFoundException;

    /**
     * Connect to existing room
     *
     * @param user            user
     * @param roomId          roomId
     * @param invitationToken invitation token
     * @throws RoomNotFoundException when the room is not found
     */
    void connectToRoom(@NotNull UserVo user, @NotEmpty String roomId, @NotEmpty String invitationToken) throws RoomNotFoundException;

    /**
     * Disconnect from room
     *
     * @param user   user
     * @param roomId roomId
     */
    void disconnectFromRoom(@NotNull UserVo user, @NotEmpty String roomId);

    /**
     * List members
     *
     * @param roomId roomId
     */
    List<MemberVo> listMembers(@NotEmpty String roomId) throws RoomNotFoundException;

}


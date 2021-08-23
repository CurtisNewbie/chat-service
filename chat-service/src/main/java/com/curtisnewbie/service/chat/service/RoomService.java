package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.exceptions.UserNotInRoomException;
import com.curtisnewbie.service.chat.vo.CreateRoomReqVo;
import com.curtisnewbie.service.chat.vo.MemberVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

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
     * @param roomId roomId
     */
    Room getRoom(@NotNull String roomId) throws RoomNotFoundException;

    /**
     * Create a new room
     *
     * @param user user
     * @param req  request param
     */
    Room createNewRoom(@NotNull UserVo user, @NotNull CreateRoomReqVo req);

}


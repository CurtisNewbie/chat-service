package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.vo.CreateRoomReqVo;
import com.curtisnewbie.service.chat.vo.ListPublicRoomRespVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Set;

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
     * Check if the room exists
     *
     * @param roomId
     */
    boolean roomExists(@NotNull String roomId);

    /**
     * Get room
     *
     * @param roomId roomId
     * @throws RoomNotFoundException if the room doesn't exists
     */
    Room getRoom(@NotNull String roomId) throws RoomNotFoundException;

    /**
     * Create a new room
     *
     * @param client the user
     * @param req    request param
     */
    Room createNewRoom(@NotNull Client client, @NotNull CreateRoomReqVo req);

    /**
     * Get public rooms info
     *
     * @param page  page
     * @param limit limit
     */
    ListPublicRoomRespVo getPublicRoomsInfo(int page, int limit);


    /**
     * Get public room ids
     *
     * @param page
     * @param limit
     */
    Set<String> getPublicRoomIds(int page, int limit);

    /**
     * Remove specified public rooms
     */
    void removeFromPublicRooms(Set<String> roomIds);
}


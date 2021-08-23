package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * <p>
 * Request vo for disconnecting from room
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class DisconnectRoomReqVo {

    /**
     * Id of the room
     */
    private String roomId;
}

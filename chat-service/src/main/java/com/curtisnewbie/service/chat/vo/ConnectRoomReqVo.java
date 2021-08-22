package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * <p>
 * Request vo for connecting to room
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class ConnectRoomReqVo {

    /**
     * Id of the room
     */
    private String roomId;
}

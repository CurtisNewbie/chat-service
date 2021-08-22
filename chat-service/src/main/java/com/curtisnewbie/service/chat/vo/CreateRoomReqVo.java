package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * Request vo for creating new room
 *
 * @author yongjie.zhuang
 */
@Data
public class CreateRoomReqVo {

    /**
     * Room type {@link com.curtisnewbie.service.chat.consts.RoomType}
     */
    private Integer roomType;

}

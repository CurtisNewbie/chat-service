package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * <p>
 * Request vo for listing members in room
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class ListRoomMembersReqVo {

    /**
     * Room's id
     */
    private String roomId;
}

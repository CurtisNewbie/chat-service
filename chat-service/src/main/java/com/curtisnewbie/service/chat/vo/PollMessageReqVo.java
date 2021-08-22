package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * Request vo for polling message
 *
 * @author yongjie.zhuang
 */
@Data
public class PollMessageReqVo {

    /**
     * Room's id
     */
    private String roomId;

    /**
     * Last message's id
     */
    private Integer lastMessageId;

    /**
     * Limit
     */
    private Integer limit;

}

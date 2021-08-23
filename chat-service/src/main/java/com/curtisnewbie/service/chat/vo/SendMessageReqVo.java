package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * Request vo for sending message
 *
 * @author yongjie.zhuang
 */
@Data
public class SendMessageReqVo {

    /**
     * Room's id
     */
    private String roomId;

    /**
     * Message
     */
    private String message;

}

package com.curtisnewbie.service.chat.vo;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * Message Vo
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
@Builder
public class MessageVo {

    /**
     * Who sent the message
     */
    private String sender;

    /**
     * Message
     */
    private String message;

    /**
     * message id
     */
    private Long messageId;
}

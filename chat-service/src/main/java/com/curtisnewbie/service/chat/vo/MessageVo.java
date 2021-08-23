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

    @Builder
    public MessageVo(String sender, String message, Long messageId) {
        this.sender = sender;
        this.message = message;
        this.messageId = messageId;
    }

    public MessageVo() {
    }
}

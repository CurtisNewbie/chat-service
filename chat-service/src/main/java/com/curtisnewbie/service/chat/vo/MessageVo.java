package com.curtisnewbie.service.chat.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

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

    /**
     * When the message was sent
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Date dateSent;

    @Builder
    public MessageVo(String sender, String message, Long messageId, Date dateSent) {
        this.sender = sender;
        this.message = message;
        this.messageId = messageId;
        this.dateSent = dateSent;
    }

    public MessageVo() {
    }
}

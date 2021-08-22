package com.curtisnewbie.service.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Message Vo
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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

package com.curtisnewbie.service.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response vo for polling messages
 *
 * @author yongjie.zhuang
 */
@Data
@Builder
public class PollMessageRespVo {

    /**
     * Messages
     */
    private List<MessageVo> messages;

    /**
     * Whether there are more messages to poll
     */
    private Boolean hasMore;

}

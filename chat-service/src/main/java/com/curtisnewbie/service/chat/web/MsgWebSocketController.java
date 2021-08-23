package com.curtisnewbie.service.chat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yongjie.zhuang
 */
@Controller
public class MsgWebSocketController {

    @RequestMapping("/socket/messages")
    public void handleRoomSocketMessages() {
        // do nothing, this is handled by RoomMessageWebSocketHandler
    }

}

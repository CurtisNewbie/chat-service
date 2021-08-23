package com.curtisnewbie.service.chat.config;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.ClientService;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.curtisnewbie.common.util.JsonUtils.writeValueAsString;

/**
 * <p>
 * WebSocket Handler for room's messages
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class RoomMessageWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ClientService clientService;

    private Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserVo user = getPrincipal(session);
        log.info("User {} connected to web socket for messages", user.getUsername());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserVo user = getPrincipal(session);
        log.info("User {} disconnected from web socket for messages", user.getUsername());
    }

    @Override
    protected void handleTextMessage(WebSocketSession currSession, TextMessage msgIn) throws Exception {
        super.handleTextMessage(currSession, msgIn);

        UserVo user = getPrincipal(currSession);

        // get rooms of client
        String roomId = clientService.getClient(user).getRoomId();

        // get all members' sessions, and send the messages
        Room room = roomService.getRoom(roomId);

        // send the message to redis server
        final long msgId = room.sendMessage(user, msgIn.getPayload());

        // construct the actual message with information such as sender and messageId
        TextMessage msgToSend = new TextMessage(
                writeValueAsString(
                        MessageVo.builder()
                                .message(msgIn.getPayload())
                                .messageId(msgId)
                                .sender(user.getUsername())
                                .build()
                )
        );

        // send messages to these members
        try {
            currSession.sendMessage(msgToSend);
        } catch (IOException e) {
            log.warn("Failed to send message", e);
        }
        room.listMembers().forEach(m -> {
            WebSocketSession wss = sessions.get(m.getId());
            if (wss != null && wss.isOpen()) {
                try {
                    wss.sendMessage(msgToSend);
                } catch (IOException e) {
                    log.warn("Failed to send message", e);
                }
            }
        });
    }

    private UserVo getPrincipal(WebSocketSession session) {
        return (UserVo) session.getPrincipal();
    }
}

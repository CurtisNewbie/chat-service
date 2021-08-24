package com.curtisnewbie.service.chat.config;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.ClientService;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.MessageVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        // send the last message immediately, if there is one
        String roomId = clientService.getClient(user).getRoomId();
        Room room = roomService.getRoom(roomId);
        PollMessageRespVo messages = room.getLastMessage();
        if (!messages.getMessages().isEmpty())
            writeMessage(session, buildMessage(messages.getMessages().get(0)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserVo user = getPrincipal(session);
        log.info("User {} disconnected from web socket for messages", user.getUsername());
    }

    @Override
    protected void handleTextMessage(WebSocketSession currSession, TextMessage msgIn) throws Exception {
        MessageVo receivedMessage = JsonUtils.readValueAsObject(msgIn.getPayload(), MessageVo.class);

        UserVo user = getPrincipal(currSession);

        // get rooms of client
        String roomId = clientService.getClient(user).getRoomId();

        // get all members' sessions, and send the messages
        Room room = roomService.getRoom(roomId);

        // send the message to redis server
        final long msgId = room.sendMessage(user, msgIn.getPayload());

        // construct the actual message with information such as sender and messageId
        TextMessage msgToSend = buildMessage(
                MessageVo.builder()
                        .message(receivedMessage.getMessage())
                        .messageId(msgId)
                        .sender(user.getUsername())
                        .build()
        );

        writeMessage(currSession, msgToSend);
        room.listMembers().forEach(m -> {
            WebSocketSession wss = sessions.get(m.getId());
            if (wss != null && wss.isOpen()) {
                writeMessage(wss, msgToSend);
            }
        });
    }

    private UserVo getPrincipal(WebSocketSession session) {
        return (UserVo) ((UsernamePasswordAuthenticationToken) session.getPrincipal()).getPrincipal();
    }

    private void writeMessage(WebSocketSession session, TextMessage msg) {
        // send messages to these members
        try {
            session.sendMessage(msg);
        } catch (IOException e) {
            log.warn("Failed to send message", e);
        }
    }

    private TextMessage buildMessage(MessageVo messageVo) throws JsonProcessingException {
        return new TextMessage(
                writeValueAsString(
                        messageVo
                ));
    }

}

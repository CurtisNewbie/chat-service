package com.curtisnewbie.service.chat.config;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.ClientService;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import com.curtisnewbie.service.chat.vo.MessageVo;
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
import java.util.Date;
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

    private Map<Integer, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserVo user = getPrincipal(session);
        sessionMap.put(user.getId(), session);
        broadcastRoomMessage(user, user.getUsername() + " just joined the room", session);
        log.info("User {} connected to web socket for messages", user.getUsername());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserVo user = getPrincipal(session);
        sessionMap.remove(user.getId());
        log.info("User {} disconnected from web socket for messages, close_status: {}", user.getUsername(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage msgIn) throws Exception {
        MessageVo receivedMessage = JsonUtils.readValueAsObject(msgIn.getPayload(), MessageVo.class);
        UserVo sender = getPrincipal(senderSession);
        broadcastRoomMessage(sender, receivedMessage.getMessage(), senderSession);
    }

    private void broadcastRoomMessage(UserVo sender, String content, WebSocketSession senderSession) throws RoomNotFoundException,
            JsonProcessingException {
        // get rooms of client
        String roomId = clientService.getClient(sender).getRoomId();

        // get all members' sessions, and send the messages
        Room room = roomService.getRoom(roomId);

        // todo, temporarily disable this functionality until user really need to see the chat history
//        room.sendMessage(sender, content);

        // construct the actual message with information such as sender and messageId
        TextMessage msgToSend = buildMessage(
                MessageVo.builder()
                        .message(content)
                        .messageId(room.nextMessageId())
                        .sender(sender.getUsername())
                        .dateSent(new Date())
                        .build()
        );

        writeMessage(senderSession, msgToSend);
        room.listMembers().forEach(m -> {
            WebSocketSession wss = sessionMap.get(m.getId());
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

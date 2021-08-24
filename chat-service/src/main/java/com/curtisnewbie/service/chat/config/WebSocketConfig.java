package com.curtisnewbie.service.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * @author yongjie.zhuang
 */
@Controller
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private RoomMessageWebSocketHandler roomMessageWebSocketHandler;

    @Value("${websocket.allowed-origins:}")
    private String[] allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        WebSocketHandlerRegistration reg = webSocketHandlerRegistry.addHandler(roomMessageWebSocketHandler, "/socket/messages")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler());

        if (allowedOrigins.length > 0)
            reg.setAllowedOrigins(allowedOrigins);
    }
}

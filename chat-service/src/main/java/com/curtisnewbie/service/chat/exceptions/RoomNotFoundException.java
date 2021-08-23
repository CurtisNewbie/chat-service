package com.curtisnewbie.service.chat.exceptions;

import com.curtisnewbie.common.exceptions.MsgEmbeddedException;

/**
 * Room is not found
 *
 * @author yongjie.zhuang
 */
public class RoomNotFoundException extends MsgEmbeddedException {

    public RoomNotFoundException() {
    }

    public RoomNotFoundException(String message) {
        super(message);
    }

    public RoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
